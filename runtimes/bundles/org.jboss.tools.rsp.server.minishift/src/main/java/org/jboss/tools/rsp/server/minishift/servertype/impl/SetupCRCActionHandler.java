/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class SetupCRCActionHandler {
	
	public static final String ACTION_SETUP_CRC_ID = "CRCServerDelegate.setupCRC";
	private static final String ACTION_SETUP_CRC_LABEL = "Run setup-crc";	

	public static final ServerActionWorkflow getInitialWorkflow(CRCServerDelegate crcServerDelegate) {
		return new SetupCRCActionHandler(crcServerDelegate).getInitialWorkflowInternal();
	}
	

	private CRCServerDelegate crcServerDelegate;
	public SetupCRCActionHandler(CRCServerDelegate crcServerDelegate) {
		this.crcServerDelegate = crcServerDelegate;		
	}
	
	protected ServerActionWorkflow getInitialWorkflowInternal() {
		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_SETUP_CRC_ID, ACTION_SETUP_CRC_LABEL, workflow);
		
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);
		
		// Simple action entirely on the UI side
		String crcPath = this.crcServerDelegate.getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_FILE, "crc");
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_TERMINAL);
		Map<String,String> propMap = new HashMap<>();
		propMap.put(ServerManagementAPIConstants.WORKFLOW_TERMINAL_CMD, crcPath + " setup");
		item1.setProperties(propMap);
		item1.setId(ACTION_SETUP_CRC_ID);
		item1.setLabel(ACTION_SETUP_CRC_LABEL);
		
		items.add(item1);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.OK, Activator.BUNDLE_ID, ACTION_SETUP_CRC_LABEL)));
		return action;
	}
	
	public WorkflowResponse handle(ServerActionRequest req) {
		return null;
	}
}
