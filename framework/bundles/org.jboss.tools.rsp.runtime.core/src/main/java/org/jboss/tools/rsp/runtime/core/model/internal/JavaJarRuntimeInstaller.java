/*******************************************************************************
 * Copyright (c) 2015 Red Hat 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JBoss by Red Hat
 *******************************************************************************/
package org.jboss.tools.rsp.runtime.core.model.internal;

import java.io.File;
import java.util.HashMap;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.rsp.eclipse.debug.core.ArgumentUtils;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.Launch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMInstallRegistry;
import org.jboss.tools.rsp.foundation.core.launchers.CommandConfig;
import org.jboss.tools.rsp.foundation.core.launchers.GenericProcessRunner;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.launching.utils.NativeEnvironmentUtils;
import org.jboss.tools.rsp.launching.utils.OSUtils;
import org.jboss.tools.rsp.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowConstants;
import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.util.internal.DownloadRuntimeOperationUtility;

/**
 * A runtime installer that launches the java -jar command on the downloaded file
 * 
 */
public class JavaJarRuntimeInstaller implements IRuntimeInstaller {

	public static final String ID = IRuntimeInstaller.JAVA_JAR_INSTALLER;
	
	public JavaJarRuntimeInstaller() {
		// for debugging
	}
	

	@Override
	public IStatus installRuntime(DownloadRuntime downloadRuntime, String unzipDirectory, String downloadDirectory,
			boolean deleteOnExit, TaskModel taskModel, IProgressMonitor monitor) {
		
		String user = (String)taskModel.getObject(IDownloadRuntimeWorkflowConstants.USERNAME_KEY);
		String pass = (String)taskModel.getObject(IDownloadRuntimeWorkflowConstants.PASSWORD_KEY);
		
		monitor.beginTask("Install Runtime '" + downloadRuntime.getName() + "' ...", 100);//$NON-NLS-1$ //$NON-NLS-2$
		monitor.worked(1);
		try {
			File f = new DownloadRuntimeOperationUtility().download(unzipDirectory, downloadDirectory, 
					getDownloadUrl(downloadRuntime, taskModel), deleteOnExit, user, pass, new SubProgressMonitor(monitor, 80));
						
			ILaunch launch = createExternalToolsLaunchConfiguration(f, unzipDirectory);
			if( launch == null ) {
				return new Status(IStatus.ERROR, RuntimeCoreActivator.PLUGIN_ID, "Unable to launch external command java -jar " + f.getAbsolutePath());
			}
			IProcess[] processes = launch.getProcesses();
			boolean finished = false;
			while(!monitor.isCanceled() && !finished) {
				boolean checkFinished = true;
				for( int i = 0; i < processes.length; i++ ) {
					checkFinished &= processes[i].isTerminated();
				}
				finished = checkFinished;
				try { 
					Thread.sleep(500);
				} catch(InterruptedException ie) {
					// Ignore
				}
			}
		} catch(CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}

	private String getDownloadUrl(DownloadRuntime downloadRuntime, TaskModel taskModel) {
		if( downloadRuntime != null ) {
			String dlUrl = downloadRuntime.getUrl();
			if( dlUrl == null ) {
				return (String)taskModel.getObject(IDownloadRuntimeWorkflowConstants.DL_RUNTIME_URL);
			}
			return dlUrl;
		}
		return null;
	}
	
	
	static final String JAVA_HOME_PROPERTY_KEY = "java.home";
	private ILaunch createExternalToolsLaunchConfiguration(File downloadedFile, 
			String unzipDirectory) {
		
		IVMInstall install = createVMRegistry().getDefaultVMInstall();
		IPath javaBin = getJavaBin(install);
		IPath workingDir = new Path(downloadedFile.getAbsolutePath());
		JavaJarInstallationLauncher launcher = new JavaJarInstallationLauncher(javaBin, workingDir, 
				new Path(downloadedFile.getAbsolutePath()), new Path(unzipDirectory));
		ILaunch l2 = launcher.createLaunch("run");
		return l2;
	}
	
	private IVMInstallRegistry createVMRegistry() {
		// This would be better if there were a singleton stored somewhere.
		// Creating this object yet again is kinda resource intensive :|
		VMInstallRegistry reg = new VMInstallRegistry();
		reg.addActiveVM();
		return reg;
	}
	private IPath getJavaBin(IVMInstall install) {
		File javaHome = null;
		if( install != null ) {
			javaHome = install.getInstallLocation();
		} else {
			String jHome = System.getProperty(JAVA_HOME_PROPERTY_KEY);
			javaHome = new File(jHome);
		}
		IPath path = new Path(javaHome.getAbsolutePath());
		if( OSUtils.isWindows()) {
			path = path.append("bin").append("java.exe");
		} else {
			path = path.append("bin").append("java");
		}
		return path;
	}

	private static class JavaJarInstallationLauncher {
		private ILaunch launch;
		private CommandLineDetails launchedDetails = null;
		private GenericProcessRunner runner;
		private IPath javaBin;
		private IPath workingDirectory;
		private IPath unzipDir;
		private IPath downloadedFile;
		
		public JavaJarInstallationLauncher(IPath javaBin, IPath workingDirectory, 
				IPath downloadedFile, IPath unzipDir) {
			this.workingDirectory = workingDirectory;
			this.javaBin = javaBin;
			this.downloadedFile = downloadedFile;
			this.unzipDir = unzipDir;
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

		public String getProgramArguments() {
			return "-DINSTALL_PATH=\"" + unzipDir + "\"  -jar " 
						+ downloadedFile.toOSString();
		}
		
		
		public GenericProcessRunner configureRunner() {
			if( runner == null ) {
				runner = new GenericProcessRunner(getCommandConfig());
			}
			return runner;
		}
		
		protected CommandConfig getCommandConfig() {
			String cmd = javaBin.toOSString();
			String args = getProgramArguments();
			String[] parsed = ArgumentUtils.parseArguments(args);
			String wd = getWorkingDirectory();
			String[] env =	NativeEnvironmentUtils.getDefault().getEnvironment(new HashMap<>(), true);
			CommandConfig details = new CommandConfig(cmd, wd, parsed, env);
			return details;
		}

		public String getWorkingDirectory() {
			return workingDirectory.toOSString();
		}

	}
}
