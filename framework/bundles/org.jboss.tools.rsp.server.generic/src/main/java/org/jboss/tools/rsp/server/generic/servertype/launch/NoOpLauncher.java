/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype.launch;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class NoOpLauncher implements IServerShutdownLauncher, IServerStartLauncher {

	private GenericServerBehavior genericServerBehavior;

	public NoOpLauncher(GenericServerBehavior genericServerBehavior) {
		this.genericServerBehavior = genericServerBehavior;
	}

	@Override
	// Stop launch
	public ILaunch launch(boolean force) throws CoreException {
		genericServerBehavior.setServerState(IServerDelegate.STATE_STOPPED);
		return null;
	}

	@Override
	public ILaunch getLaunch() {
		return null;
	}

	@Override
	public IServer getServer() {
		return this.genericServerBehavior.getServer();
	}

	@Override
	// Start launch
	public ILaunch launch(String mode) throws CoreException {
		genericServerBehavior.setServerState(IServerDelegate.STATE_STARTED);
		return null;
	}

	@Override
	public CommandLineDetails getLaunchedDetails() {
		return null;
	}

	@Override
	public CommandLineDetails getLaunchCommand(String mode) throws CoreException {
		return null;
	}

}
