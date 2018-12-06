/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.foundation.core.launchers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.ArgumentUtils;
import org.jboss.tools.rsp.eclipse.debug.core.DebugPluginConstants;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.foundation.core.FoundationCoreActivator;
import org.jboss.tools.rsp.launching.utils.ExecUtil;

public class ProcessUtility {

	protected static final String StandardVMRunner__0____1___2="{0} ({1})";
	public static final String AbstractVMRunner_0="An IProcess could not be created for the launch";
	public IProcess createIProcess(ILaunch launch, Process p, CommandLineDetails det) throws CoreException {
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
			StringBuilder buff = new StringBuilder();
			for (int i = 0; i < det.getEnvp().length; i++) {
				buff.append(det.getEnvp()[i]);
				if(i < det.getEnvp().length-1) {
					buff.append('\n');
				}
			}
			process.setAttribute(DebugPluginConstants.ATTR_ENVIRONMENT, buff.toString());
		}
		return process;
	}
	
	public IProcess newProcess(ILaunch launch, Process p, String label, Map<String, String> attributes) throws CoreException {
		IProcess process= ExecUtil.newProcess(launch, p, label, attributes);
		if (process == null) {
			p.destroy();
			abort(AbstractVMRunner_0, null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return process;
	}
	
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, FoundationCoreActivator.PLUGIN_ID, code, message, exception));
	}
	
	/**
	 * Returns the default process attribute map for Java processes.
	 *
	 * @return default process attribute map for Java processes
	 */
	protected Map<String, String> getDefaultProcessMap() {
		Map<String, String> map = new HashMap<>();
		// TODO change this?!
		map.put(IProcess.ATTR_PROCESS_TYPE, "unknown");
		return map;
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
	 * Returns the 'rendered' name for the specified command line
	 * @param commandLine the command line
	 * @param timestamp the run-at time for the process
	 * @return the name for the process
	 */
	protected static String renderProcessLabel(String[] commandLine, String timestamp) {
		String format= StandardVMRunner__0____1___2;
		return NLS.bind(format, new String[] { commandLine[0], timestamp });
	}

	public Process callProcess(String rootCommand, String[] args, String dir, String[] envp) throws IOException {
		List<String> cmd = new ArrayList<>();
		cmd.add(rootCommand);
		cmd.addAll(Arrays.asList(args));
		Process p = null;
		p = Runtime.getRuntime().exec(cmd.toArray(new String[0]), envp, new File(dir));
		return p;
	}
	
	public Process callProcess(String rootCommand, String[] args, String dir, Map<String,String> envMap) throws IOException {
		String[] envp = convertEnvironment(envMap);
		return callProcess(rootCommand, args, dir, envp);
	}
	
	/*
	 * Convert a string/string hashmap into an array of string environment variables
	 * as required by java.lang.Runtime This will super-impose the provided
	 * environment variables ON TOP OF the existing environment in eclipse, as users
	 * may not know *all* environment variables that need to be set, or to do so may
	 * be tedious.
	 */
	public static String[] convertEnvironment(Map<String, String> env) {
		if (env == null || env.size() == 0)
			return null;

		// Create a new map based on pre-existing environment of Eclipse
		Map<String, String> original = new HashMap<>(System.getenv());

		// Add additions or changes to environment on top of existing
		original.putAll(env);

		// Convert the combined map into a form that can be used to launch process
		ArrayList<String> ret = new ArrayList<>();
		Iterator<String> it = original.keySet().iterator();
		String working = null;
		while (it.hasNext()) {
			working = it.next();
			ret.add(working + "=" + original.get(working)); //$NON-NLS-1$
		}
		return ret.toArray(new String[ret.size()]);
	}
	
	public String[] callMachineReadable(String rootCommand, String[] args, String wd,
			Map<String, String> env) throws IOException, CommandTimeoutException {
		return call(rootCommand, args, wd, env, 30000);
	}

	public String[] callMachineReadable(String rootCommand, String[] args, String wd,
			String[] env) throws IOException, CommandTimeoutException {
		return call(rootCommand, args, wd, env, 30000);
	}

	public String[] call(String rootCommand, String[] args, String wd, 
			Map<String, String> env, int timeout) throws IOException, CommandTimeoutException {
		return call(rootCommand, args, wd, convertEnvironment(env), timeout);
	}
	
	public String[] call(String rootCommand, String[] args, String wd, 
			String[] env,int timeout) throws IOException, CommandTimeoutException {

		final Process p = callProcess(rootCommand, args, wd, env);

		InputStream errStream = p.getErrorStream();
		InputStream inStream = p.getInputStream();

		StreamGobbler inGob = new StreamGobbler(inStream);
		StreamGobbler errGob = new StreamGobbler(errStream);

		inGob.start();
		errGob.start();

		Integer exitCode = null;
		if (p.isAlive()) {

			exitCode = runWithTimeout(timeout, new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return p.waitFor();
				}
			});
		} else {
			exitCode = p.exitValue();
		}

		List<String> inLines = null;
		if (exitCode == null) {
			inGob.cancel();
			errGob.cancel();

			// Timeout reached
			p.destroyForcibly();
			inLines = inGob.getOutput();
			List<String> errLines = errGob.getOutput();
			throw new CommandTimeoutException(inLines, errLines);
		} else {
			inLines = inGob.getOutput();
		}

		return inLines.toArray(new String[inLines.size()]);
	}

	/**
	 * Runs and blocking waits for the given callable to finish for the given
	 * time. Returns <code>null</code> if timeouts waiting for callable value.
	 * 
	 * @param millisTimeout
	 * @param callable
	 * @return
	 */
	public static <R> R runWithTimeout(long millisTimeout, Callable<R> callable) {
		ExecutorService singleThreadExecutor = Executors.newFixedThreadPool(1);
		Future<R> future = singleThreadExecutor.submit(callable);
		try {
			return future.get(millisTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
		} finally {
			singleThreadExecutor.shutdown();
		}
		return null;
	}

}
