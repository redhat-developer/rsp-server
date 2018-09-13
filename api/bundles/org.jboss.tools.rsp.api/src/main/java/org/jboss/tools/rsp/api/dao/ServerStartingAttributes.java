/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class ServerStartingAttributes {
	private boolean initiatePolling;
	private LaunchParameters request;
	public ServerStartingAttributes() {
		
	}
	public ServerStartingAttributes(LaunchParameters request, boolean initiatePolling) {
		this.request = request;
		this.initiatePolling = initiatePolling;
	}
	public boolean isInitiatePolling() {
		return initiatePolling;
	}
	public void setInitiatePolling(boolean initiatePolling) {
		this.initiatePolling = initiatePolling;
	}
	public LaunchParameters getRequest() {
		return request;
	}
	public void setRequest(LaunchParameters request) {
		this.request = request;
	}
}
