/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.Launch;
import org.jboss.tools.rsp.foundation.core.launchers.CommandConfig;
import org.jboss.tools.rsp.launching.java.ILaunchModes;
import org.jboss.tools.rsp.launching.utils.LaunchingDebugProperties;
import org.jboss.tools.rsp.server.spi.launchers.GenericServerProcessRunner;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public abstract class AbstractCommandLineLauncher implements IServerStartLauncher {
	protected IServerDelegate delegate;
	protected ILaunch launch;
	protected CommandLineDetails launchedDetails = null;
	protected GenericServerProcessRunner runner;

	public AbstractCommandLineLauncher(IServerDelegate msDelegate) {
		this.delegate = msDelegate;
	}

	public IServerDelegate getDelegate() {
		return delegate;
	}

	public IServer getServer() {
		return delegate.getServer();
	}

	public ILaunch launch(String mode) throws CoreException {
		getLaunchCommand(mode);
		configureRunner(mode);
		launchedDetails = runner.runWithDetails(launch, new NullProgressMonitor());
		addDebugFlagsToDetails(mode, launchedDetails);
		return launch;
	}

	public CommandLineDetails getLaunchCommand(String mode) throws CoreException {
		IStatus preReqs = checkPrereqs(mode);
		if (!preReqs.isOK())
			throw new CoreException(preReqs);

		launch = createLaunch(mode);
		configureRunner(mode);
		launchedDetails = runner.getCommandLineDetails(launch, new NullProgressMonitor());
		addDebugFlagsToDetails(mode, launchedDetails);
		return launchedDetails;
	}

	private void addDebugFlagsToDetails(String mode, CommandLineDetails launchedDetails) {
		if( mode.equals(ILaunchModes.DEBUG)) {
			int portInt = -1;
			String[] cmdArr = launchedDetails.getCmdLine();
			if( cmdArr != null ) {
				for( int i = 0; i < cmdArr.length; i++ ) {
					if( cmdArr[i].startsWith("-Dwildfly.bootable.debug.port")) {
						String port = cmdArr[i].substring("-Dwildfly.bootable.debug.port".length() + 1);
						if( port != null ) {
							try {
								portInt = Integer.parseInt(port);
							} catch(NumberFormatException nfe) {
							}
						}
					}
				}
				if( portInt != -1 ) {
					launchedDetails.getProperties().put(LaunchingDebugProperties.DEBUG_DETAILS_TYPE, "java");
					launchedDetails.getProperties().put(LaunchingDebugProperties.DEBUG_DETAILS_HOST, "localhost");
					launchedDetails.getProperties().put(LaunchingDebugProperties.DEBUG_DETAILS_PORT, Integer.toString(portInt));
				}
			}
		}
	}
	
	public CommandLineDetails getLaunchedDetails() {
		return launchedDetails;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	private ILaunch createLaunch(String mode) {
		return new Launch(this, mode, null);
	}

	protected IStatus checkPrereqs(String mode) {
		return Status.OK_STATUS;

	}

	public abstract String getProgramArguments();

	public GenericServerProcessRunner configureRunner(String mode) {
		if (runner == null) {
			CommandConfig config = getCommandConfig(mode);
			runner = new GenericServerProcessRunner(delegate, config);
		}
		return runner;
	}

	protected abstract CommandConfig getCommandConfig(String mode);

	public abstract String getWorkingDirectory();

}
