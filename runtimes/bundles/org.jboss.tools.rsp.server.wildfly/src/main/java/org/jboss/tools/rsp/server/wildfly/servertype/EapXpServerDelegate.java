/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.EapXpCommandLineStartLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EapXpServerDelegate extends AbstractServerDelegate {
	public static final String START_LAUNCH_SHARED_DATA = "EapXpServerDelegate.startLaunch";
	private static final Logger LOG = LoggerFactory.getLogger(EapXpServerDelegate.class);

	public EapXpServerDelegate(IServer server) {
		super(server);
	}

	@Override
	public IStatus canPublish() {
		return Status.CANCEL_STATUS;
	}

	@Override
	public IStatus canAddDeployable(DeployableReference ref) {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus canRemoveDeployable(DeployableReference ref) {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus canStart(String launchMode) {
		if (!modesContains(launchMode)) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Server may not be launched in mode " + launchMode);
		}
		String javaCompatError = getJavaCompatibilityError();
		if (javaCompatError != null) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Server can not be started: " + javaCompatError);
		}
		if (getServerRunState() == IServerDelegate.STATE_STOPPED || getServerRunState() == IServerDelegate.STATE_UNKNOWN) {
			IStatus v = validate().getStatus();
			if (!v.isOK())
				return v;
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}

	public String getJavaCompatibilityError() {
		IVMInstall vmi = new JBossVMRegistryDiscovery().findVMInstall(this);
		if (vmi == null) {
			String msg = "Server {0} can not find a valid virtual machine to use.";
			return NLS.bind(msg, getServer().getId());
		}
		return null;
	}

	@Override
	public StartServerResponse start(String mode) {
		IStatus stat = canStart(mode);
		if (!stat.isOK()) {
			org.jboss.tools.rsp.api.dao.Status s = StatusConverter.convert(stat);
			return new StartServerResponse(s, null);
		}

		setMode(mode);
		setServerState(IServerDelegate.STATE_STARTING);

		CommandLineDetails launchedDetails = null;
		try {
			IServerStartLauncher launcher = getStartLauncher();
			ILaunch startLaunch2 = launcher.launch(mode);
			launchedDetails = launcher.getLaunchedDetails();
			setStartLaunch(startLaunch2);
			registerLaunch(startLaunch2);
			setServerState(IServerDelegate.STATE_STARTED);
		} catch (CoreException ce) {
			if (getStartLaunch() != null) {
				IProcess[] processes = getStartLaunch().getProcesses();
				for (int i = 0; i < processes.length; i++) {
					try {
						processes[i].terminate();
					} catch (DebugException de) {
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
		try {
			stopLaunch = getStopLauncher().launch(force);
			if (stopLaunch != null)
				registerLaunch(stopLaunch);
		} catch (CoreException ce) {
			if (!force)
				setServerState(IServerDelegate.STATE_STARTED);
			return ce.getStatus();
		} finally {
			setServerState(IServerDelegate.STATE_STOPPED);
		}
		return Status.OK_STATUS;

	}

	protected ILaunch getStartLaunch() {
		return (ILaunch) getSharedData(START_LAUNCH_SHARED_DATA);
	}

	protected void setStartLaunch(ILaunch launch) {
		putSharedData(START_LAUNCH_SHARED_DATA, launch);
	}

	protected IServerStartLauncher getStartLauncher() {
		return new EapXpCommandLineStartLauncher(this, this.getMode());
	}

	protected IServerShutdownLauncher getStopLauncher() {
		final ILaunch startLaunch = (ILaunch)getSharedData(START_LAUNCH_SHARED_DATA);
		return new IServerShutdownLauncher() {
			@Override
			public ILaunch launch(boolean force) throws CoreException {
				terminateAllProcesses(startLaunch);
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ILaunch getLaunch() {
				// TODO Auto-generated method stub
				return null;
			}
																																																											
			@Override
			public IServer getServer() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	private void terminateAllProcesses(ILaunch launch) {
		if( launch != null ) {
			IProcess[] processes = launch.getProcesses();
			for( int i = 0; i < processes.length; i++ ) {
				if( !processes[i].isTerminated() && processes[i].canTerminate()) {
					try {
						processes[i].terminate();
					} catch( DebugException de) {
						// ignore
					}
				}
			}
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
	
}

