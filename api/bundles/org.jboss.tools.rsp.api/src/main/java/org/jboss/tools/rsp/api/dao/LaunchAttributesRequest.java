/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class LaunchAttributesRequest {
	private String serverTypeId;
	private String mode;
	public LaunchAttributesRequest() {
		
	}
	public LaunchAttributesRequest(String serverTypeId, String mode) {
		this.serverTypeId = serverTypeId;
		this.mode = mode;
	}

	public String getServerTypeId() {
		return serverTypeId;
	}
	public void setServerTypeId(String serverTypeId) {
		this.serverTypeId = serverTypeId;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
}
