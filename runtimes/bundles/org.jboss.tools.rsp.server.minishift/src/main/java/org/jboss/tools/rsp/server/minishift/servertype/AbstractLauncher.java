/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.ArgumentUtils;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.Launch;
import org.jboss.tools.rsp.server.minishift.servertype.impl.EnvironmentUtility;
import org.jboss.tools.rsp.server.spi.launchers.CommandConfig;
import org.jboss.tools.rsp.server.spi.launchers.GenericProcessRunner;
import org.jboss.tools.rsp.server.spi.launchers.IStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public abstract class AbstractLauncher implements IStartLauncher {
	private IServerDelegate delegate;
	private ILaunch launch;
	private CommandLineDetails launchedDetails = null;
	private GenericProcessRunner runner;
	
	
	public AbstractLauncher(IServerDelegate msDelegate) {
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
		configureRunner();
		runner.run(launch, new NullProgressMonitor());
		return launch;
	}

	public CommandLineDetails getLaunchCommand(String mode) throws CoreException {
		IStatus preReqs = checkPrereqs(mode);
		if (!preReqs.isOK())
			throw new CoreException(preReqs);

		launch = createLaunch(mode);
		configureRunner();
		launchedDetails = runner.getCommandLineDetails(launch, new NullProgressMonitor());
		return launchedDetails;
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
	
	
	//protected abstract IDefaultLaunchArguments getLaunchArgs();
	
	public GenericProcessRunner configureRunner() {
		if( runner == null ) {
			runner = new GenericProcessRunner(delegate, getCommandConfig());
		}
		return runner;
	}
	
	protected CommandConfig getCommandConfig() {
		String cmd = MinishiftPropertyUtility.getMinishiftCommand(getServer());
		String args = getProgramArguments();
		String[] parsed = ArgumentUtils.parseArguments(args);
		String wd = getWorkingDirectory();
		String[] env = new EnvironmentUtility(getServer()).getEnvironment();
		CommandConfig details = new CommandConfig(cmd, wd, parsed, env);
		return details;
	}

	public String getWorkingDirectory() {
		return new Path(MinishiftPropertyUtility.getMinishiftCommand(
				getServer())).removeLastSegments(1).toOSString();
	}

}
