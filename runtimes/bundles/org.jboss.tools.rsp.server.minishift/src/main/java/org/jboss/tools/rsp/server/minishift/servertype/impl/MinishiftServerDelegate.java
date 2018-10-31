/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.io.File;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.launching.utils.StatusConverter;
import org.jboss.tools.rsp.server.minishift.discovery.MinishiftDiscovery;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IStartLauncher;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.SERVER_STATE;
import org.jboss.tools.rsp.server.spi.model.polling.PollThreadUtils;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinishiftServerDelegate extends AbstractServerDelegate {
	private static final Logger LOG = LoggerFactory.getLogger(MinishiftServerDelegate.class);

	private ILaunch startLaunch;
	public MinishiftServerDelegate(IServer server) {
		super(server);
		setServerState(ServerManagementAPIConstants.STATE_STOPPED);
	}
	protected IStartLauncher getStartLauncher() {
		return new MinishiftStartLauncher(this);
	}
	
	protected IShutdownLauncher getStopLauncher() {
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
				stateString = "started";break;
			case IServerDelegate.STATE_STARTING:
				stateString = "starting";break;
			case IServerDelegate.STATE_STOPPED:
				stateString = "stopped";break;
			case IServerDelegate.STATE_STOPPING:
				stateString = "stopping";break;
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
		
		setServerState(IServerDelegate.STATE_STARTING);
		CommandLineDetails launchedDetails = null;
		try {
			IStartLauncher launcher = getStartLauncher();
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
		
		// Time to poll to check the state
		IServerStatePoller poller = getMinishiftStatusPoller();
		SERVER_STATE state = poller.getCurrentStateSynchronous(getServer());
		if( state == SERVER_STATE.UP) {
			setServerState(IServerDelegate.STATE_STARTED);
		} else {
			setServerState(IServerDelegate.STATE_STOPPED);
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
	public IStatus canAddDeployable(DeployableReference reference) {
		return Status.CANCEL_STATUS;
	}
	
	@Override
	public IStatus canRemoveDeployable(DeployableReference reference) {
		return Status.CANCEL_STATUS;
	}

}
