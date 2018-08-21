/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.jdt.internal.launching;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.rsp.eclipse.debug.core.ArgumentUtils;
import org.jboss.tools.rsp.eclipse.debug.core.DebugPluginConstants;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.Launch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.jdt.launching.AbstractVMRunner;
import org.jboss.tools.rsp.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.StandardVMType;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMRunnerConfiguration;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.launching.utils.NativeEnvironmentUtils;
import org.jboss.tools.rsp.launching.utils.OSUtils;

/**
 * A launcher for running Java main classes.
 * 
 * TODO:  Migrate over debug code: StandardVMDebugger and subclass!
 * 
 */
public class StandardVMRunner extends AbstractVMRunner {
	public static final String StandardVMRunner__0____1___2="{0} ({1})";
	public static final String StandardVMRunner__0__at_localhost__1__1="{0} at localhost:{1}";
	public static final String StandardVMRunner_Specified_working_directory_does_not_exist_or_is_not_a_directory___0__3="Specified working directory does not exist or is not a directory: {0}";
	public static final String StandardVMRunner_Launching_VM____1="Launching VM...";
	public static final String StandardVMRunner_Constructing_command_line____2="Constructing command line...";
	public static final String StandardVMRunner_Starting_virtual_machine____3="Starting virtual machine...";
	public static final String StandardVMRunner_Unable_to_locate_executable_for__0__1="Unable to locate executable for {0}";
	public static final String StandardVMRunner_Specified_executable__0__does_not_exist_for__1__4="Specified executable {0} does not exist for {1}";

	/**
	 * Constant representing the <code>-XstartOnFirstThread</code> VM argument
	 *
	 * @since 3.2.200
	 */
	public static final String XSTART_ON_FIRST_THREAD = "-XstartOnFirstThread"; //$NON-NLS-1$
	public static final String JAVA_JVM_VERSION = "JAVA_JVM_VERSION"; //$NON-NLS-1$
	
	/**
	 * The VM install instance
	 */
	protected IVMInstall fVMInstance;

	/**
	 * Constructor
	 * @param vmInstance the VM
	 */
	public StandardVMRunner(IVMInstall vmInstance) {
		fVMInstance= vmInstance;
	}

	// TODO Implement this
	protected File findJavaExecutable() {
		File exe = null;
		if (fVMInstance instanceof StandardVM) {
			exe = ((StandardVM)fVMInstance).getJavaExecutable();
		} else {
			exe = StandardVMType.findJavaExecutable(fVMInstance.getInstallLocation());
		}
		return exe;
	}
	
	protected String getJavaInstallLocation() {
		return fVMInstance.getInstallLocation().getAbsolutePath() + File.separatorChar;
	}
	
	public String getVMName() {
		return fVMInstance.getName();
	}
	
	/**
	 * Returns the 'rendered' name for the current target
	 * @param classToRun the class
	 * @param host the host name
	 * @return the name for the current target
	 */
	protected String renderDebugTarget(String classToRun, int host) {
		String format= StandardVMRunner__0__at_localhost__1__1;
		return NLS.bind(format, new String[] { classToRun, String.valueOf(host) });
	}

	/**
	 * Returns the 'rendered' name for the specified command line
	 * @param commandLine the command line
	 * @param timestamp the run-at time for the process
	 * @return the name for the process
	 */
	public static String renderProcessLabel(String[] commandLine, String timestamp) {
		String format= StandardVMRunner__0____1___2;
		return NLS.bind(format, new String[] { commandLine[0], timestamp });
	}

	/**
	 * Prepares the command line from the specified array of strings
	 * @param commandLine the command line
	 * @return the command line label
	 */
	protected String renderCommandLine(String[] commandLine) {
		return ArgumentUtils.renderArguments(commandLine, null);
	}

	/**
	 * Adds the array of {@link String}s to the given {@link List}
	 * @param args the strings
	 * @param v the list
	 */
	protected void addArguments(String[] args, List<String> v) {
		if (args == null) {
			return;
		}
		for (int i= 0; i < args.length; i++) {
			v.add(args[i]);
		}
	}

