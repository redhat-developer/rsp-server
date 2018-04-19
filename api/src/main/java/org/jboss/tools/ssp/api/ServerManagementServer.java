package org.jboss.tools.ssp.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerBean;
import org.jboss.tools.ssp.api.beans.VMDescription;

@JsonSegment("server")
public interface ServerManagementServer {
	
	/**
	 * The `server/getVMs` request is sent by the client to fetch 
	 * a list of VMs that are able to be used 
	 */
	@JsonRequest
	CompletableFuture<List<VMDescription>> getVMs();

	@JsonNotification
	void addVM(String id, String absolutePath);

	@JsonNotification
	public void removeVM(String id);
	
	
	
	
	/**
	 * The `server/getDiscoveryPaths` request is sent by the client to fetch 
	 * a list of discovery paths that can be searched. 
	 */
	@JsonRequest
	CompletableFuture<List<DiscoveryPath>> getDiscoveryPaths();

	
	/**
	 * The `server/findServerBeans` request is sent by the client to fetch 
	 * a list of server beans for the given path 
	 */
	@JsonRequest
	CompletableFuture<List<ServerBean>> findServerBeans(DiscoveryPath path);
	
	
	/**
	 * The `server/addDiscoveryPath` notification is sent by the client to add a new path
	 * to search when discovering servers.
	 */
	@JsonNotification
	void addDiscoveryPath(DiscoveryPath path);
	
	/**
	 * The `server/removeDiscoveryPath` notification is sent by the client to 
	 * remove a path from the list to search when discovering servers.
	 */
	@JsonNotification
	void removeDiscoveryPath(DiscoveryPath path);
	
	

}
