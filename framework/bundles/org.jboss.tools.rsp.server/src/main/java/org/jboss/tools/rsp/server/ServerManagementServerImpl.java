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

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.SocketLauncher;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerCapabilitiesResponse;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.launching.utils.StatusConverter;
import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.rsp.server.model.RemoteEventManager;
import org.jboss.tools.rsp.server.model.ServerManagementModel;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class ServerManagementServerImpl implements RSPServer {
	
	private final List<RSPClient> clients = new CopyOnWriteArrayList<>();
	private final List<SocketLauncher<RSPClient>> launchers = new CopyOnWriteArrayList<>();
	
	private final ServerManagementModel model;
	private final RemoteEventManager remoteEventManager;
	private ServerManagementServerLauncher launcher;
	
	public ServerManagementServerImpl(ServerManagementServerLauncher launcher) {
		this.launcher = launcher;
		this.model = new ServerManagementModel();
		this.remoteEventManager = new RemoteEventManager(this);
	}
	
	public List<RSPClient> getClients() {
		return new ArrayList<RSPClient>(clients);
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
		this.model.clientAdded(launcher.getRemoteProxy());
	}
	
	protected void removeClient(SocketLauncher<RSPClient> launcher) {
		this.launchers.remove(launcher);
		this.model.removeClient(launcher.getRemoteProxy());
		this.clients.remove(launcher.getRemoteProxy());
	}
	
	public List<SocketLauncher<RSPClient>> getActiveLaunchers() {
		return new ArrayList<SocketLauncher<RSPClient>>(launchers);
	}
	
	public IServerManagementModel getModel() {
		return model;
	}

	/**
	 * Return existing messages.
	 */
	@Override
	public CompletableFuture<List<DiscoveryPath>> getDiscoveryPaths() {
		return CompletableFuture.completedFuture(model.getDiscoveryPathModel().getPaths());
	}

	private boolean isEmptyDiscoveryPath(DiscoveryPath path) {
		if( path == null || isEmpty(path.getFilepath())) {
			return true;
		}
		return false;
	}

	/**
	 * Add a path to our list of discovery paths
	 */
	@Override
	public CompletableFuture<Status> addDiscoveryPath(DiscoveryPath path) {
		return teeFuture(() -> addDiscoveryPathSync(path));
	}
	
	private Status addDiscoveryPathSync(DiscoveryPath path) {
		if( isEmptyDiscoveryPath(path)) 
			return invalidParameterStatus();
		String fp = path.getFilepath();
		IPath ipath = new Path(fp);
		if( !ipath.isAbsolute()) {
			return invalidParameterStatus();
		}
		boolean ret = model.getDiscoveryPathModel().addPath(path);
		return booleanToStatus(ret, "Discovery path not added: " + path.getFilepath());
	}


	@Override
	public CompletableFuture<Status> removeDiscoveryPath(DiscoveryPath path) {
		return teeFuture(() -> removeDiscoveryPathSync(path));
	}
	
	public Status removeDiscoveryPathSync(DiscoveryPath path) {
		if( isEmptyDiscoveryPath(path)) 
			return invalidParameterStatus();
		String fp = path.getFilepath();
		IPath ipath = new Path(fp);
		if( !ipath.isAbsolute()) {
			return invalidParameterStatus();
		}
		boolean ret = model.getDiscoveryPathModel().removePath(path);
		return booleanToStatus(ret, "Discovery path not removed: " + path.getFilepath());
	}

	
	@Override
	public CompletableFuture<List<ServerBean>> findServerBeans(DiscoveryPath path) {
		return teeFuture(() -> findServerBeansSync(path));
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

		ServerBeanLoader loader = new ServerBeanLoader(new File(path.getFilepath()));
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
		return teeFuture(() -> getServerHandlesSync());
	}
	
	private List<ServerHandle> getServerHandlesSync() {
		ServerHandle[] all = model.getServerModel().getServerHandles();
		return Arrays.asList(all);
	}

	@Override
	public CompletableFuture<Status> deleteServer(ServerHandle handle) {
		return teeFuture(() -> deleteServerSync(handle));
	}
	
	private Status deleteServerSync(ServerHandle handle) {
		if( handle == null || isEmpty(handle.getId())) {
			return invalidParameterStatus();
		}
		
		boolean b = model.getServerModel().removeServer(handle.getId());
		return booleanToStatus(b, "Server not removed: " + handle.getId());
	}

	@Override
	public CompletableFuture<Attributes> getRequiredAttributes(ServerType type) {
		return teeFuture(() -> getRequiredAttributesSync(type));
	}
	
	private Attributes getRequiredAttributesSync(ServerType type) {
		if( type == null || isEmpty(type.getId())) {
			return null;
		}
		Attributes rspa = model.getServerModel().getRequiredAttributes(type.getId());
		return rspa;
	}

	@Override
	public CompletableFuture<Attributes> getOptionalAttributes(ServerType type) {
		return teeFuture(() -> getOptionalAttributesSync(type));
	}
	private Attributes getOptionalAttributesSync(ServerType type) {
		if( type == null || isEmpty(type.getId())) {
			return null;
		}
		return model.getServerModel().getOptionalAttributes(type.getId());
	}
	
	@Override
	public CompletableFuture<List<ServerLaunchMode>> getLaunchModes(ServerType type) {
		return teeFuture(() -> getLaunchModesSync(type));
	}
	private List<ServerLaunchMode> getLaunchModesSync(ServerType type) {
		if( type == null || isEmpty(type.getId()) ) {
			return null;
		}
		List<ServerLaunchMode> l = model.getServerModel()
				.getLaunchModes(type.getId());
		return l;
	}
	
	@Override
	public CompletableFuture<Attributes> getRequiredLaunchAttributes(LaunchAttributesRequest req) {
		return teeFuture(() -> getRequiredLaunchAttributesSync(req));
	}
	private Attributes getRequiredLaunchAttributesSync(LaunchAttributesRequest req) {
		if( req == null || isEmpty(req.getServerTypeId()) || isEmpty(req.getMode())) {
			return null;
		}
		Attributes rspa = model.getServerModel().getRequiredLaunchAttributes(req.getServerTypeId());
		return rspa;
	}

	@Override
	public CompletableFuture<Attributes> getOptionalLaunchAttributes(LaunchAttributesRequest req) {
		return teeFuture(() -> getOptionalLaunchAttributesSync(req));
	}
	private Attributes getOptionalLaunchAttributesSync(LaunchAttributesRequest req) {
		if( req == null || isEmpty(req.getServerTypeId()) || isEmpty(req.getMode())) {
			return null;
		}
		Attributes rspa = model.getServerModel().getOptionalLaunchAttributes(req.getServerTypeId());
		return rspa;
	}

	
	@Override
	public CompletableFuture<CreateServerResponse> createServer(ServerAttributes attr) {
		return teeFuture(() -> createServerSync(attr));
	}
	private CreateServerResponse createServerSync(ServerAttributes attr) {
		if( attr == null || isEmpty(attr.getId()) || isEmpty(attr.getServerType())) {
			Status s = invalidParameterStatus();
			return new CreateServerResponse(s, null);
		}
		
		String serverType = attr.getServerType();
		String id = attr.getId();
		Map<String, Object> attributes = attr.getAttributes();
		
		CreateServerResponse ret = model.getServerModel().createServer(serverType, id, attributes);
		return ret;
	}

	@Override
	public CompletableFuture<List<ServerType>> getServerTypes() {
		return teeFuture(() -> getServerTypesSync());
	}
	private List<ServerType> getServerTypesSync() {
		ServerType[] types = model.getServerModel().getAccessibleServerTypes();
		return Arrays.asList(types);
	}

	@Override
	public CompletableFuture<StartServerResponse> startServerAsync(LaunchParameters attr) {
		return teeFuture(() -> startServerImpl(attr));
	}
	private StartServerResponse startServerImpl(LaunchParameters attr) {
		if( attr == null || isEmpty(attr.getMode()) || isEmpty(attr.getParams().getId())) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
					"Invalid Parameter");
			return (new StartServerResponse(StatusConverter.convert(is), null));
		}

		String id = attr.getParams().getId();
		IServer server = model.getServerModel().getServer(id);
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
		return teeFuture(() -> stopServerImpl(attr));
	}
	private Status stopServerImpl(StopServerAttributes attr) {
		if( attr == null || isEmpty(attr.getId())) {
			return invalidParameterStatus();
		}

		IServer server = model.getServerModel().getServer(attr.getId());
		if( server == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server " + attr.getId() + " does not exist");
			return (StatusConverter.convert(is));
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred: Server " + attr.getId() + " has no delegate.");
			return (StatusConverter.convert(is));
		}
		
		if(del.getServerState() == IServerDelegate.STATE_STOPPED && !attr.isForce()) {
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
		return teeFuture(() -> getLaunchCommandSync(req));
	}
	private CommandLineDetails getLaunchCommandSync(LaunchParameters req) {
		if( req == null || isEmpty(req.getMode()) || isEmpty(req.getParams().getId())) {
			return null;
		}
		String id = req.getParams().getId();
		IServer server = model.getServerModel().getServer(id);
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
	public CompletableFuture<Status> serverStartingByClient(ServerStartingAttributes attr) {
		return teeFuture(() -> serverStartingByClientSync(attr));
	}

	private Status serverStartingByClientSync(ServerStartingAttributes attr) {
		if( attr == null || attr.getRequest() == null || isEmpty(attr.getRequest().getMode())
				|| isEmpty(attr.getRequest().getParams().getId())) {
			return invalidParameterStatus();
		}
		String id = attr.getRequest().getParams().getId();
		IServer server = model.getServerModel().getServer(id);
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
		return teeFuture(() -> serverStartedByClientSync(attr));
	}
	private Status serverStartedByClientSync(LaunchParameters attr) {
		if( attr == null || attr.getParams() == null || isEmpty(attr.getParams().getId())) {
			return invalidParameterStatus();
		}

		String id = attr.getParams().getId();
		IServer server = model.getServerModel().getServer(id);
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
		IStatus s = model.getCapabilityManagement().registerClientCapabilities(rspc, request);
		Status st = StatusConverter.convert(s);
		Map<String,String> resp2 = model.getCapabilityManagement().getServerCapabilities();
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

	private static interface IMethodProvider<T> {
		public T method();
	}
	
	private static class RSPCompletableFuture<T> {
		public CompletableFuture<T> method(IMethodProvider<T> provider) {
			final RSPClient rspc = ClientThreadLocal.getActiveClient();
			CompletableFuture<T> completableFuture = new CompletableFuture<>();
			CompletableFuture.runAsync(() -> {
				ClientThreadLocal.setActiveClient(rspc);
				completableFuture.complete(provider.method());
				ClientThreadLocal.setActiveClient(null);
			});
			return completableFuture;
		}
	}

	<T> CompletableFuture<T> teeFuture(IMethodProvider<T> provider) {
		return new RSPCompletableFuture<T>().method(provider);
	}
}
