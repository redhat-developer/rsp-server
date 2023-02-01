/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.SocketLauncher;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.CreateServerWorkflowRequest;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.GetServerJsonResponse;
import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.api.dao.JobProgress;
import org.jboss.tools.rsp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ListDeployablesResponse;
import org.jboss.tools.rsp.api.dao.ListDeploymentOptionsResponse;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerCapabilitiesResponse;
import org.jboss.tools.rsp.api.dao.ServerDeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.api.dao.UpdateServerRequest;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeRunner;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.core.internal.ServerStringConstants;
import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.rsp.server.model.RemoteEventManager;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;
import org.jboss.tools.rsp.server.spi.jobs.IJob;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.spi.util.AlphanumComparator;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class ServerManagementServerImpl implements RSPServer {
	
	private final List<RSPClient> clients = new CopyOnWriteArrayList<>();
	private final List<SocketLauncher<RSPClient>> launchers = new CopyOnWriteArrayList<>();
	
	private final IServerManagementModel managementModel;
	private final RemoteEventManager remoteEventManager;
	private ServerManagementServerLauncher launcher;
	
	public ServerManagementServerImpl(ServerManagementServerLauncher launcher, 
			IServerManagementModel managementModel) {
		this.launcher = launcher;
		this.managementModel = managementModel;
		this.remoteEventManager = createRemoteEventManager();
	}
	
	protected RemoteEventManager createRemoteEventManager() {
		return new RemoteEventManager(this);
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
		this.managementModel.clientAdded(launcher.getRemoteProxy());
		this.remoteEventManager.initClientWithServerStates(launcher.getRemoteProxy());
	}
	
	protected void removeClient(SocketLauncher<RSPClient> launcher) {
		this.launchers.remove(launcher);
		this.managementModel.clientRemoved(launcher.getRemoteProxy());
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
		final RSPClient rspc = ClientThreadLocal.getActiveClient();
		new Thread("Shutdown") {
			@Override
			public void run() {
				ClientThreadLocal.setActiveClient(rspc);
				shutdownSync();
				ClientThreadLocal.setActiveClient(null);
			}
		}.start();
	}

	@Override
	public void disconnectClient() {
		final RSPClient rspc = ClientThreadLocal.getActiveClient();
		new Thread("Shutdown") {
			@Override
			public void run() {
				ClientThreadLocal.setActiveClient(rspc);
				try {
					Thread.sleep(200);
				} catch(InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				launcher.closeConnection(rspc);
				ClientThreadLocal.setActiveClient(null);
			}
		}.start();
	}

	private void shutdownSync() {
		managementModel.dispose();
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
		Status validate = verifyServerAndDelegate(handle);
		if( validate != null && !validate.isOK()) 
			return validate;
		
		IServer server = managementModel.getServerModel().getServer(handle.getId());
		boolean b = managementModel.getServerModel().removeServer(server);
		if( server.getDelegate().getServerRunState() != ServerManagementAPIConstants.STATE_STOPPED) {
			new Thread("Stopping server: " + server.getName()) {
				public void run() {
					IServerDelegate del = server.getDelegate();
					del.stop(false);
				}
			}.start();
		}
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
		
		return managementModel.getServerModel().createServer(serverType, id, attributes);
	}

	
	@Override
	public CompletableFuture<GetServerJsonResponse> getServerAsJson(ServerHandle sh) {
		return createCompletableFuture(() -> getServerAsJsonSync(sh));
	}

	private GetServerJsonResponse getServerAsJsonSync(ServerHandle sh) {
		Status valid = verifyServerAndDelegate(sh);
		if( valid != null && !valid.isOK()) {
			GetServerJsonResponse ret = new GetServerJsonResponse();
			ret.setStatus(valid);
			return ret;
		}
		IServer server = managementModel.getServerModel().getServer(sh.getId());
		GetServerJsonResponse ret = new GetServerJsonResponse();
		ret.setServerHandle(sh);
		try {
			String json = server.asJson(new NullProgressMonitor());
			ret.setServerJson(json);
			Status stat = StatusConverter.convert(org.jboss.tools.rsp.eclipse.core.runtime.Status.OK_STATUS);
			ret.setStatus(stat);
		} catch(CoreException ce) {
			ret.setStatus(StatusConverter.convert(ce.getStatus()));
		}
		return ret;
	}
	
	@Override
	public CompletableFuture<UpdateServerResponse> updateServer(UpdateServerRequest req) {
		return createCompletableFuture(() -> updateServerSync(req));
	}

	private UpdateServerResponse updateServerSync(UpdateServerRequest req) {
		UpdateServerResponse resp = managementModel.getServerModel().updateServer(req);
		if (req == null) {
			return resp;
		}
		resp.setServerJson(getServerAsJsonSync(req.getHandle()));
		return resp;
	}

	@Override
	public CompletableFuture<List<ServerType>> getServerTypes() {
		return createCompletableFuture(() -> getServerTypesSync());
	}

	private List<ServerType> getServerTypesSync() {
		ServerType[] types = managementModel.getServerModel().getAccessibleServerTypes();
		Comparator<ServerType> c = (h1,h2) -> new AlphanumComparator().compare(h1.getVisibleName(), h2.getVisibleName()); 
		return Arrays.asList(types).stream().sorted(c).collect(Collectors.toList());
	}

	@Override
	public CompletableFuture<StartServerResponse> startServerAsync(LaunchParameters attr) {
		return createCompletableFuture(() -> startServerImpl(attr));
	}

	private StartServerResponse startServerImpl(LaunchParameters attr) {
		if( attr == null || isEmpty(attr.getMode()) || isEmpty(attr.getParams().getId())) {
			Status is = errorStatus("Invalid Parameter", null);
			return (new StartServerResponse(is, null));
		}

		Status valid = verifyServerAndDelegate(attr.getParams().getId());
		if( valid != null && !valid.isOK()) {
			return (new StartServerResponse(valid, null));
		}
		String id = attr.getParams().getId();
		IServer server = managementModel.getServerModel().getServer(id);
		IServerDelegate del = server.getDelegate();
		try {
			return del.start(attr.getMode());
		} catch( Exception e ) {
			Status is = errorStatus(ServerStringConstants.UNEXPECTED_ERROR, e);
			return new StartServerResponse(is, null);
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
			String msg = NLS.bind(ServerStringConstants.SERVER_DNE, attr.getId());
			return errorStatus(msg);
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			return errorStatus("An unexpected error occurred: Server " + attr.getId() + " has no delegate.");
		}
		
		if(del.getServerRunState() == IServerDelegate.STATE_STOPPED && !attr.isForce()) {
			return errorStatus(
					"The server is already marked as stopped. If you wish to force a stop request, please set the force flag to true.");
		}
		
		try {
			return StatusConverter.convert(del.stop(attr.isForce()));
		} catch( Exception e ) {
			return errorStatus(ServerStringConstants.UNEXPECTED_ERROR, e);
		}

	}

	@Override
	public CompletableFuture<CommandLineDetails> getLaunchCommand(LaunchParameters req) {
		return createCompletableFuture(() -> getLaunchCommandSync(req));
	}

	private CommandLineDetails getLaunchCommandSync(LaunchParameters req) {
		boolean empty = req == null || isEmpty(req.getMode()) || isEmpty(req.getParams().getId()); 
		if( !empty ) {
			String id = req.getParams().getId();
			IServer server = managementModel.getServerModel().getServer(id);
			if( server != null ) {
				IServerDelegate del = server.getDelegate();
				if( del != null ) {
					try {
						return del.getStartLaunchCommand(req.getMode(), req.getParams());
					} catch( Exception e ) {
						// Ignore
					}
				}
			}
		}
		return null;
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
			String msg = NLS.bind(ServerStringConstants.SERVER_DNE, id);
			return errorStatus(msg);
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			return errorStatus(NLS.bind(ServerStringConstants.UNEXPECTED_ERROR_DELEGATE, id));
		}
		try {
			return StatusConverter.convert(del.clientSetServerStarting(attr));
		} catch( Exception e ) {
			return errorStatus(ServerStringConstants.UNEXPECTED_ERROR, e);
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
			String msg = NLS.bind(ServerStringConstants.SERVER_DNE, id);
			return errorStatus(msg);
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			return errorStatus("Server error: Server " + id + " does not have a delegate.");
		}

		try {
			return StatusConverter.convert(del.clientSetServerStarted(attr));
		} catch( Exception e ) {
			return errorStatus(ServerStringConstants.UNEXPECTED_ERROR, e);
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
	public CompletableFuture<ListDeployablesResponse> getDeployables(ServerHandle handle) {
		return createCompletableFuture(() -> getDeployablesSync(handle));
	}

	// This API has no way to return an error. Should be changed
	public ListDeployablesResponse getDeployablesSync(ServerHandle handle) {
		String handleIdOrNull = handle == null ? null : handle.getId();
		String handleIdOrNullString = handleIdOrNull == null ? "null" : handleIdOrNull;
		
		if( handleIdOrNull == null ) {
			ListDeployablesResponse resp = new ListDeployablesResponse(
					null, errorStatus("Unable to locate server with null id."));
			return resp;
		}
		IServer server = managementModel.getServerModel().getServer(handle.getId());
		if( server == null ) {
			ListDeployablesResponse resp = new ListDeployablesResponse(
					null, errorStatus(NLS.bind("Unable to locate server {0}", 
							handleIdOrNullString)));
			return resp;
		}
		ListDeployablesResponse resp = new ListDeployablesResponse();
		resp.setStatus(StatusConverter.convert(org.jboss.tools.rsp.eclipse.core.runtime.Status.OK_STATUS));
		resp.setStates(managementModel.getServerModel().getDeployables(server));
		return resp;
	}
	
	public CompletableFuture<ListDeploymentOptionsResponse> listDeploymentOptions(ServerHandle handle) {
		return createCompletableFuture(() -> listDeploymentOptionsSync(handle));
	}
	
	// This API has no way to return an error. Should be changed
	public ListDeploymentOptionsResponse listDeploymentOptionsSync(ServerHandle handle) {
		ListDeploymentOptionsResponse resp = new ListDeploymentOptionsResponse();
		if( handle == null ) {
			resp.setStatus(errorStatus("Unable to locate server with null id"));
			resp.setAttributes(new CreateServerAttributesUtility().toPojo());
			return resp;
		}
		IServer server = managementModel.getServerModel().getServer(handle.getId());
		if( server == null || server.getDelegate() == null) {
			resp.setStatus(errorStatus(NLS.bind("Server {0} not found.", handle.getId())));
			resp.setAttributes(new CreateServerAttributesUtility().toPojo());
			return resp;
		}
		
		resp.setStatus(StatusConverter.convert(org.jboss.tools.rsp.eclipse.core.runtime.Status.OK_STATUS));
		resp.setAttributes(server.getDelegate().listDeploymentOptions());
		return resp;
	}
	
	public CompletableFuture<Status> addDeployable(ServerDeployableReference request) {
		return createCompletableFuture(() -> addDeployableSync(request));
	}

	public Status addDeployableSync(ServerDeployableReference req) {
		if( req == null || req.getServer() == null || req.getDeployableReference() == null) {
			return errorStatus("Invalid request; Expected fields not present.", null);
		}
		String serverId = req.getServer().getId();
		IServer server = managementModel.getServerModel().getServer(serverId);
		if( server == null ) {
			return errorStatus( "Server " + serverId + " not found.");
		}
		IStatus stat = managementModel.getServerModel().addDeployable(server, req.getDeployableReference());
		return StatusConverter.convert(stat);
	}
	
	public CompletableFuture<Status> removeDeployable(ServerDeployableReference request) {
		return createCompletableFuture(() -> removeDeployableSync(request));
	}

	public Status removeDeployableSync(ServerDeployableReference reference) {
		if( reference == null || reference.getServer() == null || reference.getDeployableReference() == null) {
			return errorStatus("Invalid request; Expected fields not present.", null);
		}
		String serverId = reference.getServer().getId();
		IServer server = managementModel.getServerModel().getServer(serverId);
		if( server == null ) {
			return errorStatus( "Server " + serverId + " not found.");
		}

		IStatus stat = managementModel.getServerModel().removeDeployable(server, reference.getDeployableReference());
		return StatusConverter.convert(stat);
	}

	@Override
	public CompletableFuture<Status> publish(PublishServerRequest request) {
		return createCompletableFuture(() -> publishSync(request));
	}

	private Status checkPublishRequestError(PublishServerRequest request) {
		if( request == null || request.getServer() == null 
				|| request.getServer().getId() == null) {
			return errorStatus("Invalid request; Expected fields not present.", null);
		}
		IServer server = managementModel.getServerModel().getServer(request.getServer().getId());
		if( server == null ) {
			return errorStatus("Server not found: " + request.getServer().getId(), null);
		}
		return null;
	}
	private Status publishSync(PublishServerRequest request) {
		Status stat = checkPublishRequestError(request);
		if( stat != null )
			return stat;
		try {
			IServer server = managementModel.getServerModel().getServer(request.getServer().getId());
			IStatus stat2 = managementModel.getServerModel().publish(server, request.getKind());
			return StatusConverter.convert(stat2);
		} catch(CoreException ce) {
			return StatusConverter.convert(ce.getStatus());
		}
	}

	@Override
	public CompletableFuture<Status> publishAsync(PublishServerRequest request) {
		return createCompletableFuture(() -> publishAsyncInternal(request));
	}

	private Status publishAsyncInternal(PublishServerRequest request) {
		Status stat = checkPublishRequestError(request);
		if( stat != null )
			return stat;
		try {
			IServer server = managementModel.getServerModel().getServer(request.getServer().getId());
			IStatus stat2 = managementModel.getServerModel().publishAsync(server, request.getKind());
			return StatusConverter.convert(stat2);
		} catch(CoreException ce) {
			return StatusConverter.convert(ce.getStatus());
		}
	}

	@Override
	public CompletableFuture<ListDownloadRuntimeResponse> listDownloadableRuntimes() {
		return createCompletableFuture(() -> listDownloadableRuntimesInternal());
	}

	private ListDownloadRuntimeResponse listDownloadableRuntimesInternal() {
		Map<String, DownloadRuntime> map = managementModel.getDownloadRuntimeModel().getOrLoadDownloadRuntimes(new NullProgressMonitor());
		AlphanumComparator comp = new AlphanumComparator();
		Comparator<DownloadRuntimeDescription> alphanumComp = (drd1,drd2) -> comp.compare(drd1.getName(), drd2.getName());
		List<DownloadRuntimeDescription> list = map.values().stream()
				.sorted(alphanumComp)
				.map(dlrt -> dlrt.toDao())
				.collect(Collectors.toList());
		ListDownloadRuntimeResponse resp = new ListDownloadRuntimeResponse();
		resp.setRuntimes(list);
		return resp;
	}

	@Override
	public CompletableFuture<WorkflowResponse> downloadRuntime(DownloadSingleRuntimeRequest req) {
		return createCompletableFuture(() -> downloadRuntimeInternal(req));
	}

	private WorkflowResponse downloadRuntimeInternal(DownloadSingleRuntimeRequest req) {
		if (req == null) {
			WorkflowResponse resp = errorWorkflowResponse(errorStatus("Invalid Request: Request cannot be null."));
			resp.setItems(new ArrayList<>());
			return resp;
		}
		String id = req.getDownloadRuntimeId();
		IDownloadRuntimesProvider provider = managementModel.getDownloadRuntimeModel().findProviderForRuntime(id);
		if( provider != null ) {
			DownloadRuntime dlrt = managementModel.getDownloadRuntimeModel().findDownloadRuntime(id, new NullProgressMonitor());
			IDownloadRuntimeRunner executor = provider.getDownloadRunner(dlrt);
			if( executor != null ) {
				WorkflowResponse response = executor.execute(req);
				return response;
			}
		}
		WorkflowResponse error = new WorkflowResponse();
		Status s = errorStatus("Unable to find an executor for the given download runtime", null);
		error.setStatus(s);
		error.setItems(new ArrayList<>());
		return error;
	}
	

	@Override
	public CompletableFuture<WorkflowResponse> createServerWorkflow(CreateServerWorkflowRequest req) {
		return createCompletableFuture(() -> createServerWorkflowInternal(req));
	}

	private WorkflowResponse createServerWorkflowInternal(CreateServerWorkflowRequest req) {
		if (req == null) {
			WorkflowResponse resp = errorWorkflowResponse(errorStatus("Invalid Request: Request cannot be null."));
			resp.setItems(new ArrayList<>());
			return resp;
		}
		String serverTypeId = req.getServerTypeId();
		if (serverTypeId == null) {
			WorkflowResponse resp = errorWorkflowResponse(errorStatus("Invalid Request: serverTypeId cannot be null."));
			resp.setItems(new ArrayList<>());
			return resp;
		}
		IServerType type = managementModel.getServerModel().getIServerType(serverTypeId);
		try {
			return type.createServerWorkflow(this, req);
		} catch(RuntimeException re) {
			Status status = errorStatus("Error executing actions: " + re.getMessage(), re);
			return errorWorkflowResponse(status, req.getRequestId());
		}
	}

	@Override
	public CompletableFuture<List<JobProgress>> getJobs() {
		return createCompletableFuture(() -> getJobsSync());
	}
	
	protected List<JobProgress> getJobsSync() {
		List<IJob> jobs = managementModel.getJobManager().getJobs();
		List<JobProgress> ret = new ArrayList<>();
		JobProgress jp = null;
		for( IJob i : jobs ) {
			jp = new JobProgress(new JobHandle(i.getName(), i.getId()), i.getProgress());
			ret.add(jp);
		}
		return ret;
	}

	@Override
	public CompletableFuture<Status> cancelJob(JobHandle job) {
		return createCompletableFuture(() -> cancelJobSync(job));
	}
	
	protected Status cancelJobSync(JobHandle job) {
		if (job == null) {
			return errorStatus("Job handle cannot be null");
		}
		IStatus s =  managementModel.getJobManager().cancelJob(job);
		return StatusConverter.convert(s);
	}

	
	/*
	 * Server actions
	 * 
	 * (non-Javadoc)
	 * @see org.jboss.tools.rsp.api.RSPServer#listServerActions()
	 */
	@Override
	public CompletableFuture<ListServerActionResponse> listServerActions(ServerHandle handle) {
		return createCompletableFuture(() -> listServerActionsSync(handle));
	}
	private ListServerActionResponse listServerActionsSync(ServerHandle handle) {
		ListServerActionResponse resp = new ListServerActionResponse();
		Status s = verifyServerAndDelegate(handle);
		if( s != null && !s.isOK()) {
			resp.setStatus(s);
			return resp;
		}
		IServer server = managementModel.getServerModel().getServer(handle.getId());
		try {
			return server.getDelegate().listServerActions();
		} catch(RuntimeException re) {
			Status err = errorStatus("Error loading actions: " + re.getMessage(), re);
			resp.setStatus(err);
			return resp;
		}
	}

	@Override
	public CompletableFuture<WorkflowResponse> executeServerAction(ServerActionRequest req) {
		return createCompletableFuture(() -> executeServerActionSync(req));
	}
	
	private WorkflowResponse executeServerActionSync(ServerActionRequest req) {
		if (req == null) {
			return errorWorkflowResponse(errorStatus("Invalid Request: Request cannot be null."));
		}
		String serverId = req.getServerId();
		Status s = verifyServerAndDelegate(serverId);
		if( s != null && !s.isOK()) {
			return errorWorkflowResponse(s);
		}
		IServer server = managementModel.getServerModel().getServer(serverId);
		IServerDelegate del = server.getDelegate();
		try {
			return del.executeServerAction(req);
		} catch(RuntimeException re) {
			Status status = errorStatus("Error executing actions: " + re.getMessage(), re);
			return errorWorkflowResponse(status, req.getRequestId());
		}
	}
	
	private WorkflowResponse errorWorkflowResponse(Status s) {
		return errorWorkflowResponse(s, 0);
	}
	
	private WorkflowResponse errorWorkflowResponse(Status s, long requestId) {
		WorkflowResponse err = new WorkflowResponse();
		err.setRequestId(requestId);
		err.setStatus(s);
		return err;
	}

	private Status verifyServerAndDelegate(ServerHandle handle) {
		if( handle == null ) { 
			return errorStatus("Invalid Request: Request must include server handle.");
		}
		if( handle.getType() == null ) { 
			return errorStatus("Invalid Request: Request must include server type.");
		}
		if( handle.getType().getId() == null ) { 
			return errorStatus("Invalid Request: Request must include server type id.");
		}
		if( managementModel.getServerModel().getIServerType(
				handle.getType().getId()) == null ) {
			return errorStatus("Invalid Request: Server type not found.");
		}
		return verifyServerAndDelegate(handle.getId());
	}
	
	private Status verifyServerAndDelegate(String id) {
		if( id == null ) { 
			return errorStatus("Invalid Request: Request must include server id.");
		}
		IServer server = managementModel.getServerModel().getServer(id);
		if( server == null ) {
			return errorStatus(NLS.bind(ServerStringConstants.SERVER_DNE, id), null);
		}
		if( server.getDelegate() == null ) {
			return errorStatus(NLS.bind(ServerStringConstants.UNEXPECTED_ERROR_DELEGATE, id), null);
		}
		return null;
	}
	private Status errorStatus(String msg) {
		return errorStatus(msg, null);
	}
	private Status errorStatus(String msg, Throwable t) {
		IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, 
				ServerCoreActivator.BUNDLE_ID, 
				msg, t);
		return StatusConverter.convert(is);
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
