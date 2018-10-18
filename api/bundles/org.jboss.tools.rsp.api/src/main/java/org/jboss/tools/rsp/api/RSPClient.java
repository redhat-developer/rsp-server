/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerProcess;
import org.jboss.tools.rsp.api.dao.ServerProcessOutput;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.StringPrompt;

@JsonSegment("client")
public interface RSPClient {

	/** 
	 * Prompt the user for some feature
	 */
	@JsonRequest
	CompletableFuture<String> promptString(StringPrompt prompt);

	

	/**
	 * The `client/discoveryPathAdded` notification is sent by the server to all
	 * clients in response to the `server/addDiscoveryPath` notification.
	 * 
	 * This call indicates that a discovery path has been added to the RSP model
	 * which keeps track of filesystem paths that may be searched for server
	 * runtimes.
	 */
	@JsonNotification
	void discoveryPathAdded(DiscoveryPath message);

	/**
	 * The `client/discoveryPathRemoved` notification is sent by the server to all
	 * clients in response to the `server/removeDiscoveryPath` notification.
	 *
	 * This call indicates that a discovery path has been removed from the RSP model
	 * which keeps track of filesystem paths that may be searched for server
	 * runtimes.
	 */
	@JsonNotification
	void discoveryPathRemoved(DiscoveryPath message);

	/**
	 * The `client/serverAdded` notification is sent by the server to all clients in
	 * a response to the `server/createServer` notification.
	 * 
	 * This notification indicates that a new server adapter has been created in the
	 * RSP model of existing servers. As mentioned above, this was most likely in
	 * response to a server/createServer notification, but is not strictly limited
	 * to this entrypoint.
	 */
	@JsonNotification
	void serverAdded(ServerHandle server);

	/**
	 * The `client/serverRemoved` notification is sent by the server to all clients
	 * in response to the `server/deleteServer` notification.
	 * 
	 * This notification indicates that a server adapter has been removed from the
	 * RSP model of existing servers. As mentioned above, this was most likely in
	 * response to a server/deleteServer notification, but is not strictly limited
	 * to this entrypoint.
	 */
	@JsonNotification
	void serverRemoved(ServerHandle server);

	/**
	 * The `client/serverRemoved` notification is sent by the server to all clients
	 * when any server has had one of its attributes changed.
	 */
	@JsonNotification
	void serverAttributesChanged(ServerHandle server);

	/**
	 * The `client/serverStateChanged` notification is sent by the server to all
	 * clients when any server has had its state change. 
	 * 
	 * Possible values include:
	 *   `0` representing an unknown state 
	 *   `1` representing starting 
	 *   `2` representing started 
	 *   `3` representing stopping 
	 *   `4` representing stopped
	 * 
	 */
	@JsonNotification
	void serverStateChanged(ServerState state);

	/**
	 * The `client/serverProcessCreated` notification is sent 
	 * by the server to all clients when any server 
	 * has launched a new process which can be monitored. 
	 * 
	 * This notification is most often sent in response to a call to 
	 * `server/startServerAsync` which will typically launch a process
	 * to run the server in question. 
	 */
	@JsonNotification
	void serverProcessCreated(ServerProcess process);

	/**
	 * The `client/serverProcessTerminated` notification is sent by 
	 * the server to all clients when any process associated with a 
	 * server has been terminated.
	 * 
	 * This notification is most often sent as a result of a call to 
	 * `server/stopServerAsync`, which  should shut down a given server
	 * and cause all of that server's processes to terminate after some time.
	 */
	@JsonNotification
	void serverProcessTerminated(ServerProcess process);

	/**
	 * The `client/serverProcessOutputAppended` notification is sent by 
	 * the server to all clients when any process associated with a 
	 * server generated output on any of its output streams.
	 * 
	 * This notification may be sent as a result of anything that 
	 * causes a given server process to emit output, such as a change in 
	 * configuration, a deployment, an error, normal logging, 
	 * or any other number of possibilities. 
	 */
	@JsonNotification
	void serverProcessOutputAppended(ServerProcessOutput output);

}
