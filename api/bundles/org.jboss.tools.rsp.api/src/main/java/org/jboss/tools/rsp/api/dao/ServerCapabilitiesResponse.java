package org.jboss.tools.rsp.api.dao;

import java.util.Map;

public class ServerCapabilitiesResponse {
	private Map<String,String> serverCapabilities;
	public ServerCapabilitiesResponse(Map<String,String> map) {
		this.serverCapabilities = map;
	}
	public Map<String,String> getMap() {
		return serverCapabilities;
	}
}
