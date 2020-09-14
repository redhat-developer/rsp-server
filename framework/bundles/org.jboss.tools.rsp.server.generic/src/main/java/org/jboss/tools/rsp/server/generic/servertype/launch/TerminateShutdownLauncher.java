/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype.launch;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.model.ServerModelListenerAdapter;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class TerminateShutdownLauncher implements IServerShutdownLauncher {

	private GenericServerBehavior genericServerBehavior;
	private ILaunch startLaunch;

	public TerminateShutdownLauncher(GenericServerBehavior genericServerBehavior, ILaunch startLaunch) {
		this.genericServerBehavior = genericServerBehavior;
		this.startLaunch = startLaunch;
	}

	@Override
	public ILaunch launch(boolean force) throws CoreException {
		final Boolean[] stopped = new Boolean[1];
		stopped[0] = false;
		ServerModelListenerAdapter listener = new ServerModelListenerAdapter() {
			@Override
			public void serverStateChanged(ServerHandle server, ServerState state) {
				if(genericServerBehavior.getServerHandle().equals(server) && state.getState() == ServerManagementAPIConstants.STATE_STOPPED) {
					stopped[0] = true;
				}
			}
		};
		genericServerBehavior.getServer().getServerModel().addServerModelListener(listener);
		terminateAllProcesses(startLaunch);
		genericServerBehavior.getServer().getServerModel().removeServerModelListener(listener);
		if( stopped[0]) {
			// do nothing 
		} else if( allProcessesTerminated(startLaunch)) {
			genericServerBehavior.setServerState(IServerDelegate.STATE_STOPPED);
		} else {
			genericServerBehavior.setServerState(IServerDelegate.STATE_STARTED);
		}
		return null;
	}

	private boolean allProcessesTerminated(ILaunch launch) {
		if( launch != null ) {
			IProcess[] processes = launch.getProcesses();
			for( int i = 0; i < processes.length; i++ ) {
				if( !processes[i].isTerminated()) { 
					return false;
				}
			}
		}
		return true;
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
	public ILaunch getLaunch() {
		return null;
	}

	@Override
	public IServer getServer() {
		return this.genericServerBehavior.getServer();
	}

}
