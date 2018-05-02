package org.jboss.tools.ssp.client.bindings;
/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


import org.jboss.tools.ssp.api.ServerManagementAPIConstants;
import org.jboss.tools.ssp.api.ServerManagementClient;
import org.jboss.tools.ssp.api.ServerManagementServer;
import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerHandle;
import org.jboss.tools.ssp.api.beans.VMDescription;

public class ServerManagementClientImpl implements ServerManagementClient {
	
	public ServerManagementServer server;
	
	
	public void initialize(ServerManagementServer server) throws Exception {
		this.server = server;
	}

	public ServerManagementServer getProxy() {
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
	public void vmAdded(VMDescription vmd) {
		System.out.println("VM added: " + vmd.getId() + ":" + vmd.getInstallLocation());
	}

	@Override
	public void vmRemoved(VMDescription vmd) {
		System.out.println("VM removed: " + vmd.getId() + ":" + vmd.getInstallLocation());
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
	public void serverStateChanged(ServerHandle server, int state) {
		String stateString = null;
		switch(state) {
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
		System.out.println("Server state changed: " + server.getType() + ":" + server.getId() + " to " + stateString);
	}

	@Override
	public void serverProcessCreated(ServerHandle server, String processId) {
		System.out.println("Server process created: " + server.getType() + ":" + server.getId() + " @ " + processId);
	}

	@Override
	public void serverProcessTerminated(ServerHandle server, String processId) {
		System.out.println("Server process terminated: " + server.getType() + ":" + server.getId() + " @ " + processId);
	}

	@Override
	public void serverProcessOutputAppended(ServerHandle server, String processId, int streamType, String text) {
		System.out.println("ServerOutput: " + server.toString() + " ["+ processId + "][" + streamType + "] " + text);
	}
}
