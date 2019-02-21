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

public class WorkflowResponseItem {
	// An id for this item, such as 'workflow.license', "workflow.username", "workflow.password", etc
	// Extenders can put whatever they want here and request a client make custom UI for it, 
	// Or can try to re-use common types listed in ServerManagementAPIConstants
	private String id;
	
	private String itemType;

	// What to show: 
	
	// A label / prompt for this field, or null 
	private String label;

	// The content itself, whether that be the text of a license, 
	// or the value for a link url, 
	private String content;
	
	
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
	
	// A list of valid responses or null if any conforming type is acceptable
	private List<String> validResponses;

	
	
	/*
	 * Getters and setters below
	 */
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

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
	
	
}
