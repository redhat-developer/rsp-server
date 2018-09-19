/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.dao.CapabilitiesRequest;
import org.jboss.tools.rsp.api.dao.CapabilitiesResponse;
import org.jboss.tools.rsp.server.spi.model.ICapabilityManagement;

public class CapabilityManagement implements ICapabilityManagement, ICapabilityKeys {
	
	
	private Map<RSPClient, Capabilities> capabilities;
	
	public CapabilityManagement() {
		this.capabilities = new HashMap<RSPClient, Capabilities>();
	}
	
	public void clientAdded(RSPClient client) {
		Capabilities cap = new Capabilities(client);
		this.capabilities.put(client, cap);
	}

	public void clientRemoved(RSPClient client) {
		this.capabilities.remove(client);
	}

	public String getCapabilityProperty(RSPClient c, String key) {
		Capabilities ca = capabilities.get(c);
		if( ca != null ) {
			return ca.getProperty(key);
		}
		return null;
	}
	
	private class Capabilities {
		
		private Map<String,String> data;
		private RSPClient client;
		public Capabilities(RSPClient client) {
			this.client = client;
			requestCapabilitiesSync();
		}
		

		private void requestCapabilitiesSync() {
			String[] requested = new String[] {STRING_PROTOCOL_VERSION, BOOLEAN_STRING_PROMPT};
			try {
				// Get a list of capabilities, whatever they may be
				CapabilitiesRequest req = new CapabilitiesRequest(Arrays.asList(requested));
				CapabilitiesResponse resp = client.getClientCapabilities(req).get();
				data = resp.getMap();
			} catch(ExecutionException | InterruptedException e) {
				data = new HashMap<>();
				data.put(STRING_PROTOCOL_VERSION, PROTOCOL_VERSION_0_9_0);
			}
		}
		
		public String getProperty(String key) {
			return data.get(key);
		}
	}
}
