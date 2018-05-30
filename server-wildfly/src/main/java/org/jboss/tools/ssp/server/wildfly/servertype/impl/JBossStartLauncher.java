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
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.ssp.api.dao.CommandLineDetails;
import org.jboss.tools.ssp.eclipse.core.runtime.CoreException;
import org.jboss.tools.ssp.eclipse.core.runtime.IStatus;
import org.jboss.tools.ssp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.ssp.eclipse.core.runtime.Status;
import org.jboss.tools.ssp.eclipse.debug.core.ILaunch;
import org.jboss.tools.ssp.eclipse.debug.core.Launch;
import org.jboss.tools.ssp.eclipse.jdt.launching.ExecutionArguments;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMRunner;
import org.jboss.tools.ssp.eclipse.jdt.launching.VMRunnerConfiguration;
import org.jboss.tools.ssp.launching.ICommandProvider;
import org.jboss.tools.ssp.launching.NativeEnvironmentUtil;
import org.jboss.tools.ssp.launching.VMInstallModel;

public class JBossStartLauncher {
	private JBossServerDelegate delegate;
	private IVMRunner runner;
	private IVMInstall vmi;
	private ILaunch launch;
	public JBossStartLauncher(JBossServerDelegate jBossServerDelegate) {
		this.delegate = jBossServerDelegate;
	}
	
	public ILaunch launch(String mode) throws CoreException {
		IStatus preReqs = checkPrereqs(mode);
		if( !preReqs.isOK())
			throw new CoreException(preReqs);
		
		launch = createLaunch(mode);
		VMRunnerConfiguration runConfig = configureRunner();
		runner.run(runConfig, launch, new NullProgressMonitor());
		return launch;
	}
	
	public CommandLineDetails getLaunchCommand(String mode) throws CoreException {
		IStatus preReqs = checkPrereqs(mode);
		if( !preReqs.isOK())
			throw new CoreException(preReqs);
		
		launch = createLaunch(mode);
		VMRunnerConfiguration runConfig = configureRunner();
		if( runner instanceof ICommandProvider ) {
			return ((ICommandProvider)runner).getCommandLineDetails(runConfig, launch, new NullProgressMonitor());
		}
		return null;
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
	private IStatus checkPrereqs(String mode) {
		String vmPath = delegate.getServer().getAttribute(IJBossServerAttributes.VM_INSTALL_PATH, (String)null);
		if( vmPath == null ) {
			vmi = VMInstallModel.getDefault().getDefaultVMInstall();
		} else {
			vmi = VMInstallModel.getDefault().findVMInstall(new File(vmPath));
		}
		if( vmi == null ) {
			return Status.CANCEL_STATUS;
		}
		runner = vmi.getVMRunner(mode);
		if( runner == null ) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}
	/*
	 * The bottom methods can be abstracted out into a java-server type class
	 */
	
	private String[] getBootpath() {
		// TODO Auto-generated method stub
		return null;
	}
	private Map<String, Object> getVMSpecificAttributesMap() {
		// TODO Auto-generated method stub
		return null;
	}
	private String getWorkingDirectory() {
		String serverHome = delegate.getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		return serverHome + "/bin";
	}
	private String getMainTypeName() {
		return "org.jboss.modules.Main";
	}
	private String[] getClasspath() {
		String serverHome = delegate.getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		return new String[] {serverHome + "/jboss-modules.jar"};
	}
	private String getVMArguments() {
		String serverHome = delegate.getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		String ret = "\"-Dprogram.name=JBossTools: WildFly 12\" " + 
					"-server -Xms64m -Xmx512m -Dorg.jboss.resolver.warning=true " +
				"-Djava.net.preferIPv4Stack=true -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 " +
					"-Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true "
				+ "\"-Dorg.jboss.boot.log.file=" + serverHome + "standalone/log/boot.log\" " + 
					"\"-Dlogging.configuration=file:" + serverHome + "/standalone/configuration/logging.properties\" " + 
				"\"-Djboss.home.dir=" + serverHome + "\" -Dorg.jboss.logmanager.nocolor=true -Djboss.bind.address.management=localhost ";
		return ret;
	}
	private String getProgramArguments() {
		String serverHome = delegate.getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		return "-mp \"" + serverHome + "/modules\" " + 
				"org.jboss.as.standalone -b localhost " + 
				"--server-config=standalone.xml " +
				"-Djboss.server.base.dir=\"" + serverHome + "/standalone\"";
	}
	
	private String[] getEnvironment() {
		return getEnvironment(true);
	}
	
	private String[] getEnvironment(boolean appendNativeEnv) {
		Map<String, String> configEnv = getEnvironmentFromServer();
		return NativeEnvironmentUtil.getDefault().getEnvironment(configEnv, appendNativeEnv);
	}

	private Map<String, String>  getEnvironmentFromServer() {
		return new HashMap<String, String>();
	}
}
