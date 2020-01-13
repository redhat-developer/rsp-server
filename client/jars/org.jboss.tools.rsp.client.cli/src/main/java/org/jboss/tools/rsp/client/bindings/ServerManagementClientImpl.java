/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.client.bindings;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.api.dao.JobProgress;
import org.jboss.tools.rsp.api.dao.JobRemoved;
import org.jboss.tools.rsp.api.dao.MessageBoxNotification;
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
	
	public void initialize(RSPServer server, InputProvider inputProvider) {
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
		System.out.println("Server added: " + server.getType().getId() + ":" + server.getId());
	}

	@Override
	public void serverRemoved(ServerHandle server) {
		System.out.println("Server removed: " + server.getType().getId() + ":" + server.getId());
	}

	@Override
	public void serverAttributesChanged(ServerHandle server) {
		System.out.println("Server attribute changed: " + server.getType().getId() + ":" + server.getId());
	}
	
	@Override
	public void messageBox(MessageBoxNotification notify) {
		System.out.println("MessageBoxNotification: " + notify.getMessage());
	}

	@Override
	public void serverStateChanged(ServerState state) {
		StringBuilder sb = new StringBuilder();
		sb.append("Server state change: \n  Server:");
		sb.append(state.getServer().getType() + ":" + state.getServer().getId()); 
		sb.append("\n  State: ");
		sb.append(getRunStateString(state.getState()));
		sb.append(",  Mode: ");
		sb.append(state.getRunMode());
		sb.append("\n  Publish State: ");
		sb.append(getPublishStateString(state.getPublishState()));
		sb.append("\n  Deployments: ");
		
		List<DeployableState> deployments = state.getDeployableStates();
		for( DeployableState ds : deployments ) {
			sb.append("\n    " + ds.getReference().getLabel() );
			sb.append(" [" + getRunStateString(ds.getState()) + "]");
			sb.append(" [" + getPublishStateString(ds.getPublishState()) + "]");
		}
		System.out.println(sb.toString());
	}

	@Override
	public void serverProcessCreated(ServerProcess process) {
		System.out.println("Server process created: " + 
				process.getServer().getType() + ":" + process.getServer().getId() + " @ " 
				+ process.getProcessId());
	}
	
	public static String getRunStateString(int state) {
		String stateString = null;
		switch(state) {
		case ServerManagementAPIConstants.STATE_UNKNOWN:
			stateString = "unknown";
			break;
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
		return stateString;
	}

	public static String getPublishStateString(int state) {
		String stateString = null;
		switch(state) {
		case ServerManagementAPIConstants.PUBLISH_STATE_ADD:
			stateString = "add";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_FULL:
			stateString = "full";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL:
			stateString = "incremental";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_NONE:
			stateString = "synchronized";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_REMOVE:
			stateString = "remove";
			break;
		case ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN:
			stateString = "unknown";
			break;
			
		}
		return stateString;
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
		
		PromptStringHandler h2 = new PromptStringHandler(prompt.getPrompt(), prompt.isSecret()) {
			public void handleInput(String line) throws Exception {
				ret[0] = CompletableFuture.completedFuture(line);
				setDone();
			}
		};
		inputProvider.addInputRequest(h2);
		
		long init = System.currentTimeMillis();
		while( System.currentTimeMillis() < (init + 120000)) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ie) {
				 Thread.currentThread().interrupt();
			}
			if( ret[0] != null ) {
				return ret[0];
			}
		}
		return CompletableFuture.completedFuture(null);
	}
	
	public abstract static class PromptStringHandler implements InputHandler {
		private String prompt;
		private boolean isSecret;
		private boolean done = false;
		public PromptStringHandler(String prompt) {
			this(prompt, false);
		}
		public PromptStringHandler(String prompt, boolean secret) {
			this.prompt = prompt;
			this.isSecret = secret;
		}

		@Override
		public String getPrompt() {
			return prompt;
		}
		
		@Override
		public boolean isSecret() {
			return isSecret;
		}

		public abstract void handleInput(String line) throws Exception;
		
		@Override
		public boolean isDone() {
			return done;
		}
		
		protected void setDone() {
			done = true;
		}
	}
	@Override
	public void jobAdded(JobHandle job) {
		System.out.println("Job " + job.getName() + " (" + job.getId() + ") is now running.");
	}

	@Override
	public void jobRemoved(JobRemoved removed) {
		JobHandle h = removed.getHandle();
		System.out.println("Job " + h.getName() + " (" + h.getId() + ") has stopped running: " + removed.getStatus().toString());
	}

	@Override
	public void jobChanged(JobProgress progress) {
		JobHandle h = progress.getHandle();
		System.out.println("Job " + h.getName() + " (" + h.getId() + ") is at " + progress.getPercent() + "%");
	}

}