	/**
	 * This method allows consumers to have a last look at the command line that will be used
	 * to start the runner just prior to launching. This method returns the new array of commands
	 * to use to start the runner with or <code>null</code> if the existing command line should be used.
	 * <br><br>
	 * By default this method returns <code>null</code> indicating that the existing command line should be used to launch
	 *
	 * @param configuration the backing {@link ILaunchConfiguration}
	 * @param cmdLine the existing command line
	 * @return the new command line to launch with or <code>null</code> if the existing one should be used
	 * @since 3.7.0
	 */
	protected String[] validateCommandLine(ILaunch configuration, String[] cmdLine) {
		try {
			return wrap(configuration, cmdLine);
		}
		catch(CoreException ce) {
			LaunchingCore.log(ce);
		}
		return cmdLine;
	}

	/**
	 * Adds in special command line arguments if SWT or the <code>-ws</code> directive
	 * are used
	 *
	 * @param config the backing {@link ILaunchConfiguration}
	 * @param cmdLine the original VM arguments
	 * @return the (possibly) modified command line to launch with
	 * @throws CoreException
	 */
	private String[] wrap(ILaunch config, String[] cmdLine) throws CoreException {
		if(config != null && OSUtils.isMac()) {
			for (int i= 0; i < cmdLine.length; i++) {
				if ("-ws".equals(cmdLine[i]) || cmdLine[i].indexOf("swt.jar") > -1 || cmdLine[i].indexOf("org.eclipse.swt") > -1) {   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					return createSWTlauncher(cmdLine,
							cmdLine[0], true);
							//config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_START_ON_FIRST_THREAD, true));
				}
			}
		}
		return cmdLine;
	}

	/**
	 * Returns path to executable.
	 * @param cmdLine the old command line
	 * @param vmVersion the version of the VM
	 * @param startonfirstthread
	 * @return the new command line
	 */
	private String[] createSWTlauncher(String[] cmdLine, String vmVersion, boolean startonfirstthread) {
		// the following property is defined if Eclipse is started via java_swt
		String java_swt= System.getProperty("org.eclipse.swtlauncher");	//$NON-NLS-1$
		if (java_swt == null) {
			// not started via java_swt -> now we require that the VM supports the "-XstartOnFirstThread" option
			boolean found = false;
			ArrayList<String> args = new ArrayList<>();
			for (int i = 0; i < cmdLine.length; i++) {
				if(XSTART_ON_FIRST_THREAD.equals(cmdLine[i])) {
					found = true;
				}
				args.add(cmdLine[i]);
			}
			//newer VMs and non-MacOSX VMs don't like "-XstartOnFirstThread"
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=211625
			if(!found && startonfirstthread) {
				//add it as the first VM argument
				args.add(1, XSTART_ON_FIRST_THREAD);
			}
			return args.toArray(new String[args.size()]);
		}
		try {
			// copy java_swt to /tmp in order to get the app name right
			Process process= Runtime.getRuntime().exec(new String[] { "/bin/cp", java_swt, "/tmp" }); //$NON-NLS-1$ //$NON-NLS-2$
			process.waitFor();
			java_swt= "/tmp/java_swt"; //$NON-NLS-1$
		} catch (IOException e) {
			// ignore and run java_swt in place
		} catch (InterruptedException e) {
			// ignore and run java_swt in place
		}
		String[] newCmdLine= new String[cmdLine.length+1];
		int argCount= 0;
		newCmdLine[argCount++]= java_swt;
		newCmdLine[argCount++]= "-XXvm=" + vmVersion; //$NON-NLS-1$
		for (int i= 1; i < cmdLine.length; i++) {
			newCmdLine[argCount++]= cmdLine[i];
		}
		return newCmdLine;
	}

