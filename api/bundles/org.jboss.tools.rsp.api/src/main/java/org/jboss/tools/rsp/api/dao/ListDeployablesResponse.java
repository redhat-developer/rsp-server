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

public class ListDeployablesResponse {
	private List<DeployableState> states = null;
	private Status status;
	public ListDeployablesResponse() {
		
	}
	public ListDeployablesResponse(List<DeployableState> states, Status stat) {
		this.states = states;
		this.status = stat;
	}

	public List<DeployableState> getStates() {
		return states;
	}
	public void setStates(List<DeployableState> states) {
		this.states = states;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
}
