package org.jboss.tools.ssp.api.dao;

public class LaunchCommandRequest {
	private String mode;
	private ServerAttributes params;

	public LaunchCommandRequest(ServerAttributes params, String mode) {
		this.mode = mode;
		this.params = params;
		
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