	/**
	 * Returns the working directory to use for the launched VM,
	 * or <code>null</code> if the working directory is to be inherited
	 * from the current process.
	 *
	 * @param config the VM configuration
	 * @return the working directory to use
	 * @exception CoreException if the working directory specified by
	 *  the configuration does not exist or is not a directory
	 */
	protected File getWorkingDir(VMRunnerConfiguration config) throws CoreException {
		String path = config.getWorkingDirectory();
		if (path == null) {
			return null;
		}
		File dir = new File(path);
		if (!dir.isDirectory()) {
			abort(NLS.bind(StandardVMRunner_Specified_working_directory_does_not_exist_or_is_not_a_directory___0__3, new String[] {path}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
		}
		return dir;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMRunner#getPluginIdentifier()
	 */
	@Override
	protected String getPluginIdentifier() {
		return "org.eclipse.jdt.launching";
	}

	
	/**
	 * Construct and return a String containing the full path of a java executable
	 * command such as 'java' or 'javaw.exe'.  If the configuration specifies an
	 * explicit executable, that is used.
	 *
	 * @param config the runner configuration
	 * @return full path to java executable
	 * @exception CoreException if unable to locate an executable
	 */
	protected String constructProgramString(VMRunnerConfiguration config) throws CoreException {

		// Look for the user-specified java executable command
		String command= null;
		Map<String, Object> map= config.getVMSpecificAttributesMap();
		if (map != null) {
			command = (String) map.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
		}

		// If no java command was specified, use default executable
		if (command == null) {
			File exe = findJavaExecutable();
			if (exe == null) {
				abort(NLS.bind(StandardVMRunner_Unable_to_locate_executable_for__0__1, new String[]{getVMName()}), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			} else {
				return exe.getAbsolutePath();
			}
		}

		// Build the path to the java executable.  First try 'bin', and if that
		// doesn't exist, try 'jre/bin'
		String installLocation = getJavaInstallLocation();
		File exe = new File(installLocation + "bin" + File.separatorChar + command); //$NON-NLS-1$
		if (fileExists(exe)){
			return exe.getAbsolutePath();
		}
		exe = new File(exe.getAbsolutePath() + ".exe"); //$NON-NLS-1$
		if (fileExists(exe)){
			return exe.getAbsolutePath();
		}
		exe = new File(installLocation + "jre" + File.separatorChar + "bin" + File.separatorChar + command); //$NON-NLS-1$ //$NON-NLS-2$
		if (fileExists(exe)) {
			return exe.getAbsolutePath();
		}
		exe = new File(exe.getAbsolutePath() + ".exe"); //$NON-NLS-1$
		if (fileExists(exe)) {
			return exe.getAbsolutePath();
		}

		// not found
		abort(NLS.bind(StandardVMRunner_Specified_executable__0__does_not_exist_for__1__4, new String[]{command, getVMName()}), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		// NOTE: an exception will be thrown - null cannot be returned
		return null;
	}

	/**
	 * Convenience method to determine if the specified file exists or not
	 * @param file the file to check
	 * @return true if the file indeed exists, false otherwise
	 */
	protected boolean fileExists(File file) {
		return file.exists() && file.isFile();
	}

	protected String convertClassPath(String[] cp) {
		int pathCount= 0;
		StringBuffer buf= new StringBuffer();
		if (cp.length == 0) {
			return "";    //$NON-NLS-1$
		}
		for (int i= 0; i < cp.length; i++) {
			if (pathCount > 0) {
				buf.append(File.pathSeparator);
			}
			buf.append(cp[i]);
			pathCount++;
		}
		return buf.toString();
	}

	/**
	 * This method is used to ensure that the JVM file encoding matches that of the console preference for file encoding.
	 * If the user explicitly declares a file encoding in the launch configuration, then that file encoding is used.
	 *
	 * @param launch the {@link Launch}
	 * @param vmargs the original listing of JVM arguments
	 * @return the listing of JVM arguments including file encoding if one was not specified
	 *
	 * @since 3.4
	 */
	protected String[] ensureEncoding(ILaunch launch, String[] vmargs) {
		boolean foundencoding = false;
		for(int i = 0; i < vmargs.length; i++) {
			if(vmargs[i].startsWith("-Dfile.encoding=")) { //$NON-NLS-1$
				foundencoding = true;
			}
		}
		if(!foundencoding) {
			String encoding = launch.getAttribute(DebugPluginConstants.ATTR_CONSOLE_ENCODING);
			if(encoding == null) {
				return vmargs;
			}
			String[] newargs = new String[vmargs.length+1];
			System.arraycopy(vmargs, 0, newargs, 0, vmargs.length);
			newargs[newargs.length-1] = "-Dfile.encoding="+encoding; //$NON-NLS-1$
			return newargs;
		}
		return vmargs;
	}

	public CommandLineDetails getCommandLineDetails(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor subMonitor) throws CoreException {
		String program= constructProgramString(config);

		List<String> arguments= new ArrayList<>();
		arguments.add(program);

		// VM args are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first option
		String[] allVMArgs = combineVmArgs(config, new String[] {}); //fVMInstance);
		addArguments(ensureEncoding(launch, allVMArgs), arguments);

		addBootClassPathArguments(arguments, config);

		String[] cp= config.getClassPath();
		int cpidx = -1;
		if (cp.length > 0) {
			cpidx = arguments.size();
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(cp));
		}
		arguments.add(config.getClassToLaunch());

		String[] programArgs= config.getProgramArguments();
		addArguments(programArgs, arguments);

		String[] envp = prependJREPath(config.getEnvironment());

		String[] newenvp = checkClasspath(arguments, cp, envp);
		if(newenvp != null) {
			envp = newenvp;
			arguments.remove(cpidx);
			arguments.remove(cpidx);
		}

		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);

		subMonitor.worked(1);

		// check for cancellation
		if (subMonitor.isCanceled()) {
			return null;
		}

		subMonitor.subTask(StandardVMRunner_Starting_virtual_machine____3);
		File workingDir = getWorkingDir(config);
		String wd = workingDir == null ? null : workingDir.getAbsolutePath();
		String[] newCmdLine = validateCommandLine(launch, cmdLine);
		if(newCmdLine != null) {
			cmdLine = newCmdLine;
		}
		return new CommandLineDetails(cmdLine, wd, newenvp, new HashMap<String,String>());
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMRunner#run(org.eclipse.jdt.launching.VMRunnerConfiguration, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		run(config, launch, false, monitor);
	}
	protected void run(VMRunnerConfiguration config, ILaunch launch, boolean destroyAtEnd, IProgressMonitor monitor) throws CoreException {


		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		subMonitor.beginTask(StandardVMRunner_Launching_VM____1, 2);
		subMonitor.subTask(StandardVMRunner_Constructing_command_line____2);
		
		CommandLineDetails det = getCommandLineDetails(config, launch, subMonitor);

		Process p= null;
		File workingDir = (det.getWorkingDir() == null ? null : new File(det.getWorkingDir()));
		p= exec(det.getCmdLine(), workingDir, det.getEnvp());
		if (p == null) {
			return;
		}

		// check for cancellation
		if (monitor.isCanceled()) {
			p.destroy();
			return;
		}
		String timestamp = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a").format(new Date(System.currentTimeMillis()));
		IProcess process= newProcess(launch, p, renderProcessLabel(det.getCmdLine(), timestamp), getDefaultProcessMap());
		process.setAttribute(DebugPluginConstants.ATTR_PATH, det.getCmdLine()[0]);
		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(det.getCmdLine()));
		String ltime = launch.getAttribute(DebugPluginConstants.ATTR_LAUNCH_TIMESTAMP);
		process.setAttribute(DebugPluginConstants.ATTR_LAUNCH_TIMESTAMP, ltime != null ? ltime : timestamp);
		if(det.getWorkingDir() != null) {
			process.setAttribute(DebugPluginConstants.ATTR_WORKING_DIRECTORY, det.getWorkingDir());
		}
		if(det.getEnvp() != null) {
			Arrays.sort(det.getEnvp());
			StringBuffer buff = new StringBuffer();
			for (int i = 0; i < det.getEnvp().length; i++) {
				buff.append(det.getEnvp()[i]);
				if(i < det.getEnvp().length-1) {
					buff.append('\n');
				}
			}
			process.setAttribute(DebugPluginConstants.ATTR_ENVIRONMENT, buff.toString());
		}
		subMonitor.worked(1);
		subMonitor.done();
		
		if( destroyAtEnd && p != null ) {
			p.destroy();
		}
	}

	/**
	 * Returns the index in the given array for the CLASSPATH variable
	 * @param env the environment array or <code>null</code>
	 * @return -1 or the index of the CLASSPATH variable
	 * @since 3.6.200
	 */
	int getCPIndex(String[] env) {
		if(env != null) {
			for (int i = 0; i < env.length; i++) {
				if(env[i].regionMatches(true, 0, "CLASSPATH=", 0, 10)) { //$NON-NLS-1$
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Checks to see if the command / classpath needs to be shortened for Windows. Returns the modified
	 * environment or <code>null</code> if no changes are needed.
	 *
	 * @param args the raw arguments from the runner
	 * @param cp the raw classpath from the runner configuration
	 * @param env the current environment
	 * @return the modified environment or <code>null</code> if no changes were made
	 * @sine 3.6.200
	 */
	String[] checkClasspath(List<String> args, String[] cp, String[] env) {
		if(OSUtils.isWindows()) {
			//count the complete command length
			int size = 0;
			for (String arg : args) {
				if(arg != null) {
					size += arg.length();
				}
			}
			//greater than 32767 is a no-go
			//see http://msdn.microsoft.com/en-us/library/windows/desktop/ms682425(v=vs.85).aspx
			if(size > 32767) {
				StringBuffer newcp = new StringBuffer("CLASSPATH="); //$NON-NLS-1$
				for (int i = 0; i < cp.length; i++) {
					newcp.append(cp[i]);
					newcp.append(File.pathSeparatorChar);
				}
				String[] newenvp = null;
				int index = -1;
				if(env == null) {
					Map<String, String> nenv = NativeEnvironmentUtils.getDefault().getNativeEnvironment();
					Entry<String, String> entry = null;
					newenvp = new String[nenv.size()];
					int idx = 0;
					for (Iterator<Entry<String, String>> i = nenv.entrySet().iterator(); i.hasNext();) {
						entry = i.next();
						String value = entry.getValue();
						if(value == null) {
							value = ""; //$NON-NLS-1$
						}
						String key = entry.getKey();
						if(key.equalsIgnoreCase("CLASSPATH")) { //$NON-NLS-1$
							index = idx;
						}
						newenvp[idx] = key+'='+value;
						idx++;
					}
				}
				else {
					newenvp = env;
					index = getCPIndex(newenvp);
				}
				if(index < 0) {
					String[] newenv = new String[newenvp.length+1];
					System.arraycopy(newenvp, 0, newenv, 0, newenvp.length);
					newenv[newenvp.length] = newcp.toString();
					return newenv;
				}
				newenvp[index] = newcp.toString();
				return newenvp;
			}
		}
		return null;
	}

	/**
	 * Prepends the correct java version variable state to the environment path for Mac VMs
	 *
	 * @param env the current array of environment variables to run with
	 * @return the new path segments
	 * @since 3.3
	 */
	protected String[] prependJREPath(String[] env) {
		if (OSUtils.isMac()) {
			String javaVersion = fVMInstance.getJavaVersion();
			if (javaVersion != null) {
				if (env == null) {
					Map<String, String> map = NativeEnvironmentUtils.getDefault().getNativeEnvironmentCasePreserved();
					if (map.containsKey(JAVA_JVM_VERSION)) {
						String[] env2 = new String[map.size()];
						Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
						int i = 0;
						while (iterator.hasNext()) {
							Entry<String, String> entry = iterator.next();
							String key = entry.getKey();
							if (JAVA_JVM_VERSION.equals(key)) {
								env2[i] = key + "=" + javaVersion; //$NON-NLS-1$
							} else {
								env2[i] = key + "=" + entry.getValue(); //$NON-NLS-1$
							}
							i++;
						}
						env = env2;
					}
				} else {
					for (int i = 0; i < env.length; i++) {
						String string = env[i];
						if (string.startsWith(JAVA_JVM_VERSION)) {
							env[i]=JAVA_JVM_VERSION+"="+javaVersion; //$NON-NLS-1$
							break;
						}
					}
				}
			}
		}
		return env;
	}

	
	/**
	 * Adds arguments to the bootpath
	 * @param arguments the arguments
	 * @param config the VM config
	 */
	protected void addBootClassPathArguments(List<String> arguments, VMRunnerConfiguration config) {
		String[] prependBootCP= null;
		String[] bootCP= null;
		String[] appendBootCP= null;
		Map<String, Object> map = config.getVMSpecificAttributesMap();
		if (map != null) {
			prependBootCP= (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND);
			bootCP= (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH);
			appendBootCP= (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND);
		}
		if (prependBootCP == null && bootCP == null && appendBootCP == null) {
			// use old single attribute instead of new attributes if not specified
			bootCP = config.getBootClassPath();
		}
		if (prependBootCP != null) {
			arguments.add("-Xbootclasspath/p:" + convertClassPath(prependBootCP)); //$NON-NLS-1$
		}
		if (bootCP != null) {
			if (bootCP.length > 0) {
				arguments.add("-Xbootclasspath:" + convertClassPath(bootCP)); //$NON-NLS-1$
			}
		}
		if (appendBootCP != null) {
			arguments.add("-Xbootclasspath/a:" + convertClassPath(appendBootCP)); //$NON-NLS-1$
		}
	}

}
