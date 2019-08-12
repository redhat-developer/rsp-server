/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class CDKServerDelegate extends MinishiftServerDelegate {
	private static final String ACTION_SETUP_CDK_ID = "CDKServerDelegate.setupCDK";
	private static final String ACTION_SETUP_CDK_LABEL = "Run setup-cdk";
	
	
	public CDKServerDelegate(IServer server) {
		super(server);
	}
	@Override
	protected void fillActionList(List<ServerActionWorkflow> allActions) {
		super.fillActionList(allActions);

		// setup-cdk
		WorkflowResponse setupCdkWorkflow = new WorkflowResponse();
		setupCdkWorkflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_SETUP_CDK_LABEL)));
		ServerActionWorkflow setupCdkAction = new ServerActionWorkflow(
				ACTION_SETUP_CDK_ID, ACTION_SETUP_CDK_LABEL, setupCdkWorkflow);
		allActions.add(setupCdkAction);
	}
	
	@Override
	public WorkflowResponse executeServerAction(ServerActionRequest req) {
		if( req != null && ACTION_SETUP_CDK_ID.equals(req.getActionId() )) {
			return runSetupCdk(req);
		}
		return super.executeServerAction(req);
	}
	
	protected WorkflowResponse runSetupCdk(ServerActionRequest req) {
		try {
			ILaunch launch = new SetupCDKLauncher(this).launch("run");
			registerLaunch(launch);
			return okWorkflowResponse();
		} catch(CoreException ce) {
			WorkflowResponse resp = new WorkflowResponse();
			resp.setStatus(StatusConverter.convert(new Status(
					IStatus.ERROR, Activator.BUNDLE_ID, 
					"Error running setup-cdk: " + ce.getMessage(), ce)));
			resp.setItems(new ArrayList<>());
			return resp;
		}
	}
}
