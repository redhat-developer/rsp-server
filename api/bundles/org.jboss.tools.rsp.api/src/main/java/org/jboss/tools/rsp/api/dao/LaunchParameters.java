/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class LaunchParameters {
	private String mode;
	private ServerAttributes params;
	public LaunchParameters() {
		
	}
	public LaunchParameters(ServerAttributes params, String mode) {
		this.mode = mode;
		this.params = params;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public ServerAttributes getParams() {
		return params;
	}

	public void setParams(ServerAttributes params) {
		this.params = params;
	}
}
