package org.jboss.tools.ssp.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("server")
public interface ServerManagementServer {
	
	/**
	 * The `server/getDiscoveryPaths` request is sent by the client to fetch 
	 * a list of discovery paths that can be searched. 
	 */
	@JsonRequest
	CompletableFuture<List<DiscoveryPath>> getDiscoveryPaths();
	
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
