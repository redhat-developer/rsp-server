package org.jboss.tools.rsp.api.dao;

import java.util.List;

public class ListServerActionResponse {
	private List<ServerActionWorkflow> workflows;
	private Status status;
	
	public ListServerActionResponse() {
		// Do nothing
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<ServerActionWorkflow> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<ServerActionWorkflow> workflows) {
		this.workflows = workflows;
	}
}
