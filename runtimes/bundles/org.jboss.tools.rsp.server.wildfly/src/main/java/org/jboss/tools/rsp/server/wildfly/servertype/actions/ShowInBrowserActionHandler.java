package org.jboss.tools.rsp.server.wildfly.servertype.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;

public class ShowInBrowserActionHandler {
	public static final String ACTION_SHOW_IN_BROWSER_ID = "ShowInBrowserActionHandler.actionId";
	public static final String ACTION_SHOW_IN_BROWSER_LABEL = "Show in browser...";
	public static final String ACTION_SHOW_IN_BROWSER_SELECTED_PROMPT_ID = "ShowInBrowserActionHandler.selection.id";
	public static final String ACTION_SHOW_IN_BROWSER_SELECTED_PROMPT_LABEL = 
			"Which deployment do you want to show in the web browser?";
	public static final String ACTION_SHOW_IN_BROWSER_SELECT_SERVER_ROOT = "Show server's root page.";

	public static final ServerActionWorkflow getInitialWorkflow(WildFlyServerDelegate wildFlyServerDelegate2) {
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
		
		List<String> deployments = wildFlyServerDelegate2.getServerPublishModel().getDeployableStates()
		.stream().map(DeployableState::getReference).map(DeployableReference::getPath)
		.collect(Collectors.toList());
		
		List<String> deployments2 = new ArrayList<>();
		deployments2.add(ACTION_SHOW_IN_BROWSER_SELECT_SERVER_ROOT);
		deployments2.addAll(deployments);
		
		prompt.setValidResponses(deployments2);
		item1.setPrompt(prompt);
		
		items.add(item1);
		workflow.setItems(items);
		return action;
	}
	
	
	private WildFlyServerDelegate wildFlyServerDelegate;
	public ShowInBrowserActionHandler(WildFlyServerDelegate wildFlyServerDelegate) {
		this.wildFlyServerDelegate = wildFlyServerDelegate;
	}

	public WorkflowResponse handle(ServerActionRequest req) {
		String choice = (String)req.getData().get(ACTION_SHOW_IN_BROWSER_SELECTED_PROMPT_ID);
		if( choice == null ) {
			return AbstractServerDelegate.cancelWorkflowResponse();
		}
		String url = null;
		if( choice.equals(ACTION_SHOW_IN_BROWSER_SELECT_SERVER_ROOT)) {
			url = wildFlyServerDelegate.getPollURL(wildFlyServerDelegate.getServer());
		} else {
			List<DeployableState> states = wildFlyServerDelegate.getServerPublishModel().getDeployableStates();
			for( DeployableState ds : states ) {
				if( ds.getReference().getPath().equals(choice)) {
					// TODO figure out the context root for this deployment?!
				}
			}
		}
		
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

}
