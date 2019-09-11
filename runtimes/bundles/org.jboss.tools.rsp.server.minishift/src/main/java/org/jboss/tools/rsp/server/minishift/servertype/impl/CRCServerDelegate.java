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

public class CRCServerDelegate extends MinishiftServerDelegate {
	private static final String ACTION_SETUP_CRC_ID = "CRCServerDelegate.setupCRC";
	private static final String ACTION_SETUP_CRC_LABEL = "Run setup-crc";
	
	
	public CRCServerDelegate(IServer server) {
		super(server);
	}
	
	@Override
	protected void fillActionList(List<ServerActionWorkflow> allActions) {
		super.fillActionList(allActions);

		// setup-crc
		WorkflowResponse setupCrcWorkflow = new WorkflowResponse();
		setupCrcWorkflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_SETUP_CRC_LABEL)));
		ServerActionWorkflow setupCrcAction = new ServerActionWorkflow(
				ACTION_SETUP_CRC_ID, ACTION_SETUP_CRC_LABEL, setupCrcWorkflow);
		allActions.add(setupCrcAction);
	}
	
	@Override
	public WorkflowResponse executeServerAction(ServerActionRequest req) {
		if( req != null && ACTION_SETUP_CRC_ID.equals(req.getActionId() )) {
			return runSetupCrc(req);
		}
		return super.executeServerAction(req);
	}
	
	protected WorkflowResponse runSetupCrc(ServerActionRequest req) {
		return okWorkflowResponse();
		/*try {
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
		}*/
	}
}
