/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pascal Gruen (pascal.gruen@googlemail.com) - Bug 217994 Run/Debug honors JRE VM args before Launcher VM args
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.jdt.launching;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugPluginConstants;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.jdt.core.JavaCore;
import org.jboss.tools.rsp.eclipse.jdt.core.JavaCoreConstants;
import org.jboss.tools.rsp.launching.java.ICommandProvider;
import org.jboss.tools.rsp.launching.utils.ExecUtil;
import org.jboss.tools.rsp.launching.utils.OSUtils;

/**
 * Abstract implementation of a VM runner.
 * <p>
 * Clients implementing VM runners should subclass this class.
 * </p>
 * @see IVMRunner
 * @since 2.0
 */
public abstract class AbstractVMRunner implements IVMRunner, ICommandProvider {
	public static final String AbstractVMRunner_0="An IProcess could not be created for the launch";

	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 *
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 * @throws CoreException The exception encapsulating the reason for the abort
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, getPluginIdentifier(), code, message, exception));
	}

	/**
	 * Returns the identifier of the plug-in this VM runner
	 * originated from.
	 *
	 * @return plug-in identifier
	 */
	protected abstract String getPluginIdentifier();

	private static String[] quoteWindowsArgs(String[] cmdLine) {
		// see https://bugs.eclipse.org/387504 , workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6511002
		if (OSUtils.isWindows()) {
			String[] winCmdLine = new String[cmdLine.length];
			if(cmdLine.length > 0) {
				winCmdLine[0] = cmdLine[0];
			}
			for (int i = 1; i < cmdLine.length; i++) {
				winCmdLine[i] = winQuote(cmdLine[i]);
			}
			cmdLine = winCmdLine;
		}
		return cmdLine;
	}


	private static boolean needsQuoting(String s) {
		int len = s.length();
		if (len == 0) // empty string has to be quoted
			return true;
		if ("\"\"".equals(s)) //$NON-NLS-1$
			return false; // empty quotes must not be quoted again
		for (int i = 0; i < len; i++) {
			switch (s.charAt(i)) {
				case ' ': case '\t': case '\\': case '"':
					return true;
			}
		}
		return false;
	}

	private static String winQuote(String s) {
		if (! needsQuoting(s))
			return s;
		s = s.replaceAll("([\\\\]*)\"", "$1$1\\\\\""); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll("([\\\\]*)\\z", "$1$1"); //$NON-NLS-1$ //$NON-NLS-2$
		return "\"" + s + "\""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the given array of strings as a single space-delimited string.
	 *
	 * @param cmdLine array of strings
	 * @return a single space-delimited string
	 */
	protected String getCmdLineAsString(String[] cmdLine) {
		StringBuffer buff= new StringBuffer();
		for (int i = 0, numStrings= cmdLine.length; i < numStrings; i++) {
			buff.append(cmdLine[i]);
			buff.append(' ');
		}
		return buff.toString().trim();
	}

	/**
	 * Returns the default process attribute map for Java processes.
	 *
	 * @return default process attribute map for Java processes
	 */
	protected Map<String, String> getDefaultProcessMap() {
		Map<String, String> map = new HashMap<>();
		map.put(IProcess.ATTR_PROCESS_TYPE, IJavaLaunchConfigurationConstants.ID_JAVA_PROCESS_TYPE);
		return map;
	}

	/**
	 * Returns a new process aborting if the process could not be created.
	 * @param launch the launch the process is contained in
	 * @param p the system process to wrap
	 * @param label the label assigned to the process
	 * @param attributes values for the attribute map
	 * @return the new process
	 * @throws CoreException problems occurred creating the process
	 * @since 3.0
	 */
	protected IProcess newProcess(ILaunch launch, Process p, String label, Map<String, String> attributes) throws CoreException {
		IProcess process= ExecUtil.newProcess(launch, p, label, attributes);
		if (process == null) {
			p.destroy();
			abort(AbstractVMRunner_0, null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return process;
	}
	
	public IProcess newProcess(ILaunch launch, Process process, String label) throws CoreException {
		return newProcess(launch, process, label, null);
	}
	
	
	/**
	 * Combines and returns VM arguments specified by the runner configuration,
	 * with those specified by the VM install, if any.
	 *
	 * @param configuration runner configuration
	 * @param vmInstall VM install
	 * @return combined VM arguments specified by the runner configuration
	 *  and VM install
	 * @since 3.0
	 */
//	protected String[] combineVmArgs(VMRunnerConfiguration configuration, IVMInstall vmInstall) {
//		String[] vmVMArgs = vmInstall.getVMArguments();
//		return combineVmArgs(configuration, vmVMArgs);
//	}
	
	
	protected String[] combineVmArgs(VMRunnerConfiguration configuration, String[] vmVMArgs) {
		String[] launchVMArgs= configuration.getVMArguments();
		if (vmVMArgs == null || vmVMArgs.length == 0) {
			return launchVMArgs;
		}
//		// string substitution
//		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
//		for (int i = 0; i < vmVMArgs.length; i++) {
//			try {
//				vmVMArgs[i] = manager.performStringSubstitution(vmVMArgs[i], false);
//			} catch (CoreException e) {
//				LaunchingPlugin.log(e.getStatus());
//			}
//		}
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=217994
		// merge default VM + launcher VM arguments. Make sure to pass launcher arguments in last so that
		// they can take precedence over the default VM args!
		String[] allVMArgs = new String[launchVMArgs.length + vmVMArgs.length];
		System.arraycopy(vmVMArgs, 0, allVMArgs, 0, vmVMArgs.length);
		System.arraycopy(launchVMArgs, 0, allVMArgs, vmVMArgs.length, launchVMArgs.length);
		return allVMArgs;
	}
	

	/**
	 * Examines the project and install for presence of module and execution support.
	 *
	 * @param config
	 *            runner configuration
	 * @param vmInstall
	 *            VM install
	 * @return <code>true</code> if project is a module and uses JRE version 9 or more, or <code>false</code> otherwise
	 * @since 3.10
	 */
	protected boolean isModular(VMRunnerConfiguration config, IVMInstall vmInstall) {
		if (config.getModuleDescription() != null && config.getModuleDescription().length() > 0 && vmInstall instanceof AbstractVMInstall) {
			AbstractVMInstall install = (AbstractVMInstall) vmInstall;
			String vmver = install.getJavaVersion();
			// versionToJdkLevel only handles 3 char versions = 1.5, 1.6, 1.9, etc
			if (vmver.length() > 3) {
				vmver = vmver.substring(0, 3);
			}
			if (JavaCore.compareJavaVersions(vmver, JavaCoreConstants.VERSION_9) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Executes the given command line using the given working directory
	 *
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory
	 * @return the {@link Process}
	 * @throws CoreException if the execution fails
	 * @see DebugPluginConstants#exec(String[], File)
	 */
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		cmdLine = quoteWindowsArgs(cmdLine);
		return ExecUtil.exec(cmdLine, workingDirectory);
	}

	/**
	 * Executes the given command line using the given working directory and environment
	 *
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory
	 * @param envp the environment
	 * @return the {@link Process}
	 * @throws CoreException is the execution fails
	 * @since 3.0
	 * @see DebugPluginConstants#exec(String[], File, String[])
	 */
	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		cmdLine = quoteWindowsArgs(cmdLine);
		return ExecUtil.exec(cmdLine, workingDirectory, envp);
	}
	
}
