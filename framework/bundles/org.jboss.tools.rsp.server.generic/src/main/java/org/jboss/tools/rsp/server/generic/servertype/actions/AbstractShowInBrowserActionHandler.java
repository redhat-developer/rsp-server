/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowPromptDetails;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.generic.impl.Activator;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public abstract class AbstractShowInBrowserActionHandler {
	public static final String ACTION_SHOW_IN_BROWSER_JSON_ID = "showInBrowser";
	public static final String ACTION_SHOW_IN_BROWSER_ID = "ShowInBrowserActionHandler.actionId";
	public static final String ACTION_SHOW_IN_BROWSER_LABEL = "Show in browser...";
	public static final String ACTION_SHOW_IN_BROWSER_SELECTED_PROMPT_ID = "ShowInBrowserActionHandler.selection.id";
	public static final String ACTION_SHOW_IN_BROWSER_SELECTED_PROMPT_LABEL = 
			"Which deployment do you want to show in the web browser?";
	
	private IServerDelegate serverDelegate;
	public AbstractShowInBrowserActionHandler(IServerDelegate genericServerBehavior) {
		this.serverDelegate = genericServerBehavior;
	}
	
	protected abstract String[] getDeploymentUrls(DeployableState ds);
	
	protected abstract String getBaseUrl();

	public ServerActionWorkflow getInitialWorkflow() {
		return getInitialWorkflowInternal();
	}
	
	protected ServerActionWorkflow getInitialWorkflowInternal() {
		WorkflowResponse workflow = new WorkflowResponse();
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_SHOW_IN_BROWSER_LABEL)));
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_SHOW_IN_BROWSER_ID, ACTION_SHOW_IN_BROWSER_LABEL, workflow);
		
		// Initial prompt 
		List<WorkflowResponseItem> items = new ArrayList<>();
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL);
		item1.setId(ACTION_SHOW_IN_BROWSER_SELECTED_PROMPT_ID);
		item1.setLabel(ACTION_SHOW_IN_BROWSER_SELECTED_PROMPT_LABEL);
		
		WorkflowPromptDetails prompt = new WorkflowPromptDetails();
		prompt.setResponseSecret(false);
		prompt.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_STRING);
		
		List<String> urls = getDeploymentUrls();
		
		List<String> deployments2 = new ArrayList<>();
		if( !urls.contains(getBaseUrl()))
			deployments2.add(getBaseUrl());
		deployments2.addAll(urls);
		
		prompt.setValidResponses(deployments2);
		item1.setPrompt(prompt);
		
		items.add(item1);
		workflow.setItems(items);
		return action;
	}
	
	private List<String> getDeploymentUrls() {
		List<DeployableState> dss = getDeployableStates();
		ArrayList<String> collector = new ArrayList<>();
		for( DeployableState ds : dss ) {
			String[] urls = getDeploymentUrls(ds);
			if( urls != null ) 
				collector.addAll(Arrays.asList(urls));
		}
		return collector;
	}
	
	protected List<DeployableState> getDeployableStates() {
		return serverDelegate.getServerPublishModel().getDeployableStatesWithOptions();
	}

	protected String getOutputName(DeployableReference ref) {
		Map<String, Object> options = ref.getOptions();
		String def = null;
		if( ref.getPath() != null ) {
			def = new File(ref.getPath()).getName();
		}
		String k = ServerManagementAPIConstants.DEPLOYMENT_OPTION_OUTPUT_NAME; 
		if( options != null && options.get(k) != null ) {
			return (String)options.get(k);
		}
		return def;
	}
	
	public WorkflowResponse handle(ServerActionRequest req) {
		if( req == null || req.getData() == null ) 
			return AbstractServerDelegate.cancelWorkflowResponse();
			
		String choice = (String)req.getData().get(ACTION_SHOW_IN_BROWSER_SELECTED_PROMPT_ID);
		if( choice == null )
			return AbstractServerDelegate.cancelWorkflowResponse();
		
		String url = findUrlFromChoice(choice);
		if( url != null ) {
			WorkflowResponseItem item = new WorkflowResponseItem();
			item.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_BROWSER);
			item.setLabel("Open the following url: " + url);
			item.setContent(url);
			WorkflowResponse resp = new WorkflowResponse();
			resp.setItems(Arrays.asList(item));
			resp.setStatus(StatusConverter.convert(Status.OK_STATUS));
			return resp;
		}
		return AbstractServerDelegate.cancelWorkflowResponse();
	}
	
	private String findUrlFromChoice(String choice) {
		// We can accept any url they type here I guess
		if( choice.toLowerCase().startsWith("http://") || choice.toLowerCase().startsWith("https://")) { 
			return choice;
		}
		return null;
	}
	
}
