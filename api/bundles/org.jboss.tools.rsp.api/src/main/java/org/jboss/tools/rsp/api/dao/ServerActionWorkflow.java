/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
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
