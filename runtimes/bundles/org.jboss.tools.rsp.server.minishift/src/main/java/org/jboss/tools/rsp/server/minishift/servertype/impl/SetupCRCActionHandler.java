package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class SetupCRCActionHandler {

	public static final ServerActionWorkflow getInitialWorkflow() {
		return new SetupCRCActionHandler().getInitialWorkflowInternal();
	}
	
	protected ServerActionWorkflow getInitialWorkflowInternal() {
		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				CRCServerDelegate.ACTION_SETUP_CRC_ID, CRCServerDelegate.ACTION_SETUP_CRC_LABEL, workflow);
		
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);
		
		// Simple action entirely on the UI side
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_TERMINAL);
		Map<String,String> propMap = new HashMap<>();
		propMap.put(ServerManagementAPIConstants.WORKFLOW_TERMINAL_CMD, "crc setup");
		item1.setProperties(propMap);
		item1.setId(CRCServerDelegate.ACTION_SETUP_CRC_ID);
		item1.setLabel(CRCServerDelegate.ACTION_SETUP_CRC_LABEL);
		
		items.add(item1);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.OK, Activator.BUNDLE_ID, CRCServerDelegate.ACTION_SETUP_CRC_LABEL)));
		return action;
	}
}
