/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class GetServerJsonResponse {
	private Status status;
	private String serverJson;
	private ServerHandle serverHandle;
	
	public GetServerJsonResponse() {
		// 0-arg constructor required
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getServerJson() {
		return serverJson;
	}
	public void setServerJson(String serverJson) {
		this.serverJson = serverJson;
	}
	public ServerHandle getServerHandle() {
		return serverHandle;
	}
	public void setServerHandle(ServerHandle serverHandle) {
		this.serverHandle = serverHandle;
	}
}
