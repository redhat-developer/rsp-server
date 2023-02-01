/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.servertype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.CreateServerWorkflowRequest;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.WorkflowPromptDetails;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.SPIActivator;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public abstract class AbstractServerType implements IServerType {

	protected String id;
	protected String name;
	protected String desc;

	public AbstractServerType(String id, String name, String desc) {
		this.id = id;
		this.name = name;
		this.desc = desc;
	}

	public Attributes getRequiredAttributes() {
		return new CreateServerAttributesUtility().toPojo();
	}

	public Attributes getOptionalAttributes() {
		return new CreateServerAttributesUtility().toPojo();
	}

	public Attributes getRequiredLaunchAttributes() {
		return new CreateServerAttributesUtility().toPojo();
	}

	public Attributes getOptionalLaunchAttributes() {
		return new CreateServerAttributesUtility().toPojo();
	}
	
	protected long ensureRequestId(long requestId) {
		if (requestId == -1 || requestId == 0) {
			// New request, return what we need
			requestId = (long) ((Math.random() * ((100000 - 10) + 1)) + 10);
		}
		return requestId;
	}
	
	protected WorkflowResponseItem attributeToWorkflowItem(String key, Attribute a) {
		WorkflowResponseItem item = new WorkflowResponseItem();
		item.setId(key);
		item.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL);
		item.setLabel(a.getDescription());
		WorkflowPromptDetails prompt = new WorkflowPromptDetails();
		prompt.setResponseType(a.getType());
		prompt.setResponseSecret(a.isSecret());
		item.setPrompt(prompt);
		return item;
	}
	
	/*
	 * This is the default implementation of a new API. 
	 * The default implementation acts as a bridge between the old 
	 * and new API. 
	 */
	public WorkflowResponse createServerWorkflow(RSPServer server, CreateServerWorkflowRequest req) {
		if( req.getRequestId() <= 0 ) {
			return handleInitialCreateServerWorkflow(req);
		}
		ServerAttributes attr = new ServerAttributes();
		attr.setServerType(req.getServerTypeId());
		String id = req.getData().get("id") != null ? req.getData().get("id").toString() : null;
		if( id == null ) {
			return errorWorkflowResponse("attribute 'id' must not be null");
		}
		attr.setId(id);
		attr.setAttributes(req.getData());
		CompletableFuture<CreateServerResponse> create = server.createServer(attr);
		CreateServerResponse result = null;
		try {
			result = create.get();
		} catch(ExecutionException | InterruptedException ee) {
			if( ee instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			return errorWorkflowResponse(ee.getMessage());
		}
		if( result.getStatus().isOK()) {
			WorkflowResponse resp = new WorkflowResponse();
			resp.setStatus(StatusConverter.convert(Status.OK_STATUS));
			resp.setItems(new ArrayList<>());
			return resp;
		}
		
		// not ok. 
		List<String> invalid = result.getInvalidKeys();
		WorkflowResponse resp = handleInitialCreateServerWorkflow(req);
		resp.setStatus(StatusConverter.convert(new Status(IStatus.INFO, SPIActivator.BUNDLE_ID, "Invalid Fields")));
		resp.setInvalidFields(invalid);
		return resp;
	}

	public static WorkflowResponse cancelWorkflowResponse() {
		WorkflowResponse resp = new WorkflowResponse();
		resp.setStatus(StatusConverter.convert(Status.CANCEL_STATUS));
		resp.setItems(new ArrayList<>());
		return resp;
	}
	public static WorkflowResponse errorWorkflowResponse(String msg) {
		WorkflowResponse resp = new WorkflowResponse();
		resp.setStatus(StatusConverter.convert(new Status(Status.ERROR, SPIActivator.BUNDLE_ID, msg)));
		resp.setItems(new ArrayList<>());
		return resp;
	}

	protected WorkflowResponse handleInitialCreateServerWorkflow(CreateServerWorkflowRequest req) {
		long requestId = ensureRequestId(req.getRequestId());
		WorkflowResponse workflow = new WorkflowResponse();
		workflow.setRequestId(requestId);
		
		ArrayList<WorkflowResponseItem> items = new ArrayList<>();
		WorkflowResponseItem requiredHeading = new WorkflowResponseItem();		
		requiredHeading.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL);
		requiredHeading.setContent("Required Attributes");
		requiredHeading.setId("requiredHeading");
		items.add(requiredHeading);
		
		// id is required
		WorkflowResponseItem idItem = new WorkflowResponseItem();
		idItem.setId("id");
		idItem.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL);
		idItem.setLabel("Server Name");
		WorkflowPromptDetails idPrompt = new WorkflowPromptDetails();
		idPrompt.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_STRING);
		idItem.setPrompt(idPrompt);
		items.add(idItem);
		
		Attributes required = getRequiredAttributes();
		Map<String,Attribute> reqMap = required.getAttributes();
		Iterator<String> it = reqMap.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			Attribute at = reqMap.get(key);
			items.add(attributeToWorkflowItem(key, at));
		}

		
		WorkflowResponseItem optionalHeading = new WorkflowResponseItem();		
		optionalHeading.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL);
		optionalHeading.setContent("Optional Attributes");
		optionalHeading.setId("optionalHeading");
		items.add(optionalHeading);
		
		Attributes optional = getOptionalAttributes();
		Map<String,Attribute> optMap = optional.getAttributes();
		Iterator<String> it2 = optMap.keySet().iterator();
		while(it2.hasNext()) {
			String key = it2.next();
			Attribute at = optMap.get(key);
			items.add(attributeToWorkflowItem(key, at));
		}

		workflow.setItems(items);
		// The ok-status indicates the workflow is done and the results can be submitted
		workflow.setStatus(StatusConverter.convert(new Status(IStatus.INFO, SPIActivator.BUNDLE_ID, "")));
		return workflow;
	}
	
	public ServerLaunchMode[] getLaunchModes() {
		return new ServerLaunchMode[] {};
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return desc;
	}
}
