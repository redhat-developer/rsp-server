package org.jboss.tools.ssp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.tools.ssp.api.ServerManagementClient;
import org.jboss.tools.ssp.api.ServerManagementServer;
import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerBean;
import org.jboss.tools.ssp.server.model.ServerManagementModel;

public class ServerManagementServerImpl implements ServerManagementServer {
	
	private final List<ServerManagementClient> clients = new CopyOnWriteArrayList<>();

	
	private final ServerManagementModel model = new ServerManagementModel();

	public ServerManagementServerImpl() {
		// Intentionally empty. Can be changed.
	}
	
	/**
	 * Connect the given chat client.
     * Return a runnable which should be executed to disconnect the client.
	 */
	public Runnable addClient(ServerManagementClient client) {
		this.clients.add(client);
		return () -> this.clients.remove(client);
	}

	/**
	 * Return existing messages.
	 */
	public CompletableFuture<List<DiscoveryPath>> getDiscoveryPaths() {
		return CompletableFuture.completedFuture(model.getRuntimePathModel().getPaths());
	}

	/**
	 * Add a path to our list of discovery paths
	 */
	public void addDiscoveryPath(DiscoveryPath path) {
		model.getRuntimePathModel().addPath(path);
	}

	@Override
	public void removeDiscoveryPath(DiscoveryPath path) {
		model.getRuntimePathModel().removePath(path);
		
	}

	@Override
	public CompletableFuture<List<ServerBean>> findServerBeans(DiscoveryPath path) {
		// TODO Auto-generated method stub
		ServerBean example = new ServerBean(path.getFilepath(), "someName", "AS", "DV", "10.1.0.abcde", "10.1", "o.j.t.wst.server.core.as");
		List<ServerBean> ret = new ArrayList<>();
		ret.add(example);
		return CompletableFuture.completedFuture(ret);
	}

}
