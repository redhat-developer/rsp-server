/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.api;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerHandle;
import org.jboss.tools.ssp.api.beans.ServerProcess;
import org.jboss.tools.ssp.api.beans.ServerProcessOutput;
import org.jboss.tools.ssp.api.beans.ServerStateChange;
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
	void serverStateChanged(ServerStateChange stateChange);
	
	@JsonNotification
	void serverProcessCreated(ServerProcess process);

	@JsonNotification
	void serverProcessTerminated(ServerProcess process);
	

	@JsonNotification
	void serverProcessOutputAppended(ServerProcessOutput output);
	
}
