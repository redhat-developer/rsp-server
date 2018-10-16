/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.Map;

public class ServerCapabilitiesResponse {
	private Map<String, String> serverCapabilities;
	private Status clientRegistrationStatus;

	public ServerCapabilitiesResponse(Status clientRegistrationStatus, Map<String, String> map) {
		this.clientRegistrationStatus = clientRegistrationStatus;
		this.serverCapabilities = map;
	}

	public Status getClientRegistrationStatus() {
		return clientRegistrationStatus;
	}

	public Map<String, String> getServerCapabilities() {
		return serverCapabilities;
	}

	public void setServerCapabilities(Map<String, String> serverCapabilities) {
		this.serverCapabilities = serverCapabilities;
	}

	public void setClientRegistrationStatus(Status clientRegistrationStatus) {
		this.clientRegistrationStatus = clientRegistrationStatus;
	}
}
