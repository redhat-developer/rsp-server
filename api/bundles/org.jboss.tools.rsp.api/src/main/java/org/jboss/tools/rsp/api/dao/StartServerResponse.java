package org.jboss.tools.rsp.api.dao;

public class StartServerResponse {
	private Status status;
	private CommandLineDetails details;
	public StartServerResponse() {
		
	}
	public StartServerResponse(Status status, CommandLineDetails details) {
		this.status = status;
		this.details = details;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public CommandLineDetails getDetails() {
		return details;
	}

	public void setDebugDetails(CommandLineDetails details) {
		this.details = details;
	}
}
