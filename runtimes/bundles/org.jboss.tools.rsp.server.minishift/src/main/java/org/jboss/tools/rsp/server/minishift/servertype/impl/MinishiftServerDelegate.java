/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.api.dao.WorkflowPromptDetails;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.server.minishift.discovery.MinishiftDiscovery;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.SERVER_STATE;
import org.jboss.tools.rsp.server.spi.model.polling.PollThreadUtils;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinishiftServerDelegate extends AbstractServerDelegate {
	public static final String STARTUP_PROGRAM_ARGS_STRING = "startup.progargs.append.string";
	public static final String STARTUP_ENV_VARS_MAP = "startup.envvars.map";
	
	public static final String ACTION_MINISHIFT_COMMAND_LABEL = "Run Command with arguments...";
	public static final String ACTION_MINISHIFT_COMMAND_ID = "action.minishift.command.run.args";
	public static final String ACTION_MINISHIFT_COMMAND_FIELD_ID = ACTION_MINISHIFT_COMMAND_ID + ".field";
	
	public static final String ACTION_SERVICE_CATALOG_COMMAND_LABEL = "Add Service Catalog";
	public static final String ACTION_SERVICE_CATALOG_COMMAND_ID = "action.minishift.command.run.servicecatalog.add";
	
	private static final Logger LOG = LoggerFactory.getLogger(MinishiftServerDelegate.class);

	private ILaunch startLaunch;
	public MinishiftServerDelegate(IServer server) {
		super(server);
		setServerState(ServerManagementAPIConstants.STATE_STOPPED);
	}
	protected IServerStartLauncher getStartLauncher() {
		return new MinishiftStartLauncher(this);
	}
	
	protected IServerShutdownLauncher getStopLauncher() {
		return new MinishiftStopLauncher(this);
	}
	
	@Override
	public CreateServerValidation validate() {
		String bin = getServer().getAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, (String)null);
		
		if( null == bin ) {
			return validationErrorResponse("Minishift binary location must not be null", IMinishiftServerAttributes.MINISHIFT_BINARY, Activator.BUNDLE_ID);
		}
		File fBin = new File(bin);
		if(!fBin.exists())
			return validationErrorResponse("Minishift binary location must exist", IMinishiftServerAttributes.MINISHIFT_BINARY, Activator.BUNDLE_ID);

		if(!fBin.isFile())
			return validationErrorResponse("Minishift binary location must not be a directory.", IMinishiftServerAttributes.MINISHIFT_BINARY, Activator.BUNDLE_ID);

		MinishiftDiscovery discovery = new MinishiftDiscovery();
		if( !discovery.isMinishiftBinaryFile(fBin)) {
			return validationErrorResponse("Provided path is not a Minishift binary file: " + bin, IMinishiftServerAttributes.MINISHIFT_BINARY, Activator.BUNDLE_ID);
		}
		return new CreateServerValidation(Status.OK_STATUS, null);
	}

	@Override
	public IStatus canStart(String launchMode) {
		if( !modesContains(launchMode)) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID,
					"Server may not be launched in mode " + launchMode);
		}
		if( getServerRunState() == IServerDelegate.STATE_STOPPED ) {
			IStatus v = validate().getStatus();
			if( !v.isOK() )
				return v;
			return Status.OK_STATUS;
		} else {
			String stateString = null;
			switch(getServerRunState()) {
			case IServerDelegate.STATE_STARTED:
				stateString = "started";
				break;
			case IServerDelegate.STATE_STARTING:
				stateString = "starting";
				break;
			case IServerDelegate.STATE_STOPPED:
				stateString = "stopped";
				break;
			case IServerDelegate.STATE_STOPPING:
				stateString = "stopping";
				break;
			}
			return new Status(IStatus.CANCEL, Activator.BUNDLE_ID,
					"Server cannot be started. It is in state " + stateString);
		}
	}
	
	@Override
	public StartServerResponse start(String mode) {
		IStatus stat = canStart(mode);
		if( !stat.isOK()) {
			org.jboss.tools.rsp.api.dao.Status s = StatusConverter.convert(stat);
			return new StartServerResponse(s, null);
		}
		
		setMode(mode);
		setServerState(IServerDelegate.STATE_STARTING);
		
		CommandLineDetails launchedDetails = null;
		try {
			IServerStartLauncher launcher = getStartLauncher();
			startLaunch = launcher.launch(mode);
			launchedDetails = launcher.getLaunchedDetails();
			registerLaunch(startLaunch);
		} catch(CoreException ce) {
			if( startLaunch != null ) {
				IProcess[] processes = startLaunch.getProcesses();
				for( int i = 0; i < processes.length; i++ ) {
					try {
						processes[i].terminate();
					} catch(DebugException de) {
						LOG.error(de.getMessage(), de);
					}
				}
			}
			setServerState(IServerDelegate.STATE_STOPPED);
			org.jboss.tools.rsp.api.dao.Status s = StatusConverter.convert(ce.getStatus());
			return new StartServerResponse(s, launchedDetails);
		}
		return new StartServerResponse(StatusConverter.convert(Status.OK_STATUS), launchedDetails);
	}

	
	@Override
	public IStatus stop(boolean force) {
		setServerState(IServerDelegate.STATE_STOPPING);
		ILaunch stopLaunch = null;
		//launchPoller(IServerStatePoller.SERVER_STATE.DOWN);
		try {
			stopLaunch = getStopLauncher().launch(force);
			if( stopLaunch != null)
				registerLaunch(stopLaunch);
		} catch(CoreException ce) {
			// Dead code... but I feel it's not dead?  idk :( 
//			if( stopLaunch != null ) {
//				IProcess[] processes = startLaunch.getProcesses();
//				for( int i = 0; i < processes.length; i++ ) {
//					try {
//						processes[i].terminate();
//					} catch(DebugException de) {
//						LaunchingCore.log(de);
//					}
//				}
//			}
			setServerState(IServerDelegate.STATE_STARTED);
			return ce.getStatus();
		}
		return Status.OK_STATUS;

	}
	
	protected void launchPoller(IServerStatePoller.SERVER_STATE expectedState) {
		IPollResultListener listener = expectedState == IServerStatePoller.SERVER_STATE.DOWN ? 
				shutdownServerResultListener() : launchServerResultListener();
		IServerStatePoller poller = getPoller(expectedState);
		// 5 minute timeout
		PollThreadUtils.pollServer(getServer(), expectedState, poller, listener,5*60*1000);
	}
	
	/*
	 * Default implementation, subclasses can override.
	 */
	protected IServerStatePoller getPoller(IServerStatePoller.SERVER_STATE expectedState) {
		return getMinishiftStatusPoller();
	}
	
	private IServerStatePoller getMinishiftStatusPoller() {
		IServerStatePoller poller = new MinishiftStatusPoller();
		return poller;
	}

	@Override
	protected void processTerminated(IProcess p) {
		// The launch command will terminate but that just means startup has completed.
		// Not that the runtime has shutdown.
		fireServerProcessTerminated(getProcessId(p));
		
		// Because this was a background thread, we must elevate permissions.
		// This thread is not associated with any specific client.
		ISecureStorageProvider storage = getServer().getServerManagementModel().getSecureStorageProvider();
		boolean hasPerms = storage.currentThreadHasSystemPermissions();
		if( !hasPerms ) {
			storage.grantCurrentThreadSystemPermissions();
		}
		
		// Time to poll to check the state
		IServerStatePoller poller = getMinishiftStatusPoller();
		SERVER_STATE state = poller.getCurrentStateSynchronous(getServer());
		if( state == SERVER_STATE.UP) {
			setServerState(IServerDelegate.STATE_STARTED);
		} else {
			setMode(null);
			setServerState(IServerDelegate.STATE_STOPPED);
		}
		
		// Don't forget to revoke the permissions if we were the first one to grant it
		if( !hasPerms ) {
			storage.revokeCurrentThreadSystemPermissions();
		}
	}

	@Override
	public CommandLineDetails getStartLaunchCommand(String mode, ServerAttributes params) {
		try {
			return getStartLauncher().getLaunchCommand(mode);
		} catch(CoreException ce) {
			LOG.error(ce.getMessage(), ce);
			return null;
		}
	}

	@Override
	public IStatus clientSetServerStarting(ServerStartingAttributes attr) {
		setServerState(STATE_STARTING, true);
		if( attr.isInitiatePolling()) {
			launchPoller(IServerStatePoller.SERVER_STATE.UP);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus clientSetServerStarted(LaunchParameters attr) {
		setServerState(STATE_STARTED, true);
		return Status.OK_STATUS;
	}
	
	
	/**
	 * This server type can't publish nothin yet!
	 */
	
	@Override
	public IStatus canAddDeployable(DeployableReference req) {
		return Status.CANCEL_STATUS;
	}
	
	@Override
	public IStatus canRemoveDeployable(DeployableReference reference) {
		return Status.CANCEL_STATUS;
	}
	@Override
	public void updateServer(IServer dummyServer, UpdateServerResponse resp) {
		// Do nothing
	}
	@Override
	public void setDefaults(IServerWorkingCopy server) {
		server.setAttribute(STARTUP_PROGRAM_ARGS_STRING, "start");
		server.setAttribute(STARTUP_ENV_VARS_MAP, new HashMap<>());
	}
	
	@Override
	public ListServerActionResponse listServerActions() {
		ListServerActionResponse ret = new ListServerActionResponse();
		ret.setStatus(StatusConverter.convert(Status.OK_STATUS));
		List<ServerActionWorkflow> allActions = new ArrayList<>();
		fillActionList(allActions);
		ret.setWorkflows(allActions);
		return ret;
	}
	
	protected void fillActionList(List<ServerActionWorkflow> allActions) {
		if( ServerManagementAPIConstants.STATE_STARTED == getServerState().getState()) {
			addGenericMinishiftCommandAction(allActions, ACTION_SERVICE_CATALOG_COMMAND_ID, ACTION_SERVICE_CATALOG_COMMAND_LABEL);
			addArbitraryMinishiftCommandAction(allActions);
		}
	}
	
	protected void addGenericMinishiftCommandAction(List<ServerActionWorkflow> allActions,
			String id, String label) {
		WorkflowResponse workflow = new WorkflowResponse();
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, label)));
		ServerActionWorkflow action = new ServerActionWorkflow(
				id, label, workflow);
		workflow.setItems(new ArrayList<>());
		allActions.add(action);
	}
	
	protected void addArbitraryMinishiftCommandAction(List<ServerActionWorkflow> allActions) {
		// minishift command.  ex:  openshift component add service-catalog
		WorkflowResponse runCommandWorkflow = new WorkflowResponse();
		runCommandWorkflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_MINISHIFT_COMMAND_LABEL)));
		ServerActionWorkflow runCommandAction = new ServerActionWorkflow(
				ACTION_MINISHIFT_COMMAND_ID, ACTION_MINISHIFT_COMMAND_LABEL, runCommandWorkflow);
		List<WorkflowResponseItem> items = new ArrayList<>();
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setId(ACTION_MINISHIFT_COMMAND_FIELD_ID);
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL);
		item1.setLabel("Please type the command line arguments. ex: openshift component add service-catalog");
		WorkflowPromptDetails prompt = new WorkflowPromptDetails();
		prompt.setResponseSecret(false);
		prompt.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_STRING);
		item1.setPrompt(prompt);
		items.add(item1);
		runCommandWorkflow.setItems(items);
		allActions.add(runCommandAction);
	}
	
	@Override
	public WorkflowResponse executeServerAction(ServerActionRequest req) {
		if( req != null ) {
			if( ACTION_MINISHIFT_COMMAND_ID.equals(req.getActionId() ))
				return runArbitraryMinishiftCommand(req);
			if( ACTION_SERVICE_CATALOG_COMMAND_ID.equals(req.getActionId()))
				return runMinishiftCommand(req, "openshift component add service-catalog");
		}
		return cancelWorkflowResponse();
	}
	protected WorkflowResponse runArbitraryMinishiftCommand(ServerActionRequest req) {
		String args = (String)req.getData().get(ACTION_MINISHIFT_COMMAND_FIELD_ID);
		return runMinishiftCommand(req, args);
	}
	
	protected WorkflowResponse runMinishiftCommand(ServerActionRequest req, String args) {
		try {
			ILaunch launch = new MinishiftCommandLauncher(this, args).launch("run");
			registerLaunch(launch);
			return okWorkflowResponse();
		} catch(CoreException ce) {
			WorkflowResponse resp = new WorkflowResponse();
			resp.setStatus(StatusConverter.convert(new Status(
					IStatus.ERROR, Activator.BUNDLE_ID, 
					"Error running server command: " + ce.getMessage(), ce)));
			resp.setItems(new ArrayList<>());
			return resp;
		}
	}

}
