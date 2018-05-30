package org.jboss.tools.ssp.api.dao;

public class GetLaunchCommandRequest {
	private String serverId;
	private String mode;
	private ServerAttributes params;

	public GetLaunchCommandRequest(String serverId, String mode, ServerAttributes params) {
		this.serverId = serverId;
		this.mode = mode;
		this.params = params;
		
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public ServerAttributes getParams() {
		return params;
	}

	public void setParams(ServerAttributes params) {
		this.params = params;
	}
}
