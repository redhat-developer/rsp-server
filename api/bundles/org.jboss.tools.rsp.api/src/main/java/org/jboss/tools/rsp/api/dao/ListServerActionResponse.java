package org.jboss.tools.rsp.api.dao;

import java.util.Map;

public class ListServerActionResponse {
	private Map<String, WorkflowResponse> workflows;
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

	public Map<String, WorkflowResponse> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(Map<String, WorkflowResponse> workflows) {
		this.workflows = workflows;
	}
}
