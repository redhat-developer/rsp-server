/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype;

import java.io.File;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
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
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.launching.utils.StatusConverter;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IStartLauncher;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.model.polling.PollThreadUtils;
import org.jboss.tools.rsp.server.spi.model.polling.WebPortPoller;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJBossServerDelegate extends AbstractServerDelegate {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractJBossServerDelegate.class);

	private ILaunch startLaunch;
	
	public AbstractJBossServerDelegate(IServer server) {
		super(server);
	}

	protected abstract IStartLauncher getStartLauncher();
	
	protected abstract IShutdownLauncher getStopLauncher();

	protected abstract String getPollURL(IServer server);
	
	@Override
	public CreateServerValidation validate() {
		String home = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		
		if( null == home ) {
			return validationErrorResponse("Server home must not be null", IJBossServerAttributes.SERVER_HOME, Activator.BUNDLE_ID);
		}
		if(!(new File(home).exists())) {
			return validationErrorResponse("Server home must exist", IJBossServerAttributes.SERVER_HOME, Activator.BUNDLE_ID);
		}
		
		IVMInstall vmi = new JBossVMRegistryDiscovery().findVMInstall(this);
		if( vmi == null ) {
			String msg = "Server " + getServer().getId() + " can not find a valid virtual machine to use.";
			return validationErrorResponse(msg, null, Activator.BUNDLE_ID);
		}
		return new CreateServerValidation(Status.OK_STATUS, null);
	}

	public IStatus canStart(String launchMode) {
		if( !modesContains(launchMode)) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID,
					"Server may not be launched in mode " + launchMode);
		}
		if( getServerState() == IServerDelegate.STATE_STOPPED ) {
			IStatus v = validate().getStatus();
			if( !v.isOK() )
				return v;
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}
	
	private boolean modesContains(String needle) {
		ServerLaunchMode[] modes = getServer().getServerType().getLaunchModes();
		for( int i = 0; i < modes.length; i++ ) {
			if( modes[i].getMode().equals(needle)) {
				return true;
			}
		}
		return false;
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
			launchPoller(IServerStatePoller.SERVER_STATE.UP);
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
		launchPoller(IServerStatePoller.SERVER_STATE.DOWN);
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
		PollThreadUtils.pollServer(getServer(), expectedState, poller, listener);
	}
	
	/*
	 * Default implementation, subclasses can override.
	 */
	protected IServerStatePoller getPoller(IServerStatePoller.SERVER_STATE expectedState) {
		return getDefaultWebPortPoller();
	}
	
	private IServerStatePoller getDefaultWebPortPoller() {
		return new WebPortPoller("Web Poller: " + this.getServer().getName()) {
			@Override
			protected String getURL(IServer server) {
				return getPollURL(server);
			}
		};
	}

	@Override
	protected void processTerminated(IProcess p, ILaunch l) {
		if( l == startLaunch ) {
			IProcess[] all = l.getProcesses();
			boolean allTerminated = true;
			for( int i = 0; i < all.length; i++ ) {
				allTerminated &= all[i].isTerminated();
			}
			if( allTerminated ) {
				setServerState(IServerDelegate.STATE_STOPPED);
				startLaunch = null;
			}
		}
		fireServerProcessTerminated(getProcessId(p));
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
}
