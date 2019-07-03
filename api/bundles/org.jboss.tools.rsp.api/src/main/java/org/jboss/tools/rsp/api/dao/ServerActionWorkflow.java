package org.jboss.tools.rsp.api.dao;

public class ServerActionWorkflow {
	private String actionId;
	private String actionLabel;
	private WorkflowResponse actionWorkflow;
	
	public ServerActionWorkflow() {
		// 0-arg
	}
	
	public ServerActionWorkflow(String id, String label, WorkflowResponse workflow) {
		this.actionId = id;
		this.actionLabel = label;
		this.actionWorkflow = workflow;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getActionLabel() {
		return actionLabel;
	}

	public void setActionLabel(String actionLabel) {
		this.actionLabel = actionLabel;
	}

	public WorkflowResponse getActionWorkflow() {
		return actionWorkflow;
	}

	public void setActionWorkflow(WorkflowResponse actionWorkflow) {
		this.actionWorkflow = actionWorkflow;
	}
	
}
