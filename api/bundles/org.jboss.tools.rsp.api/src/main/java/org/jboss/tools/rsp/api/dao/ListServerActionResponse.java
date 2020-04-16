/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
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
