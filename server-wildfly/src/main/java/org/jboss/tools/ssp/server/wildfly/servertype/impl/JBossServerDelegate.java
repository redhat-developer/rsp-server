/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import java.io.File;

import org.jboss.tools.ssp.eclipse.core.runtime.CoreException;
import org.jboss.tools.ssp.eclipse.core.runtime.IStatus;
import org.jboss.tools.ssp.eclipse.core.runtime.Status;
import org.jboss.tools.ssp.eclipse.debug.core.DebugException;
import org.jboss.tools.ssp.eclipse.debug.core.ILaunch;
import org.jboss.tools.ssp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.ssp.launching.LaunchingCore;
import org.jboss.tools.ssp.launching.VMInstallModel;
import org.jboss.tools.ssp.server.model.AbstractServerDelegate;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;

public class JBossServerDelegate extends AbstractServerDelegate {
	private ILaunch startLaunch;
	
	public JBossServerDelegate(IServer server) {
		super(server);
	}

	@Override
	public IStatus validate() {
		String home = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		
		if( null == home ) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "Server home must not be null");
		}
		if(!(new File(home).exists())) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "Server home must exist");
		}
		
		String vmPath = getServer().getAttribute(IJBossServerAttributes.VM_INSTALL_PATH, (String)null);
		IVMInstall vmi = null;
		if( vmPath != null && !vmPath.isEmpty()) {
			File vmFile = new File(vmPath);
			if( !vmFile.exists()) {
				return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "VM file location does not exist: " + vmPath);
			}
			vmi = VMInstallModel.getDefault().findVMInstall(vmFile);
		} else {
			vmi = VMInstallModel.getDefault().getDefaultVMInstall();
		}
		if( vmi == null ) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "VM " + vmPath + " is not found in the VM model");
		}
		return Status.OK_STATUS;
	}

	
	public IStatus canStart(String launchMode) {
		if( !"run".equals(launchMode)) {
			return new Status(IStatus.ERROR, 
					"org.jboss.tools.ssp.server.wildfly", 
					"Server must be launched in run mode only.");
		}
		if( getServerState() == IServerDelegate.STATE_STOPPED ) {
			IStatus v = validate();
			if( !v.isOK() )
				return v;
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}
	
	@Override
	public IStatus start(String mode) {
		setServerState(IServerDelegate.STATE_STARTING);
		
		try {
			startLaunch = new JBossStartLauncher(this).launch(mode);
			registerLaunch(startLaunch);
			// TODO fire poller or similar
			setServerState(IServerDelegate.STATE_STARTED);
		} catch(CoreException ce) {
			if( startLaunch != null ) {
				IProcess[] processes = startLaunch.getProcesses();
				for( int i = 0; i < processes.length; i++ ) {
					try {
						processes[i].terminate();
					} catch(DebugException de) {
						LaunchingCore.log(de);
					}
				}
			}
			setServerState(IServerDelegate.STATE_STOPPED);
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}

	
	@Override
	public IStatus stop(boolean force) {
		setServerState(IServerDelegate.STATE_STOPPING);
		ILaunch stopLaunch = null;
		try {
			stopLaunch = new JBossStopLauncher(this).launch(force);
			registerLaunch(stopLaunch);
			
			setServerState(IServerDelegate.STATE_STOPPED);
		} catch(CoreException ce) {
			if( stopLaunch != null ) {
				IProcess[] processes = startLaunch.getProcesses();
				for( int i = 0; i < processes.length; i++ ) {
					try {
						processes[i].terminate();
					} catch(DebugException de) {
						LaunchingCore.log(de);
					}
				}
			}
			setServerState(IServerDelegate.STATE_STARTED);
			return ce.getStatus();
		}
		return Status.OK_STATUS;

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
	
}
