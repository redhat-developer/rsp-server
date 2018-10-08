/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
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
