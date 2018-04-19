package org.jboss.tools.ssp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.StandardVMType;
import org.jboss.tools.jdt.launching.VMInstallModel;
import org.jboss.tools.ssp.api.ServerManagementClient;
import org.jboss.tools.ssp.api.ServerManagementServer;
import org.jboss.tools.ssp.api.SocketLauncher;
import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerBean;
import org.jboss.tools.ssp.api.beans.VMDescription;
import org.jboss.tools.ssp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.ssp.server.model.ServerManagementModel;

public class ServerManagementServerImpl implements ServerManagementServer {
	
	private final List<ServerManagementClient> clients = new CopyOnWriteArrayList<>();
	private final List<SocketLauncher<ServerManagementClient>> launchers 
		= new CopyOnWriteArrayList<>();
	
	private final ServerManagementModel model = new ServerManagementModel();

	public ServerManagementServerImpl() {
		// Intentionally empty. Can be changed.
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
	
	public ServerManagementModel getModel() {
		return model;
	}
	
	/*
	 * Some methods for adding or removing VMs
	 */
	
	/**
	 * Get a list of VMs currently registered
	 * @return
	 */
	@Override
	public CompletableFuture<List<VMDescription>> getVMs() {
		IVMInstall[] arr = VMInstallModel.getDefault().getVMs();
		VMDescription[] vmd = new VMDescription[arr.length];
		for( int i = 0; i < arr.length; i++ ) {
			String vers = arr[i] instanceof IVMInstall2 ? ((IVMInstall2)arr[i]).getJavaVersion() : null;
			vmd[i] = new VMDescription(arr[i].getId(), arr[i].getInstallLocation().getAbsolutePath(), vers);
		}
		return CompletableFuture.completedFuture(Arrays.asList(vmd));
	}
	
	@Override
	public void addVM(String id, String absolutePath) {
		// TODO Check that the vm doesn't already exist
		IVMInstall vmi = StandardVMType.getDefault().createVMInstall(id);
		vmi.setInstallLocation(new File(absolutePath));
		VMInstallModel.getDefault().addVMInstall(vmi);
	}

	@Override
	public void removeVM(String id) {
		VMInstallModel.getDefault().removeVMInstall(id);
	}

	
	
	/**
	 * Return existing messages.
	 */
	@Override
	public CompletableFuture<List<DiscoveryPath>> getDiscoveryPaths() {
		return CompletableFuture.completedFuture(model.getRuntimePathModel().getPaths());
	}

	/**
	 * Add a path to our list of discovery paths
	 */
	@Override
	public void addDiscoveryPath(DiscoveryPath path) {
		model.getRuntimePathModel().addPath(path);
	}

	@Override
	public void removeDiscoveryPath(DiscoveryPath path) {
		model.getRuntimePathModel().removePath(path);
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
		ServerManagementServerLauncher.getDefault().shutdown();
	}
	
	

}
