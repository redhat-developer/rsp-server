/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.launchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.Launch;
import org.jboss.tools.rsp.eclipse.jdt.launching.ExecutionArguments;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMRunner;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMRunnerConfiguration;
import org.jboss.tools.rsp.launching.java.ICommandProvider;
import org.jboss.tools.rsp.launching.java.VMInstallClasspath;
import org.jboss.tools.rsp.launching.utils.NativeEnvironmentUtils;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public abstract class AbstractJavaLauncher implements IStartLauncher {
	private IServerDelegate delegate;
	private IVMRunner runner;
	private ILaunch launch;
	private CommandLineDetails launchedDetails = null;
	private VMRunnerConfiguration runConfig;

	public AbstractJavaLauncher(IServerDelegate serverDelegate) {
		this.delegate = serverDelegate;
	}

	protected IServerDelegate getDelegate() {
		return delegate;
	}
	public IServer getServer() {
		return delegate.getServer();
	}
	
	public ILaunch launch(String mode) throws CoreException {
		getLaunchCommand(mode);
		runner.run(runConfig, launch, new NullProgressMonitor());
		return launch;
	}

	public CommandLineDetails getLaunchCommand(String mode) throws CoreException {
		IStatus preReqs = checkPrereqs(mode);
		if (!preReqs.isOK())
			throw new CoreException(preReqs);

		launch = createLaunch(mode);
		runConfig = configureRunner();
		if (runner instanceof ICommandProvider) {
			launchedDetails = ((ICommandProvider) runner).getCommandLineDetails(runConfig, launch,
					new NullProgressMonitor());
			return launchedDetails;
		}
		return null;
	}

	public CommandLineDetails getLaunchedDetails() {
		return launchedDetails;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	private VMRunnerConfiguration configureRunner() {
		String pgmArgs = getProgramArguments();
		String vmArgs = getVMArguments();
		String[] classpath = getClasspath();
		String[] environment = getEnvironment();
		String mainType = getMainTypeName();
		String workingDirectory = getWorkingDirectory();
		
		// null-safe
		pgmArgs = (pgmArgs == null ? "" : pgmArgs);
		vmArgs = (vmArgs == null ? "" : vmArgs);

		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);

		// VM-specific attributes
		Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap();

		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirectory);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);
		runConfig.setEnvironment(environment);

		// Bootpath
		String[] bootpath = getBootpath();
		if (bootpath != null && bootpath.length > 0)
			runConfig.setBootClassPath(bootpath);
		return runConfig;
	}

	private ILaunch createLaunch(String mode) {
		return new Launch(this, mode, null);
	}

	protected IStatus checkPrereqs(String mode) {
		runner = getVMRunner(delegate, mode);
		if (runner == null) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;

	}
	
	protected IVMInstall getVMInstall(IServerDelegate delegate) {
		return getDefaultRegistry().getDefaultVMInstall();
	}

	private IVMRunner getVMRunner(IServerDelegate delegate, String mode) {
		IVMInstall vmi = getVMInstall(delegate);
		return vmi == null ? null : vmi.getVMRunner(mode);
	}
	
	protected abstract IVMInstallRegistry getDefaultRegistry();

	protected abstract String getWorkingDirectory();

	protected abstract String getMainTypeName();

	protected abstract String getVMArguments();

	protected abstract String getProgramArguments();

	protected abstract String[] getClasspath();

	protected String[] getBootpath() {
		// TODO Auto-generated method stub
		return null;
	}

	protected Map<String, Object> getVMSpecificAttributesMap() {
		// TODO Auto-generated method stub
		return null;
	}

	protected String[] getJREClasspath() {
		IVMInstall vm = getVMInstall(delegate);
		return VMInstallClasspath.get(vm);
	}

	protected String[] addJreClasspathEntries(List<String> current) {
		ArrayList<String> ret = new ArrayList<>(Arrays.asList(getJREClasspath()));
		ret.addAll(current);
		return (String[]) ret.toArray(new String[ret.size()]);
	}

	protected String[] getEnvironment() {
		return getEnvironment(true);
	}

	protected String[] getEnvironment(boolean appendNativeEnv) {
		Map<String, String> configEnv = getEnvironmentFromServer();
		return NativeEnvironmentUtils.getDefault().getEnvironment(configEnv, appendNativeEnv);
	}

	protected Map<String, String> getEnvironmentFromServer() {
		return new HashMap<String, String>(System.getenv());
	}
	
}
