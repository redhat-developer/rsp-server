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

@JsonSegment("client")
public interface ServerManagementClient {
	
	/**
	 * The `client/discoveryPathAdded` is sent by the server to all clients 
	 * in a response to the `server/addDiscoveryPath` notification.
	 */
	@JsonNotification
	void discoveryPathAdded(DiscoveryPath message);

	/**
	 * The `client/discoveryPathRemoved` is sent by the server to all clients 
	 * in a response to the `server/removeDiscoveryPath` notification.
	 */
	@JsonNotification
	void discoveryPathRemoved(DiscoveryPath message);
//
//	/**
//	 * The `client/vmAdded` is sent by the server to all clients 
//	 * in a response to the `server/addVM` notification.
//	 */
//	@JsonNotification
//	void vmAdded(VMDescription vmd);
//	
//	/**
//	 * The `client/vmRemoved` is sent by the server to all clients 
//	 * in a response to the `server/removeVM` notification.
//	 */
//	@JsonNotification
//	void vmRemoved(VMDescription vmd);
	
	/**
	 * The `client/serverAdded` is sent by the server to all clients 
	 * in a response to the `server/createServer` notification.
	 */
	@JsonNotification
	void serverAdded(ServerHandle server);

	/**
	 * The `client/serverRemoved` is sent by the server to all clients 
	 * in a response to the `server/deleteServer` notification.
	 */
	@JsonNotification
	void serverRemoved(ServerHandle server);
	
	
	/**
	 * The `client/serverRemoved` is sent by the server to all clients 
	 * when any server has had its attributes changed
	 */
	@JsonNotification
	void serverAttributesChanged(ServerHandle server);
	
	/**
	 * The `client/serverStateChanged` is sent by the server to all clients 
	 * when any server has had its state change. 
	 * Possible values include:
	 *  `0` representing an unknown state
	 *  `1` representing starting
	 *  `2` representing started
	 *  `3` representing stopping
	 *  `4` representing stopped 
	 *    
	 */
	@JsonNotification
	void serverStateChanged(ServerStateChange stateChange);
	
	/**
	 * The `client/serverProcessCreated` is sent by the server to all clients 
	 * when any server has launched a new process which can be monitored 
	 */
	@JsonNotification
	void serverProcessCreated(ServerProcess process);
	
	/**
	 * The `client/serverProcessTerminated` is sent by the server to all clients 
	 * when any process associated with a server has been terminated. 
	 */
	@JsonNotification
	void serverProcessTerminated(ServerProcess process);
	

	/**
	 * The `client/serverProcessOutputAppended` is sent by the server to all clients 
	 * when any process associated with a server generated output on 
	 * any of its output streams. 
	 */
	@JsonNotification
	void serverProcessOutputAppended(ServerProcessOutput output);
	
}
