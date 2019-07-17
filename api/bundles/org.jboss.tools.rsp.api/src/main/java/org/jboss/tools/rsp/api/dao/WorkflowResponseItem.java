/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.HashMap;

public class WorkflowResponseItem {
	// An id for this item, such as 'workflow.license', "workflow.username", "workflow.password", etc
	// Extenders can put whatever they want here and request a client make custom UI for it, 
	// Or can try to re-use common types listed in ServerManagementAPIConstants
	private String id;
	
	/*
	 * One of
	 *    null (defaults to  WORKFLOW_TYPE_PROMPT_SMALL)
	 *    ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL
	 *    ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_LARGE
	 *    ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_EDITOR
	 *    ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_BROWSER
	 */
	private String itemType;

	// What to show: 
	
	// A label / prompt for this field, or null 
	private String label;

	// The content itself, whether that be the text of a license, 
	// or the value for a link url, 
	private String content;
	
	private WorkflowPromptDetails prompt;
	
	private HashMap<String,String> properties;
	
	
	public WorkflowResponseItem() {
		// 0-arg constructor 
	}
	
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

	public WorkflowPromptDetails getPrompt() {
		return prompt;
	}

	public void setPrompt(WorkflowPromptDetails prompt) {
		this.prompt = prompt;
	}

	public HashMap<String,String> getProperties() {
		return properties;
	}

	public void setProperties(HashMap<String,String> properties) {
		this.properties = properties;
	}

}
