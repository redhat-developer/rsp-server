package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.StringPrompt;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.secure.crypto.CryptoException;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
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
		try {
			ILaunch launch = new SetupCRCLauncher(this).launch("run");
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
	
	protected IServerStartLauncher getStartLauncher() {
		return new StartCRCLauncher(this);
	}
	
	@Override
	public void setDefaults(IServerWorkingCopy server) {
		server.setAttribute(STARTUP_PROGRAM_ARGS_STRING, "--pull-secret-file");
		HashMap<String,String> tmp = new HashMap<>();
		server.setAttribute(STARTUP_ENV_VARS_MAP, tmp);
	}
	
	public String getPullSecret() {
		// This step should only be done if this createServerDelegate is called
		// as part of a user workflow in the context of a call from a client.
		RSPClient rspc = ClientThreadLocal.getActiveClient();
		if( rspc == null )
			return null;
				
		try {
			String msg = "Please provide the path where the image pull secret is stored."; 
			StringPrompt prompt = new StringPrompt(100, msg, true);
			int tries = 0;
			int maxTries = 10;
			while(tries < maxTries) {
				String pullSecret = rspc.promptString(prompt).get();
				if( pullSecret != null && pullSecret.length() != 0 && pullSecret.trim().length() != 0) {
					return pullSecret;
				}
				tries += 1;
			}			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
}
