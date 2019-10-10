package org.jboss.tools.rsp.server.wildfly.servertype.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;

public class EditServerConfigurationActionHandler {
	public static final String ACTION_ID = "EditServerConfigurationActionHandler.actionId";
	public static final String ACTION_LABEL = "Edit Configuration File...";

	public static final ServerActionWorkflow getInitialWorkflow(WildFlyServerDelegate wildFlyServerDelegate2) {
		return new EditServerConfigurationActionHandler(wildFlyServerDelegate2).getInitialWorkflowInternal();
	}


	private WildFlyServerDelegate wildFlyServerDelegate;
	public EditServerConfigurationActionHandler(WildFlyServerDelegate wildFlyServerDelegate) {
		this.wildFlyServerDelegate = wildFlyServerDelegate;
	}
	
	protected ServerActionWorkflow getInitialWorkflowInternal() {
		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_ID, ACTION_LABEL, workflow);
		
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);

		String configFilePath = getConfigurationFile();
		if( !(new File(configFilePath).exists())) {
			workflow.setStatus(StatusConverter.convert(
					new Status(IStatus.CANCEL, Activator.BUNDLE_ID, ACTION_LABEL)));
			return action;
		}
		
		// Simple action entirely on the UI side
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_EDITOR);
		Map<String,String> propMap = new HashMap<>();
		propMap.put(ServerManagementAPIConstants.WORKFLOW_EDITOR_PROPERTY_PATH, configFilePath);
		item1.setProperties(propMap);
		item1.setId(ACTION_ID);
		item1.setLabel(ACTION_LABEL);
		
		items.add(item1);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.OK, Activator.BUNDLE_ID, ACTION_LABEL)));
		return action;
	}

	protected String getConfigurationFile() {
		// this may need changing if we allow them to set their configuration folder
		// but for now it is ok
		String home = wildFlyServerDelegate.getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		String configFile = wildFlyServerDelegate.getServer().getAttribute(IJBossServerAttributes.WILDFLY_CONFIG_FILE, 
				IJBossServerAttributes.WILDFLY_CONFIG_FILE_DEFAULT);
		IPath configFilePath = new Path(home).append("standalone").append("configuration").append(configFile);
		return configFilePath.toOSString();
	}

	public WorkflowResponse handle(ServerActionRequest req) {
		if( req == null || req.getData() == null ) 
			return AbstractServerDelegate.okWorkflowResponse();
		return null;
	}
	
}
