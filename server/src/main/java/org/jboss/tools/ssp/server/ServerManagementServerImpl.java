/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.tools.ssp.api.ServerManagementClient;
import org.jboss.tools.ssp.api.ServerManagementServer;
import org.jboss.tools.ssp.api.SocketLauncher;
import org.jboss.tools.ssp.api.dao.CreateServerAttributes;
import org.jboss.tools.ssp.api.dao.DiscoveryPath;
import org.jboss.tools.ssp.api.dao.ServerAttributes;
import org.jboss.tools.ssp.api.dao.ServerBean;
import org.jboss.tools.ssp.api.dao.ServerHandle;
import org.jboss.tools.ssp.api.dao.ServerType;
import org.jboss.tools.ssp.api.dao.StartServerAttributes;
import org.jboss.tools.ssp.api.dao.Status;
import org.jboss.tools.ssp.api.dao.StopServerAttributes;
import org.jboss.tools.ssp.eclipse.core.runtime.IStatus;
import org.jboss.tools.ssp.launching.VMInstallModel;
import org.jboss.tools.ssp.server.core.internal.StatusConverter;
import org.jboss.tools.ssp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.ssp.server.model.RemoteEventManager;
import org.jboss.tools.ssp.server.model.ServerManagementModel;
import org.jboss.tools.ssp.server.spi.model.IServerManagementModel;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;

public class ServerManagementServerImpl implements ServerManagementServer {
	
	private final List<ServerManagementClient> clients = new CopyOnWriteArrayList<>();
	private final List<SocketLauncher<ServerManagementClient>> launchers 
		= new CopyOnWriteArrayList<>();
	
	private final VMInstallModel vmModel;
	private final ServerManagementModel model;
	private final RemoteEventManager eventManager;
	private ServerManagementServerLauncher launcher;
	
	public ServerManagementServerImpl(ServerManagementServerLauncher launcher) {
		this.launcher = launcher;
		vmModel = VMInstallModel.getDefault();
		vmModel.addActiveVM();
		model = new ServerManagementModel();
		eventManager = new RemoteEventManager(this);
	}
	
	public List<ServerManagementClient> getClients() {
		return new ArrayList<ServerManagementClient>(clients);
	}
	
	/**
	 * Connect the given chat client.
     * Return a runnable which should be executed to disconnect the client.
	 */
	public Runnable addClient(SocketLauncher<ServerManagementClient> launcher) {
		this.launchers.add(launcher);
		ServerManagementClient client = launcher.getRemoteProxy();
		this.clients.add(client);
		return () -> this.removeClient(launcher);
	}

	private void removeClient(SocketLauncher<ServerManagementClient> launcher) {
		this.launchers.add(launcher);
		this.clients.remove(launcher.getRemoteProxy());
		
	}
	
	public List<SocketLauncher<ServerManagementClient>> getActiveLaunchers() {
		return new ArrayList<SocketLauncher<ServerManagementClient>>(launchers);
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

	/**
	 * Add a path to our list of discovery paths
	 */
	@Override
	public void addDiscoveryPath(DiscoveryPath path) {
		model.getDiscoveryPathModel().addPath(path);
	}

	@Override
	public void removeDiscoveryPath(DiscoveryPath path) {
		model.getDiscoveryPathModel().removePath(path);
	}

	@Override
	public CompletableFuture<List<ServerBean>> findServerBeans(DiscoveryPath path) {
		ServerBeanLoader loader = new ServerBeanLoader(new File(path.getFilepath()));
		ServerBean bean = loader.getServerBean();
		List<ServerBean> ret = new ArrayList<>();
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
	public void deleteServer(ServerHandle handle) {
		model.getServerModel().removeServer(handle.getId());
	}

	@Override
	public CompletableFuture<CreateServerAttributes> getRequiredAttributes(ServerType type) {
		CreateServerAttributes sspa = model.getServerModel().getRequiredAttributes(type.getId());
		return CompletableFuture.completedFuture(sspa);
	}

	@Override
	public CompletableFuture<CreateServerAttributes> getOptionalAttributes(ServerType type) {
		CreateServerAttributes sspa = model.getServerModel().getOptionalAttributes(type.getId());
		return CompletableFuture.completedFuture(sspa);
	}

	@Override
	public CompletableFuture<Status> createServer(ServerAttributes attr) {
		String serverType = attr.getServerType();
		String id = attr.getId();
		Map<String, Object> attributes = attr.getAttributes();
		
		IStatus ret = model.getServerModel().createServer(serverType, id, attributes);
		return CompletableFuture.completedFuture(StatusConverter.convert(ret));
	}

	@Override
	public CompletableFuture<List<String>> getServerTypes() {
		String[] types = model.getServerModel().getServerTypes();
		List<String> asList = Arrays.asList(types);
		return CompletableFuture.completedFuture(asList);
	}

	@Override
	public CompletableFuture<Status> startServerAsync(StartServerAttributes attr) {
		IServer server = model.getServerModel().getServer(attr.getId());
		IServerDelegate del = server.getDelegate();
		IStatus ret = del.start(attr.getMode());
		return CompletableFuture.completedFuture(StatusConverter.convert(ret));
	}

	@Override
	public CompletableFuture<Status> stopServerAsync(StopServerAttributes attr) {
		IServer server = model.getServerModel().getServer(attr.getId());
		IServerDelegate del = server.getDelegate();
		IStatus ret = del.stop(attr.isForce());
		return CompletableFuture.completedFuture(StatusConverter.convert(ret));
	}
}
