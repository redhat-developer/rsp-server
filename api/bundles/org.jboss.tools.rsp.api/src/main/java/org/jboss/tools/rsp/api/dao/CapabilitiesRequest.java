package org.jboss.tools.rsp.api.dao;

import java.util.List;

public class CapabilitiesRequest {
	private List<String> list;
	public CapabilitiesRequest(List<String> list) {
		this.list = list;
	}
	public List<String> getList() {
		return list;
	}
}
