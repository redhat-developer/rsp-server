/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.Map;

public class UpdateServerResponse {
	private ServerHandle handle;
	private String serverJson;
	private Status status;
	private Map<String, String> propertiesInError;
	
	public UpdateServerResponse() {
		
	}

	public ServerHandle getHandle() {
		return handle;
	}

	public void setHandle(ServerHandle handle) {
		this.handle = handle;
	}

	public String getServerJson() {
		return serverJson;
	}

	public void setServerJson(String serverJson) {
		this.serverJson = serverJson;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Map<String, String> getPropertiesInError() {
		return propertiesInError;
	}

	public void setPropertiesInError(Map<String, String> propertiesInError) {
		this.propertiesInError = propertiesInError;
	}
}
