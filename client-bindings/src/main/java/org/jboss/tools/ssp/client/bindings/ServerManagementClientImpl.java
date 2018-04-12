package org.jboss.tools.ssp.client.bindings;
/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


import org.jboss.tools.ssp.api.ServerManagementClient;
import org.jboss.tools.ssp.api.ServerManagementServer;

public class ServerManagementClientImpl implements ServerManagementClient {
	
	public ServerManagementServer server;
	
	
	public void initialize(ServerManagementServer server) throws Exception {
		this.server = server;
	}

	public ServerManagementServer getProxy() {
		return server;
	}
}
