/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.client.bindings;

import java.util.concurrent.CompletableFuture;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerProcess;
import org.jboss.tools.rsp.api.dao.ServerProcessOutput;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.StringPrompt;
import org.jboss.tools.rsp.client.cli.InputHandler;
import org.jboss.tools.rsp.client.cli.InputProvider;

public class ServerManagementClientImpl implements RSPClient {
	
	private RSPServer server;
	private InputProvider inputProvider;
	public ServerManagementClientImpl() {
		super();
		// here for debugging
	}
	
	public void initialize(RSPServer server, InputProvider inputProvider) throws Exception {
		this.server = server;
		this.inputProvider = inputProvider;
	}

	public RSPServer getProxy() {
		return server;
	}

	@Override
	public void discoveryPathAdded(DiscoveryPath message) {
		System.out.println("Added discovery path: " + message.getFilepath());
	}

	@Override
	public void discoveryPathRemoved(DiscoveryPath message) {
		System.out.println("Removed discovery path: " + message.getFilepath());
	}
	
	@Override
	public void serverAdded(ServerHandle server) {
		System.out.println("Server added: " + server.getType() + ":" + server.getId());
	}

	@Override
	public void serverRemoved(ServerHandle server) {
		System.out.println("Server removed: " + server.getType() + ":" + server.getId());
	}

	@Override
	public void serverAttributesChanged(ServerHandle server) {
		System.out.println("Server attribute changed: " + server.getType() + ":" + server.getId());
	}

	@Override
	public void serverStateChanged(ServerState state) {
		String stateString = null;
		switch(state.getState()) {
		case ServerManagementAPIConstants.STATE_STARTED:
			stateString = "started";
			break;
		case ServerManagementAPIConstants.STATE_STARTING:
			stateString = "starting";
			break;
		case ServerManagementAPIConstants.STATE_STOPPED:
			stateString = "stopped";
			break;
		case ServerManagementAPIConstants.STATE_STOPPING:
			stateString = "stopping";
			break;
			
		}
		System.out.println("Server state changed: " + state.getServer().getType() + ":" + state.getServer().getId() + " to " + stateString);
	}

	@Override
	public void serverProcessCreated(ServerProcess process) {
		System.out.println("Server process created: " + 
				process.getServer().getType() + ":" + process.getServer().getId() + " @ " 
				+ process.getProcessId());
	}

	@Override
	public void serverProcessTerminated(ServerProcess process) {
		System.out.println("Server process terminated: " 
				+ process.getServer().getType() + ":" + process.getServer().getId() + " @ " 
				+ process.getProcessId());
	}

	@Override
	public void serverProcessOutputAppended(ServerProcessOutput out) {
		System.out.println("ServerOutput: " 
				+ out.getServer().toString() + " ["
				+ out.getProcessId() + "][" 
				+ out.getStreamType() + "] " + out.getText());
	}

	@Override
	public CompletableFuture<String> promptString(StringPrompt prompt) {
		@SuppressWarnings("unchecked")
		final CompletableFuture<String>[] ret = new CompletableFuture[1];
		ret[0] = null;
		
		PromptStringHandler h2 = new PromptStringHandler(prompt.getPrompt()) {
			public void handleInput(String line) throws Exception {
				ret[0] = CompletableFuture.completedFuture(line);
			}
		};
		inputProvider.addInputRequest(h2);
		
		long init = System.currentTimeMillis();
		while( System.currentTimeMillis() < (init + 120000)) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ie) {
				
			}
			if( ret[0] != null ) {
				return ret[0];
			}
		}
		return CompletableFuture.completedFuture(null);
	}
	
	private abstract class PromptStringHandler implements InputHandler {
		private String prompt;
		public PromptStringHandler(String prompt) {
			this.prompt = prompt;
		}

		@Override
		public String getPrompt() {
			return prompt;
		}

		public abstract void handleInput(String line) throws Exception;
	}

}
