package org.jboss.tools.rsp.api.dao;

import java.util.Map;

public class CapabilitiesResponse {
	private Map<String,String> map;
	public CapabilitiesResponse(Map<String,String> map) {
		this.map = map;
	}
	public Map<String,String> getMap() {
		return map;
	}
}
