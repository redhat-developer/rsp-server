/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.model.ICapabilityManagement;

public class CapabilityManagement implements ICapabilityManagement, ICapabilityKeys {
	
	
	private Map<RSPClient, Capabilities> capabilities;
	
	public CapabilityManagement() {
		this.capabilities = new HashMap<RSPClient, Capabilities>();
	}
	
	public void clientAdded(RSPClient client) {
		// add a default implementation until a client registers their capabilities
		Capabilities cap = new Capabilities009();
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
	
	public IStatus registerClientCapabilities(RSPClient client, ClientCapabilitiesRequest response) {
		ClientCapabilities cc = new ClientCapabilities(response);
		this.capabilities.put(client, cc);
		return Status.OK_STATUS;
	}
	
	private static class Capabilities {
		private Map<String,String> data;
		public Capabilities(Map<String,String> data) {
			this.data = data;
		}
		
		public String getProperty(String key) {
			return data == null ? null : data.get(key);
		}
	}
	
	private static class ClientCapabilities extends Capabilities {
		public ClientCapabilities(ClientCapabilitiesRequest response) {
			super(response == null ? null : response.getMap());
		}
	}
	
	private static class Capabilities009 extends Capabilities {
		public Capabilities009() {
			super(defaultCapabilities());
		}
		private static Map<String,String> defaultCapabilities() {
            Map<String,String> ret = new HashMap<String,String>();
            ret.put(ICapabilityKeys.STRING_PROTOCOL_VERSION, ICapabilityKeys.PROTOCOL_VERSION_0_9_0);
            ret.put(ICapabilityKeys.BOOLEAN_STRING_PROMPT, Boolean.toString(false));
            return ret;
		}
	}

	@Override
	public Map<String,String> getServerCapabilities() {
        Map<String,String> ret = new HashMap<String,String>();
        ret.put(ICapabilityKeys.STRING_PROTOCOL_VERSION, ICapabilityKeys.PROTOCOL_VERSION_CURRENT);
        ret.put(ICapabilityKeys.BOOLEAN_STRING_PROMPT, Boolean.toString(true));
        ret.put(ICapabilityKeys.BOOLEAN_PUBLISH_ARCHIVE, Boolean.toString(true));
        return ret;
	}
}
