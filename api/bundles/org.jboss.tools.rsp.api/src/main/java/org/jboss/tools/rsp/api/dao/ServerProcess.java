/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class ServerProcess {
	private ServerHandle server;
	private String processId;
	public ServerProcess() {
		
	}
	public ServerProcess(ServerHandle handle, String process) {
		this.processId = process;
		this.server = handle;
	}
	public ServerHandle getServer() {
		return server;
	}
	public void setServer(ServerHandle server) {
		this.server = server;
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
}
