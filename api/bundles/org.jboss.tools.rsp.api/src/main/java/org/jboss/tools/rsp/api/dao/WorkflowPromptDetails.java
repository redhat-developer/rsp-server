/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.List;

public class WorkflowPromptDetails {

	/*
	 * One of 
	 *    null  (no response required)
	 *    ServerManagementAPIConstants.ATTR_TYPE_BOOL
	 *    ServerManagementAPIConstants.ATTR_TYPE_INT
	 *    ServerManagementAPIConstants.ATTR_TYPE_STRING
	 *    ServerManagementAPIConstants.ATTR_TYPE_LIST
	 *    ServerManagementAPIConstants.ATTR_TYPE_MAP
	 */
	private String responseType;
	
	/*
	 * Is the response a secret / should it be hidden in the UI when typing it?
	 */
	private boolean responseSecret = false;
	
	// A list of valid responses or null if any conforming type is acceptable
	private List<String> validResponses;

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public List<String> getValidResponses() {
		return validResponses;
	}

	public void setValidResponses(List<String> validResponses) {
		this.validResponses = validResponses;
	}

	public boolean isResponseSecret() {
		return responseSecret;
	}

	public void setResponseSecret(boolean responseSecret) {
		this.responseSecret = responseSecret;
	}
	
	
}
