/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package org.jboss.tools.ssp.api;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerHandle;
import org.jboss.tools.ssp.api.beans.VMDescription;

@JsonSegment("client")
public interface ServerManagementClient {
	
//	/**
//	 * The `client/didPostMessage` is sent by the server to all clients 
//	 * in a response to the `server/postMessage` notification.
//	 */

	@JsonNotification
	void discoveryPathAdded(DiscoveryPath message);

	@JsonNotification
	void discoveryPathRemoved(DiscoveryPath message);

	@JsonNotification
	void vmAdded(VMDescription vmd);
	
	@JsonNotification
	void vmRemoved(VMDescription vmd);
	
	@JsonNotification
	void serverAdded(ServerHandle server);
	
	@JsonNotification
	void serverRemoved(ServerHandle server);
	
	@JsonNotification
	void serverAttributesChanged(ServerHandle server);
	
	@JsonNotification
	void serverStateChanged(ServerHandle server, int state);
	
	@JsonNotification
	void serverProcessCreated(ServerHandle server, String processId);

	@JsonNotification
	void serverProcessTerminated(ServerHandle server, String processId);
	

	@JsonNotification
	void serverProcessOutputAppended(ServerHandle server, String processId, int streamType, String text);
	
}
