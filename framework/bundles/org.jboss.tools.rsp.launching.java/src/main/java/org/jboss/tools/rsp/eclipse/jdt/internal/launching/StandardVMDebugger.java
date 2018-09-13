/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Smirnoff   - Bug 289916
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.jdt.internal.launching;


import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.debug.core.ArgumentUtils;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMRunnerConfiguration;
import org.jboss.tools.rsp.launching.utils.NativeEnvironmentUtils;
import org.jboss.tools.rsp.launching.utils.OSUtils;


/**
 * A launcher for debugging Java main classes. Uses JDI to launch a VM in debug
 * mode.
 */
public class StandardVMDebugger extends StandardVMRunner {
	private static final String StandardVMDebugger_Could_not_find_a_free_socket_for_the_debugger_1="Cannot find a free socket for the debugger";
	private static final String StandardVMDebugger_Starting_virtual_machine____4="Starting virtual machine...";


	/**
	 * @since 3.3 OSX environment variable specifying JRE to use
	 */
	protected static final String JAVA_JVM_VERSION = "JAVA_JVM_VERSION"; //$NON-NLS-1$

	/**
	 * JRE path segment descriptor
	 *
	 * String equals the word: <code>jre</code>
	 *
	 * @since 3.3.1
	 */
	protected static final String JRE = "jre"; //$NON-NLS-1$

	/**
	 * Bin path segment descriptor
	 *
	 * String equals the word: <code>bin</code>
	 *
	 * @since 3.3.1
	 */
	protected static final String BIN = "bin"; //$NON-NLS-1$

	/**
	 * Creates a new launcher
	 * @param vmInstance the backing {@link IVMInstall} to launch
	 */
	public StandardVMDebugger(IVMInstall vmInstance) {
		super(vmInstance);
	}

	/* (non-Javadoc)
	 * @see org.jboss.tools.rsp.eclipse.jdt.launching.IVMRunner#run(org.jboss.tools.rsp.eclipse.jdt.launching.VMRunnerConfiguration, org.jboss.tools.rsp.eclipse.debug.core.ILaunch, org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		run(config, launch, false, monitor);
	}

	@Override
	public CommandLineDetails getCommandLineDetails(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor subMonitor) throws CoreException {
		// TODO maybe make these adjustable?  idk
		String transport = "dt_socket";
		String server = "y";
		String suspend = "y";
		String host = "localhost";

		int port= findFreePort();
		if (port == -1) {
			abort(StandardVMDebugger_Could_not_find_a_free_socket_for_the_debugger_1, null, IJavaLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE);
		}

		subMonitor.worked(1);

		
		String program= constructProgramString(config);

		List<String> arguments= new ArrayList<>(12);

		arguments.add(program);

		if (fVMInstance instanceof StandardVM && ((StandardVM)fVMInstance).getDebugArgs() != null){
			String debugArgString = ((StandardVM)fVMInstance).getDebugArgs().replaceAll("\\Q" + StandardVM.VAR_PORT + "\\E", Integer.toString(port));  //$NON-NLS-1$ //$NON-NLS-2$
			String[] debugArgs = ArgumentUtils.parseArguments(debugArgString);
			for (int i = 0; i < debugArgs.length; i++) {
				arguments.add(debugArgs[i]);
			}
		} else {
			// Extracted method out
			addDebugFlags(arguments, transport, server, suspend, host, port);
		}

		String[] allVMArgs = combineVmArgs(config, new String[0]); //fVMInstance);
		addArguments(ensureEncoding(launch, allVMArgs), arguments);
		addBootClassPathArguments(arguments, config);

		String[] mp = config.getModulepath();
		if (mp != null && mp.length > 0) { // There can be scenarios like junit where launched class is in classpath
											// with modular path entries
			arguments.add("-p"); //$NON-NLS-1$
			arguments.add(convertClassPath(mp));
		}

		String[] cp= config.getClassPath();
		int cpidx = -1;
		if (cp.length > 0) {
			cpidx = arguments.size();
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}

		String dependencies = config.getOverrideDependencies();
		if (dependencies != null && dependencies.length() > 0) {
			String[] parseArguments = ArgumentUtils.parseArguments(dependencies);
			for (String string : parseArguments) {
				arguments.add(string);
			}

		}

		if (isModular(config, fVMInstance)) {
			arguments.add("-m"); //$NON-NLS-1$
			arguments.add(config.getModuleDescription() + "/" + config.getClassToLaunch()); //$NON-NLS-1$
		} else {
			arguments.add(config.getClassToLaunch());
		}

		/*
		 * String[] cp= config.getClassPath(); int cpidx = -1; if (cp.length > 0) { cpidx = arguments.size(); arguments.add("-classpath");
		 * //$NON-NLS-1$ arguments.add(convertClassPath(cp)); }
		 *
		 * arguments.add(config.getClassToLaunch());
		 */
		addArguments(config.getProgramArguments(), arguments);

