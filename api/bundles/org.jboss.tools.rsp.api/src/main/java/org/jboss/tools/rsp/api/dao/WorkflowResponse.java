/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.List;

public class WorkflowResponse {
	
	// The status of your request
	private Status status;
	
	// A request id assigned by the server to possibly be followed up with by client later
	private long requestId;
	
	// A job ID if a long-running task has been initiated
	private String jobId;

	// A possible list of workflow items to be completed or null 
	private List<WorkflowResponseItem> items;
	
	private List<String> invalidFields;
	
	public WorkflowResponse() {
		super();
	}

	public List<WorkflowResponseItem> getItems() {
		return items;
	}

	public void setItems(List<WorkflowResponseItem> items) {
		this.items = items;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public List<String> getInvalidFields() {
		return invalidFields;
	}

	public void setInvalidFields(List<String> invalidFields) {
		this.invalidFields = invalidFields;
	}
}
