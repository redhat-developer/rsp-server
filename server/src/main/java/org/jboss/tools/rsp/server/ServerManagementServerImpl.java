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
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
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
		model = new ServerManagementModel();
		remoteEventManager = new RemoteEventManager(this);
	}
	
	public List<RSPClient> getClients() {
		return new ArrayList<RSPClient>(clients);
	}
	
	/**
	 * Connect the given chat client.
	 * Return a runnable which should be executed to disconnect the client.
	 */
	public Runnable addClient(SocketLauncher<RSPClient> launcher) {
		this.launchers.add(launcher);
		RSPClient client = launcher.getRemoteProxy();
		this.clients.add(client);
		return () -> this.removeClient(launcher);
	}

	private void removeClient(SocketLauncher<RSPClient> launcher) {
		this.launchers.add(launcher);
		this.clients.remove(launcher.getRemoteProxy());
		
	}
	
	public List<SocketLauncher<RSPClient>> getActiveLaunchers() {
		return new ArrayList<SocketLauncher<RSPClient>>(launchers);
	}
	
	public IServerManagementModel getModel() {
		return model;
	}
	
	/*
	 * Some methods for adding or removing VMs
	 */
	
//	/**
//	 * Get a list of VMs currently registered
//	 * @return
//	 */
//	@Override
//	public CompletableFuture<List<VMDescription>> getVMs() {
//		IVMInstall[] arr = VMInstallModel.getDefault().getVMs();
//		VMDescription[] vmd = new VMDescription[arr.length];
//		for( int i = 0; i < arr.length; i++ ) {
//			vmd[i] = getDescription(arr[i]);
//		}
//		return CompletableFuture.completedFuture(Arrays.asList(vmd));
//	}
//	
//	private VMDescription getDescription(IVMInstall vmi) {
//		String vers = vmi instanceof IVMInstall2 ? ((IVMInstall2)vmi).getJavaVersion() : null;
//		return new VMDescription(vmi.getId(), vmi.getInstallLocation().getAbsolutePath(), vers);
//	}
//	
//	@Override
//	public void addVM(VMDescription desc) {
//		
//		try {
//			IVMInstall vmi = StandardVMType.getDefault().createVMInstall(desc.getId());
//			vmi.setInstallLocation(new File(desc.getInstallLocation()));
//			VMInstallModel.getDefault().addVMInstall(vmi);
//		} catch(IllegalArgumentException arg) {
//			LaunchingCore.log(arg);
//		}
//	}
//
//	@Override
//	public void removeVM(VMHandle handle) {
//		VMInstallModel.getDefault().removeVMInstall(handle.getId());
//	}
//
//	
	
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
	
	private CompletableFuture<Status> invalidParameterError() {
		IStatus s = new org.jboss.tools.rsp.eclipse.core.runtime.Status(
				IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Parameter is invalid. It may be null, missing required fields, or unacceptable values.");
		return CompletableFuture.completedFuture(StatusConverter.convert(s));
	}
	
	/**
	 * Add a path to our list of discovery paths
	 */
	@Override
	public CompletableFuture<Status> addDiscoveryPath(DiscoveryPath path) {
		if( isEmptyDiscoveryPath(path)) 
			return invalidParameterError();
		String fp = path.getFilepath();
		IPath ipath = new Path(fp);
		if( !ipath.isAbsolute()) {
			return invalidParameterError();
		}

		boolean ret = model.getDiscoveryPathModel().addPath(path);
		return booleanToStatus(ret, "Discovery path not added: " + path.getFilepath());
	}

	@Override
	public CompletableFuture<Status> removeDiscoveryPath(DiscoveryPath path) {
		if( isEmptyDiscoveryPath(path)) 
			return invalidParameterError();
		String fp = path.getFilepath();
		IPath ipath = new Path(fp);
		if( !ipath.isAbsolute()) {
			return invalidParameterError();
		}

		boolean ret = model.getDiscoveryPathModel().removePath(path);
		return booleanToStatus(ret, "Discovery path not removed: " + path.getFilepath());
	}

	
	private CompletableFuture<Status> booleanToStatus(boolean b, String message) {
		IStatus s = null;
		if( b ) {
			s = org.jboss.tools.rsp.eclipse.core.runtime.Status.OK_STATUS;
		} else {
			s = new org.jboss.tools.rsp.eclipse.core.runtime.Status(
					IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, message);
		}
		return CompletableFuture.completedFuture(StatusConverter.convert(s));
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
	@Override
	public CompletableFuture<List<ServerBean>> findServerBeans(DiscoveryPath path) {
		List<ServerBean> ret = new ArrayList<>();
		if( path == null || isEmpty(path.getFilepath())) {
			return CompletableFuture.completedFuture(ret);
		}
		
		String fp = path.getFilepath();
		IPath ipath = new Path(fp);
		if( !ipath.isAbsolute()) {
			return CompletableFuture.completedFuture(ret);
		}

		ServerBeanLoader loader = new ServerBeanLoader(new File(path.getFilepath()));
		ServerBean bean = loader.getServerBean();
		if( bean != null )
			ret.add(bean);
		return CompletableFuture.completedFuture(ret);
	}

	@Override
	public void shutdown() {
		launcher.shutdown();
	}

	@Override
	public CompletableFuture<List<ServerHandle>> getServerHandles() {
		ServerHandle[] all = model.getServerModel().getServerHandles();
		return CompletableFuture.completedFuture(Arrays.asList(all));
	}

	@Override
	public CompletableFuture<Status> deleteServer(ServerHandle handle) {
		if( handle == null || isEmpty(handle.getId())) {
			return invalidParameterError();
		}
		
		boolean b = model.getServerModel().removeServer(handle.getId());
		return booleanToStatus(b, "Server not removed: " + handle.getId());
	}

	@Override
	public CompletableFuture<Attributes> getRequiredAttributes(ServerType type) {
		if( type == null || isEmpty(type.getId())) {
			return CompletableFuture.completedFuture(null);
		}
		Attributes rspa = model.getServerModel().getRequiredAttributes(type.getId());
		return CompletableFuture.completedFuture(rspa);
	}

	@Override
	public CompletableFuture<Attributes> getOptionalAttributes(ServerType type) {
		if( type == null || isEmpty(type.getId())) {
			return CompletableFuture.completedFuture(null);
		}
		Attributes rspa = model.getServerModel().getOptionalAttributes(type.getId());
		return CompletableFuture.completedFuture(rspa);
	}
	
	@Override
	public CompletableFuture<List<ServerLaunchMode>> getLaunchModes(ServerType type) {
		if( type == null || isEmpty(type.getId()) ) {
			return CompletableFuture.completedFuture(null);
		}
		
		List<ServerLaunchMode> l = model.getServerModel()
				.getLaunchModes(type.getId());
		return CompletableFuture.completedFuture(l);
	}
	
	@Override
	public CompletableFuture<Attributes> getRequiredLaunchAttributes(LaunchAttributesRequest req) {
		if( req == null || isEmpty(req.getServerTypeId()) || isEmpty(req.getMode())) {
			return CompletableFuture.completedFuture(null);
		}
		Attributes rspa = model.getServerModel().getRequiredLaunchAttributes(req.getServerTypeId());
		return CompletableFuture.completedFuture(rspa);
	}

	@Override
	public CompletableFuture<Attributes> getOptionalLaunchAttributes(LaunchAttributesRequest req) {
		if( req == null || isEmpty(req.getServerTypeId()) || isEmpty(req.getMode())) {
			return CompletableFuture.completedFuture(null);
		}
		Attributes rspa = model.getServerModel().getOptionalLaunchAttributes(req.getServerTypeId());
		return CompletableFuture.completedFuture(rspa);
	}

	@Override
	public CompletableFuture<Status> createServer(ServerAttributes attr) {
		if( attr == null || isEmpty(attr.getId()) || isEmpty(attr.getServerType())) {
			return invalidParameterError();
		}
		
		String serverType = attr.getServerType();
		String id = attr.getId();
		Map<String, Object> attributes = attr.getAttributes();
		
		IStatus ret = model.getServerModel().createServer(serverType, id, attributes);
		return CompletableFuture.completedFuture(StatusConverter.convert(ret));
	}

	@Override
	public CompletableFuture<List<ServerType>> getServerTypes() {
		ServerType[] types = model.getServerModel().getServerTypes();
		List<ServerType> asList = Arrays.asList(types);
		return CompletableFuture.completedFuture(asList);
	}

	@Override
	public CompletableFuture<StartServerResponse> startServerAsync(LaunchParameters attr) {
		if( attr == null || isEmpty(attr.getMode()) || isEmpty(attr.getParams().getId())) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
					"Invalid Parameter");
			return CompletableFuture.completedFuture(new StartServerResponse(StatusConverter.convert(is), null));
		}

		String id = attr.getParams().getId();
		IServer server = model.getServerModel().getServer(id);
		if( server == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server " + id + " does not exist");
			return CompletableFuture.completedFuture(new StartServerResponse(StatusConverter.convert(is), null));
		}

		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred: Server " + id + " has no delegate.");
			return CompletableFuture.completedFuture(new StartServerResponse(StatusConverter.convert(is), null));
		}
		
		try {
			StartServerResponse ret = del.start(attr.getMode());
			return CompletableFuture.completedFuture(ret);
		} catch( Exception e ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred.", e);
			return CompletableFuture.completedFuture(new StartServerResponse(StatusConverter.convert(is), null));
		}
	}

	@Override
	public CompletableFuture<Status> stopServerAsync(StopServerAttributes attr) {
		if( attr == null || isEmpty(attr.getId())) {
			return invalidParameterError();
		}

		IServer server = model.getServerModel().getServer(attr.getId());
		if( server == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server " + attr.getId() + " does not exist");
			return CompletableFuture.completedFuture(StatusConverter.convert(is));
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred: Server " + attr.getId() + " has no delegate.");
			return CompletableFuture.completedFuture(StatusConverter.convert(is));
		}
		
		if(del.getServerState() == IServerDelegate.STATE_STOPPED && !attr.isForce()) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
					"The server is already marked as stopped. If you wish to force a stop request, please set the force flag to true.");
			return CompletableFuture.completedFuture(StatusConverter.convert(is));
		}
		
		try {
			IStatus ret = del.stop(attr.isForce());
			return CompletableFuture.completedFuture(StatusConverter.convert(ret));
		} catch( Exception e ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred.", e);
			return CompletableFuture.completedFuture(StatusConverter.convert(is));
		}

	}

	@Override
	public CompletableFuture<CommandLineDetails> getLaunchCommand(LaunchParameters req) {
		if( req == null || isEmpty(req.getMode()) || isEmpty(req.getParams().getId())) {
			return CompletableFuture.completedFuture(null);
		}
		String id = req.getParams().getId();
		IServer server = model.getServerModel().getServer(id);
		if( server == null ) {
			return CompletableFuture.completedFuture(null);
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			return CompletableFuture.completedFuture(null);
		}
		try {
			CommandLineDetails det = del.getStartLaunchCommand(req.getMode(), req.getParams());
			return CompletableFuture.completedFuture(det);
		} catch( Exception e ) {
			return CompletableFuture.completedFuture(null);
		}
	}

	@Override
	public CompletableFuture<Status> serverStartingByClient(ServerStartingAttributes attr) {
		if( attr == null || attr.getRequest() == null || isEmpty(attr.getRequest().getMode())
				|| isEmpty(attr.getRequest().getParams().getId())) {
			return invalidParameterError();
		}
		String id = attr.getRequest().getParams().getId();
		IServer server = model.getServerModel().getServer(id);
		if( server == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server " + id + " does not exist");
			return CompletableFuture.completedFuture(StatusConverter.convert(is));
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server error: Server " + id + " does not have a delegate.");
			return CompletableFuture.completedFuture(StatusConverter.convert(is));
		}
		try {
			IStatus s = del.clientSetServerStarting(attr);
			return CompletableFuture.completedFuture(StatusConverter.convert(s));
		} catch( Exception e ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred.", e);
			return CompletableFuture.completedFuture(StatusConverter.convert(is));			
		}
	}
	@Override
	public CompletableFuture<Status> serverStartedByClient(LaunchParameters attr) {
		if( attr == null || attr.getParams() == null || isEmpty(attr.getParams().getId())) {
			return invalidParameterError();
		}

		String id = attr.getParams().getId();
		IServer server = model.getServerModel().getServer(id);
		if( server == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server " + id + " does not exist");
			return CompletableFuture.completedFuture(StatusConverter.convert(is));
		}
		IServerDelegate del = server.getDelegate();
		if( del == null ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server error: Server " + id + " does not have a delegate.");
			return CompletableFuture.completedFuture(StatusConverter.convert(is));
		}

		try {
			IStatus s = del.clientSetServerStarted(attr);
			return CompletableFuture.completedFuture(StatusConverter.convert(s));
		} catch( Exception e ) {
			IStatus is = new org.jboss.tools.rsp.eclipse.core.runtime.Status(IStatus.ERROR, 
					ServerCoreActivator.BUNDLE_ID, "An unexpected error occurred.", e);
			return CompletableFuture.completedFuture(StatusConverter.convert(is));			
		}

	}
}