		//With the newer VMs and no backwards compatibility we have to always prepend the current env path (only the runtime one)
		//with a 'corrected' path that points to the location to load the debug dlls from, this location is of the standard JDK installation
		//format: <jdk path>/jre/bin
		String[] envp = prependJREPath(config.getEnvironment(), new Path(program));

		String[] newenvp = checkClasspath(arguments, cp, envp);
		if(newenvp != null) {
			envp = newenvp;
			arguments.remove(cpidx);
			arguments.remove(cpidx);
		}

		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);

		// check for cancellation
		if (subMonitor.isCanceled()) {
			return null;
		}
		File workingDir = getWorkingDir(config);
		String wd = workingDir == null ? null : workingDir.getAbsolutePath();
		String[] newCmdLine = validateCommandLine(launch, cmdLine);
		if(newCmdLine != null) {
			cmdLine = newCmdLine;
		}

		subMonitor.worked(1);
		subMonitor.subTask(StandardVMDebugger_Starting_virtual_machine____4);
		Map<String,String> debugFlagMap = generateDebugFlagMap(transport, server, suspend, host, port);
		return new CommandLineDetails(cmdLine, wd, newenvp, debugFlagMap);
	}
	
	
	private static final String DEBUG_TRANSPORT_KEY = "debug.java.transport";
	private static final String DEBUG_TRANSPORT_SERVER = "debug.java.server";
	private static final String DEBUG_TRANSPORT_SUSPEND = "debug.java.suspend";
	
	private HashMap<String,String> generateDebugFlagMap(String transport, String server, 
			String suspend, String host, int port) {
		HashMap<String,String> ret = new HashMap<>();
		ret.put(ServerManagementAPIConstants.DEBUG_DETAILS_TYPE, "java"); // TODO extract to better loc?
		ret.put(ServerManagementAPIConstants.DEBUG_DETAILS_HOST, host); 
		ret.put(ServerManagementAPIConstants.DEBUG_DETAILS_PORT, Integer.toString(port));
		ret.put(DEBUG_TRANSPORT_KEY, transport);
		ret.put(DEBUG_TRANSPORT_SERVER, server);
		ret.put(DEBUG_TRANSPORT_SUSPEND, suspend);
		return ret;
	}
	
	private void addDebugFlags(List<String> arguments, String transport, String server, String suspend, String host, int port) {
		// VM arguments are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first options
		double version = getJavaVersion();
		if (version < 1.5) {
			arguments.add("-Xdebug"); //$NON-NLS-1$
			arguments.add("-Xnoagent"); //$NON-NLS-1$
		}

		//check if java 1.4 or greater
		if (version < 1.4) {
			arguments.add("-Djava.compiler=NONE"); //$NON-NLS-1$
		}
		
		StringBuffer debugFlagBuffer = new StringBuffer();
		if (version < 1.5) {
			debugFlagBuffer.append("-Xrunjdwp:transport=");
		} else {
			debugFlagBuffer.append("-agentlib:jdwp=transport=");
		}
		debugFlagBuffer.append(transport);
		debugFlagBuffer.append(",server=");
		debugFlagBuffer.append(server);
		debugFlagBuffer.append(",suspend=");
		debugFlagBuffer.append(suspend);
		debugFlagBuffer.append(",address=");
		debugFlagBuffer.append(host);
		debugFlagBuffer.append(":");
		debugFlagBuffer.append(port);
		
		arguments.add(debugFlagBuffer.toString());
	}
	
	/**
	 * This method performs platform specific operations to modify the runtime path for JREs prior to launching.
	 * Nothing is written back to the original system path.
	 *
	 * <p>
	 * For Windows:
	 * Prepends the location of the JRE bin directory for the given JDK path to the PATH variable in Windows.
	 * This method assumes that the JRE is located within the JDK install directory
	 * in: <code><JDK install dir>/jre/bin/</code> where the JRE itself would be located
	 * in: <code><JDK install dir>/bin/</code>  where the JDK itself is located
	 * </p>
	 * <p>
	 * For Mac OS:
	 * Searches for and sets the correct state of the JAVA_VM_VERSION environment variable to ensure it matches
	 * the currently chosen VM of the launch config
	 * </p>
	 *
	 * @param env the current array of environment variables to run with
	 * @param jdkpath the path to the executable (javaw).
	 * @return the altered JRE path
	 * @since 3.3
	 */
	protected String[] prependJREPath(String[] env, IPath jdkpath) {
		if(OSUtils.isWindows()) {
			IPath jrepath = jdkpath.removeLastSegments(1);
			if(jrepath.lastSegment().equals(BIN)) {
				int count = jrepath.segmentCount();
				if(count > 1 && !jrepath.segment(count-2).equalsIgnoreCase(JRE)) {
					jrepath = jrepath.removeLastSegments(1).append(JRE).append(BIN);
				}
			}
			else {
				jrepath = jrepath.append(JRE).append(BIN);
			}
			if(jrepath.toFile().exists()) {
				String jrestr = jrepath.toOSString();
				if(env == null){
					Map<String, String> map = NativeEnvironmentUtils.getDefault().getNativeEnvironment();
					env = new String[map.size()];
					String var = null;
					int index = 0;
					for(Iterator<String> iter = map.keySet().iterator(); iter.hasNext();) {
						var = iter.next();
						String value = map.get(var);
						if (value == null) {
							value = ""; //$NON-NLS-1$
						}
						if (var.equalsIgnoreCase("path")) { //$NON-NLS-1$
							if(value.indexOf(jrestr) == -1) {
								value = jrestr+';'+value;
							}
						}
						env[index] = var+"="+value; //$NON-NLS-1$
						index++;
					}
				} else {
					String var = null;
					int esign = -1;
					for(int i = 0; i < env.length; i++) {
						esign = env[i].indexOf('=');
						if(esign > -1) {
							var = env[i].substring(0, esign);
							if(var != null && var.equalsIgnoreCase("path")) { //$NON-NLS-1$
								if(env[i].indexOf(jrestr) == -1) {
									env[i] = var + "="+jrestr+';'+(esign == env[i].length() ? "" : env[i].substring(esign+1)); //$NON-NLS-1$ //$NON-NLS-2$
									break;
								}
							}
						}
					}
				}
			}
		}
		return super.prependJREPath(env);
	}
	/**
	 * Returns the version of the current VM in use
	 * @return the VM version
	 */
	private double getJavaVersion() {
		String version = null;
		if (fVMInstance instanceof IVMInstall) {
			version = ((IVMInstall)fVMInstance).getJavaVersion();
		}
		if (version == null) {
			// unknown version
			return 0D;
		}
		int index = version.indexOf("."); //$NON-NLS-1$
		int nextIndex = version.indexOf(".", index+1); //$NON-NLS-1$
		try {
			if (index > 0 && nextIndex>index) {
				return Double.parseDouble(version.substring(0,nextIndex));
			}
			return Double.parseDouble(version);
		} catch (NumberFormatException e) {
			return 0D;
		}

	}

	/**
	 * Returns a free port number on localhost, or -1 if unable to find a free port.
	 *
	 * @return a free port number on localhost, or -1 if unable to find a free port
	 * @since 3.0
	 */
	public static int findFreePort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (IOException e) {
		}
		return -1;
	}
}
