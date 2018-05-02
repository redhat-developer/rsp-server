/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.jboss.tools.ssp.launching.NativeEnvironmentUtil;
import org.jboss.tools.ssp.launching.VMInstallModel;

public class JBossStopLauncher {
	private JBossServerDelegate delegate;
	private IVMRunner runner;
	private IVMInstall vmi;
	private ILaunch launch;
	public JBossStopLauncher(JBossServerDelegate jBossServerDelegate) {
		this.delegate = jBossServerDelegate;
	}
	public ILaunch launch(boolean force) throws CoreException {
		String mode = "run";
		IStatus preReqs = checkPrereqs(mode);
		if( !preReqs.isOK())
			throw new CoreException(preReqs);
		
		launch = createLaunch(mode);
		VMRunnerConfiguration runConfig = configureRunner();
		runner.run(runConfig, launch, new NullProgressMonitor());
		return launch;
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
		String vmId = delegate.getServer().getAttribute(IJBossServerAttributes.VM_INSTALL_ID, (String)null);
		if( vmId == null ) {
			return Status.CANCEL_STATUS;
		}
		vmi = VMInstallModel.getDefault().findVMInstall(vmId);
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
		StringBuffer sb = new StringBuffer();
		sb.append("-Djboss.modules.system.pkgs=com.sun.java.swing ");
		sb.append("-Dcom.ibm.jsse2.overrideDefaultTLS=true ");
		sb.append("-Dlogging.configuration=file:");
		sb.append(serverHome);
		sb.append("/bin/jboss-cli-logging.properties");
		return sb.toString();
	}
	
	private String getProgramArguments() {
		String serverHome = delegate.getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		StringBuffer sb = new StringBuffer();
		sb.append("-mp \"");
		sb.append(serverHome);
		sb.append("/modules\" org.jboss.as.cli ");
		sb.append("--connect command=:shutdown");
		return sb.toString();
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
