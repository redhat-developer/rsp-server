package org.jboss.tools.ssp.api.beans;

public class ServerHandle {
	private String id;
	private String type;
	public ServerHandle(String id, String type) {
		this.id = id;
		this.type = type;
	}
	
	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

}
