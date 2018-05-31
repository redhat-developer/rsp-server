package org.jboss.tools.ssp.api.dao;

public class ServerStartingAttributes {
	private boolean initiatePolling;
	private LaunchCommandRequest request;
	public ServerStartingAttributes(LaunchCommandRequest request, boolean initiatePolling) {
		this.request = request;
		this.initiatePolling = initiatePolling;
	}
	public boolean isInitiatePolling() {
		return initiatePolling;
	}
	public void setInitiatePolling(boolean initiatePolling) {
		this.initiatePolling = initiatePolling;
	}
	public LaunchCommandRequest getRequest() {
		return request;
	}
	public void setRequest(LaunchCommandRequest request) {
		this.request = request;
	}
}
