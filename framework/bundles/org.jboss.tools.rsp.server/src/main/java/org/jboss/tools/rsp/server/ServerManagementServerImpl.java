/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.SocketLauncher;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ModifyDeployableRequest;
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerCapabilitiesResponse;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.launching.utils.StatusConverter;
import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.rsp.server.model.RemoteEventManager;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class ServerManagementServerImpl implements RSPServer {
	
	private final List<RSPClient> clients = new CopyOnWriteArrayList<>();
	private final List<SocketLauncher<RSPClient>> launchers = new CopyOnWriteArrayList<>();
	
	private final IServerManagementModel managementModel;
	private final RemoteEventManager remoteEventManager;
	private ServerManagementServerLauncher launcher;
	
	public ServerManagementServerImpl(ServerManagementServerLauncher launcher, IServerManagementModel managementModel) {
		this.launcher = launcher;
		this.managementModel = managementModel;
		this.remoteEventManager = new RemoteEventManager(this);
	}
	
	public List<RSPClient> getClients() {
		return new ArrayList<>(clients);
	}
	
	/**
	 * Connect the given client.
	 * Return a runnable which should be executed to disconnect the client.
	 * This method is called *before* the server begins actually listening to the socket.
	 * Any functionality which requires sending a jsonrequest to the client
	 * should NOT be performed in this method, and should instead be performed
	 * in clientAdded instead.
	 */
	public Runnable addClient(SocketLauncher<RSPClient> launcher) {
		this.launchers.add(launcher);
		RSPClient client = launcher.getRemoteProxy();
		this.clients.add(client);
		return () -> this.removeClient(launcher);
	}

	public void clientAdded(SocketLauncher<RSPClient> launcher) {
		managementModel.clientAdded(launcher.getRemoteProxy());
	}
	
	protected void removeClient(SocketLauncher<RSPClient> launcher) {
		this.launchers.remove(launcher);
		managementModel.clientRemoved(launcher.getRemoteProxy());
		this.clients.remove(launcher.getRemoteProxy());
	}
	
	public List<SocketLauncher<RSPClient>> getActiveLaunchers() {
		return new ArrayList<>(launchers);
	}
	
	public IServerManagementModel getModel() {
		return managementModel;
	}

	/**
	 * Returns existing discovery paths.
	 */
	@Override
	public CompletableFuture<List<DiscoveryPath>> getDiscoveryPaths() {
		return CompletableFuture.completedFuture(managementModel.getDiscoveryPathModel().getPaths());
	}

	/**
	 * Adds a path to our list of discovery paths
	 */
	@Override
	public CompletableFuture<Status> addDiscoveryPath(DiscoveryPath path) {
		return createCompletableFuture(() -> addDiscoveryPathSync(path));
	}
	
	private Status addDiscoveryPathSync(DiscoveryPath path) {
		if( isEmptyDiscoveryPath(path)) 
			return invalidParameterStatus();
		String fp = path.getFilepath();
		IPath ipath = new Path(fp);
		if( !ipath.isAbsolute()) {
			return invalidParameterStatus();
		}
		boolean ret = managementModel.getDiscoveryPathModel().addPath(path);
		return booleanToStatus(ret, "Discovery path not added: " + path.getFilepath());
	}

	@Override
	public CompletableFuture<Status> removeDiscoveryPath(DiscoveryPath path) {
		return createCompletableFuture(() -> removeDiscoveryPathSync(path));
	}
	
	public Status removeDiscoveryPathSync(DiscoveryPath path) {
		if( isEmptyDiscoveryPath(path)) 
			return invalidParameterStatus();
		String fp = path.getFilepath();
		IPath ipath = new Path(fp);
		if( !ipath.isAbsolute()) {
			return invalidParameterStatus();
		}
		boolean ret = managementModel.getDiscoveryPathModel().removePath(path);
		return booleanToStatus(ret, "Discovery path not removed: " + path.getFilepath());
	}

	private boolean isEmptyDiscoveryPath(DiscoveryPath path) {
		return path == null || isEmpty(path.getFilepath());
	}

	@Override
	public CompletableFuture<List<ServerBean>> findServerBeans(DiscoveryPath path) {
		return createCompletableFuture(() -> findServerBeansSync(path));
	}

	private List<ServerBean> findServerBeansSync(DiscoveryPath path) {
		List<ServerBean> ret = new ArrayList<>();
		if( path == null || isEmpty(path.getFilepath())) {
			return ret;
		}
		
		String fp = path.getFilepath();
		IPath ipath = new Path(fp);
		if( !ipath.isAbsolute()) {
			return ret;
		}

		ServerBeanLoader loader = new ServerBeanLoader(new File(path.getFilepath()), managementModel);
		ServerBean bean = loader.getServerBean();
		if( bean != null )
			ret.add(bean);
		return ret;	
	}

	@Override
	public void shutdown() {
		launcher.shutdown();
	}

	@Override
	public CompletableFuture<List<ServerHandle>> getServerHandles() {
		return createCompletableFuture(() -> getServerHandlesSync());
	}
	
	private List<ServerHandle> getServerHandlesSync() {
		ServerHandle[] all = managementModel.getServerModel().getServerHandles();
		return Arrays.asList(all);
	}

	@Override
	public CompletableFuture<Status> deleteServer(ServerHandle handle) {
		return createCompletableFuture(() -> deleteServerSync(handle));
	}
	
	private Status deleteServerSync(ServerHandle handle) {
		if( handle == null || isEmpty(handle.getId())) {
			return invalidParameterStatus();
		}
		IServer server = managementModel.getServerModel().getServer(handle.getId());
		boolean b = managementModel.getServerModel().removeServer(server);
		return booleanToStatus(b, "Server not removed: " + handle.getId());
	}

	@Override
	public CompletableFuture<Attributes> getRequiredAttributes(ServerType type) {
		return createCompletableFuture(() -> getRequiredAttributesSync(type));
	}
	
	private Attributes getRequiredAttributesSync(ServerType type) {
		if( type == null || isEmpty(type.getId())) {
			return null;
		}
		IServerType serverType = managementModel.getServerModel().getIServerType(type.getId());
		Attributes rspa = managementModel.getServerModel().getRequiredAttributes(serverType);
		return rspa;
	}

	@Override
	public CompletableFuture<Attributes> getOptionalAttributes(ServerType type) {
		return createCompletableFuture(() -> getOptionalAttributesSync(type));
	}

	private Attributes getOptionalAttributesSync(ServerType type) {
		if( type == null || isEmpty(type.getId())) {
			return null;
		}
		IServerType serverType = managementModel.getServerModel().getIServerType(type.getId());
		return managementModel.getServerModel().getOptionalAttributes(serverType);
	}
	
	@Override
	public CompletableFuture<List<ServerLaunchMode>> getLaunchModes(ServerType type) {
		return createCompletableFuture(() -> getLaunchModesSync(type));
	}

	private List<ServerLaunchMode> getLaunchModesSync(ServerType type) {
		if( type == null || isEmpty(type.getId()) ) {
			return null;
		}
		IServerType serverType = managementModel.getServerModel().getIServerType(type.getId());
		List<ServerLaunchMode> l = managementModel.getServerModel()
				.getLaunchModes(serverType);
		return l;
	}
	
	@Override
	public CompletableFuture<Attributes> getRequiredLaunchAttributes(LaunchAttributesRequest req) {
		return createCompletableFuture(() -> getRequiredLaunchAttributesSync(req));
	}
	private Attributes getRequiredLaunchAttributesSync(LaunchAttributesRequest req) {
		if( req == null || isEmpty(req.getServerTypeId()) || isEmpty(req.getMode())) {
			return null;
		}
		IServerType serverType = managementModel.getServerModel().getIServerType(req.getServerTypeId());
		Attributes rspa = managementModel.getServerModel().getRequiredLaunchAttributes(serverType);
		return rspa;
	}

	@Override
	public CompletableFuture<Attributes> getOptionalLaunchAttributes(LaunchAttributesRequest req) {
		return createCompletableFuture(() -> getOptionalLaunchAttributesSync(req));
	}

	private Attributes getOptionalLaunchAttributesSync(LaunchAttributesRequest req) {
		if( req == null || isEmpty(req.getServerTypeId()) || isEmpty(req.getMode())) {
			return null;
		}
		IServerType serverType = managementModel.getServerModel().getIServerType(req.getServerTypeId());
		Attributes rspa = managementModel.getServerModel().getOptionalLaunchAttributes(serverType);
		return rspa;
	}
	
	@Override
	public CompletableFuture<CreateServerResponse> createServer(ServerAttributes attr) {
		return createCompletableFuture(() -> createServerSync(attr));
	}

	private CreateServerResponse createServerSync(ServerAttributes attr) {
		if( attr == null || isEmpty(attr.getId()) || isEmpty(attr.getServerType())) {
			Status s = invalidParameterStatus();
			return new CreateServerResponse(s, null);
		}
		
		String serverType = attr.getServerType();
		String id = attr.getId();
		Map<String, Object> attributes = attr.getAttributes();
		
		CreateServerResponse ret = managementModel.getServerModel().createServer(serverType, id, attributes);
		return ret;
	}

	@Override
	public CompletableFuture<List<ServerType>> getServerTypes() {
		return createCompletableFuture(() -> getServerTypesSync());
	}

	private List<ServerType> getServerTypesSync() {
		ServerType[] types = managementModel.getServerModel().getAccessibleServerTypes();
		return Arrays.asList(types);
	}

	@Override
	public CompletableFuture<StartServerResponse> startServerAsync(LaunchParameters attr) {
		return createCompletableFuture(() -> startServerImpl(attr));
	}

	private StartServerResponse startServerImpl(LaunchParameters attr) {
		if( attr == null || isEmpty(attr.getMode()) || isEmpty(attr.getParams().getId())) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
					"Invalid Parameter");
			return (new StartServerResponse(StatusConverter.convert(is), null));
		}

		String id = attr.getParams().getId();
		IServer server = managementModel.getServerModel().getServer(id);
		if( server == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server " + id + " does not exist");
			return (new StartServerResponse(StatusConverter.convert(is), null));
		}

		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred: Server " + id + " has no delegate.");
			return (new StartServerResponse(StatusConverter.convert(is), null));
		}
		
		try {
			StartServerResponse ret = del.start(attr.getMode());
			return ret;
		} catch( Exception e ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred.", e);
			return (new StartServerResponse(StatusConverter.convert(is), null));
		}
	}
	
	@Override
	public CompletableFuture<Status> stopServerAsync(StopServerAttributes attr) {
		return createCompletableFuture(() -> stopServerImpl(attr));
	}

	private Status stopServerImpl(StopServerAttributes attr) {
		if( attr == null || isEmpty(attr.getId())) {
			return invalidParameterStatus();
		}

		IServer server = managementModel.getServerModel().getServer(attr.getId());
		if( server == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server " + attr.getId() + " does not exist");
			return (StatusConverter.convert(is));
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred: Server " + attr.getId() + " has no delegate.");
			return (StatusConverter.convert(is));
		}
		
		if(del.getServerRunState() == IServerDelegate.STATE_STOPPED && !attr.isForce()) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
					"The server is already marked as stopped. If you wish to force a stop request, please set the force flag to true.");
			return (StatusConverter.convert(is));
		}
		
		try {
			IStatus ret = del.stop(attr.isForce());
			return (StatusConverter.convert(ret));
		} catch( Exception e ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred.", e);
			return (StatusConverter.convert(is));
		}

	}

	@Override
	public CompletableFuture<CommandLineDetails> getLaunchCommand(LaunchParameters req) {
		return createCompletableFuture(() -> getLaunchCommandSync(req));
	}

	private CommandLineDetails getLaunchCommandSync(LaunchParameters req) {
		if( req == null || isEmpty(req.getMode()) || isEmpty(req.getParams().getId())) {
			return null;
		}
		String id = req.getParams().getId();
		IServer server = managementModel.getServerModel().getServer(id);
		if( server == null ) {
			return null;
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			return null;
		}
		try {
			CommandLineDetails det = del.getStartLaunchCommand(req.getMode(), req.getParams());
			return det;
		} catch( Exception e ) {
			return null;
		}
	}
	
	@Override
	public CompletableFuture<ServerState> getServerState(ServerHandle handle) {
		return createCompletableFuture(() -> getServerStateSync(handle));
	}

	public ServerState getServerStateSync(ServerHandle handle) {
		IServer is = managementModel.getServerModel().getServer(handle.getId());
		return is.getDelegate().getServerState();
	}
	
	@Override
	public CompletableFuture<Status> serverStartingByClient(ServerStartingAttributes attr) {
		return createCompletableFuture(() -> serverStartingByClientSync(attr));
	}

	private Status serverStartingByClientSync(ServerStartingAttributes attr) {
		if( attr == null || attr.getRequest() == null || isEmpty(attr.getRequest().getMode())
				|| isEmpty(attr.getRequest().getParams().getId())) {
			return invalidParameterStatus();
		}
		String id = attr.getRequest().getParams().getId();
		IServer server = managementModel.getServerModel().getServer(id);
		if( server == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server " + id + " does not exist");
			return StatusConverter.convert(is);
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server error: Server " + id + " does not have a delegate.");
			return StatusConverter.convert(is);
		}
		try {
			IStatus s = del.clientSetServerStarting(attr);
			return StatusConverter.convert(s);
		} catch( Exception e ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred.", e);
			return StatusConverter.convert(is);
		}
	}
	
	@Override
	public CompletableFuture<Status> serverStartedByClient(LaunchParameters attr) {
		return createCompletableFuture(() -> serverStartedByClientSync(attr));
	}

	private Status serverStartedByClientSync(LaunchParameters attr) {
		if( attr == null || attr.getParams() == null || isEmpty(attr.getParams().getId())) {
			return invalidParameterStatus();
		}

		String id = attr.getParams().getId();
		IServer server = managementModel.getServerModel().getServer(id);
		if( server == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server " + id + " does not exist");
			return StatusConverter.convert(is);
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server error: Server " + id + " does not have a delegate.");
			return StatusConverter.convert(is);
		}

		try {
			IStatus s = del.clientSetServerStarted(attr);
			return StatusConverter.convert(s);
		} catch( Exception e ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred.", e);
			return StatusConverter.convert(is);
		}
	}

	@Override
	public CompletableFuture<ServerCapabilitiesResponse> registerClientCapabilities(ClientCapabilitiesRequest request) {
		RSPClient rspc = ClientThreadLocal.getActiveClient();
		IStatus s = managementModel.getCapabilityManagement().registerClientCapabilities(rspc, request);
		Status st = StatusConverter.convert(s);
		Map<String,String> resp2 = managementModel.getCapabilityManagement().getServerCapabilities();
		ServerCapabilitiesResponse resp = new ServerCapabilitiesResponse(st, resp2);
		return CompletableFuture.completedFuture(resp);
	}

	/*
	 * Utility methods below
	 */	
	private Status booleanToStatus(boolean b, String message) {
		IStatus s = null;
		if( b ) {
			s = org.jboss.tools.rsp.eclipse.core.runtime.Status.OK_STATUS;
		} else {
			s = new org.jboss.tools.rsp.eclipse.core.runtime.Status(
					IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, message);
		}
		return StatusConverter.convert(s);
	}

	private boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	private Status invalidParameterStatus() {
		IStatus s = new org.jboss.tools.rsp.eclipse.core.runtime.Status(
				IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Parameter is invalid. It may be null, missing required fields, or unacceptable values.");
		return StatusConverter.convert(s);
	}

	@Override
	public CompletableFuture<List<DeployableState>> getDeployables(ServerHandle handle) {
		return createCompletableFuture(() -> getDeployablesSync(handle));
	}

	public List<DeployableState> getDeployablesSync(ServerHandle handle) {
		IServer server = managementModel.getServerModel().getServer(handle.getId());
		 return managementModel.getServerModel().getDeployables(server);
	}
	
	public CompletableFuture<Status> addDeployable(ModifyDeployableRequest request) {
		return createCompletableFuture(() -> addDeployableSync(request.getServer(), request.getDeployable()));
	}

	public Status addDeployableSync(ServerHandle handle, DeployableReference reference) {
		IServer server = managementModel.getServerModel().getServer(handle.getId());
		IStatus stat = managementModel.getServerModel().addDeployable(server, reference);
		return StatusConverter.convert(stat);
	}
	
	public CompletableFuture<Status> removeDeployable(ModifyDeployableRequest request) {
		return createCompletableFuture(() -> removeDeployableSync(request.getServer(), request.getDeployable()));
	}

	public Status removeDeployableSync(ServerHandle handle, DeployableReference reference) {
		IServer server = managementModel.getServerModel().getServer(handle.getId());
		IStatus stat = managementModel.getServerModel().removeDeployable(server, reference);
		return StatusConverter.convert(stat);
	}

	@Override
	public CompletableFuture<Status> publish(PublishServerRequest request) {
		return createCompletableFuture(() -> publishSync(request));
	}

	private Status publishSync(PublishServerRequest request) {
		try {
			IServer server = managementModel.getServerModel().getServer(request.getServer().getId());
			IStatus stat = managementModel.getServerModel().publish(server, request.getKind());
			return StatusConverter.convert(stat);
		} catch(CoreException ce) {
			return StatusConverter.convert(ce.getStatus());
		}
	}

	private static <T> CompletableFuture<T> createCompletableFuture(Supplier<T> supplier) {
		final RSPClient rspc = ClientThreadLocal.getActiveClient();
		CompletableFuture<T> completableFuture = new CompletableFuture<>();
		CompletableFuture.runAsync(() -> {
			ClientThreadLocal.setActiveClient(rspc);
			completableFuture.complete(supplier.get());
			ClientThreadLocal.setActiveClient(null);
		});
		return completableFuture;
	}
}
