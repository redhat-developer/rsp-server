/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugPluginConstants;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.IProcessFactory;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.debug.core.model.RuntimeProcess;

public class ExecUtil {

	public static final int ERROR = 125;
	public static final String DebugPlugin_0="Exception occurred executing command line.";

	private ExecUtil() {
	}

	/**
	 * Convenience method that performs a runtime exec on the given command line
	 * in the context of the specified working directory, and returns the
	 * resulting process. If the current runtime does not support the
	 * specification of a working directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if the
	 * exec should be re-executed without specifying a working directory.
	 *
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *  canceled
	 * @exception CoreException if the exec fails
	 * @see Runtime
	 *
	 * @since 2.1
	 */
	public static Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		return exec(cmdLine, workingDirectory, null);
	}

	/**
	 * Convenience method that performs a runtime exec on the given command line
	 * in the context of the specified working directory, and returns the
	 * resulting process. If the current runtime does not support the
	 * specification of a working directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if the
	 * exec should be re-executed without specifying a working directory.
	 *
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory, or <code>null</code>
	 * @param envp the environment variables set in the process, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *  canceled
	 * @exception CoreException if the exec fails
	 * @see Runtime
	 *
	 * @since 3.0
	 */
	public static Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		Process p= null;
		try {
			if (workingDirectory == null) {
				p = Runtime.getRuntime().exec(cmdLine, envp);
			} else {
				p = Runtime.getRuntime().exec(cmdLine, envp, workingDirectory);
			}
		} catch (IOException e) {
		    Status status = new Status(IStatus.ERROR, DebugPluginConstants.DEBUG_CORE_ID, ERROR, DebugPlugin_0, e);
		    throw new CoreException(status);
		} 
		return p;
	}

	public static IProcess newProcess(ILaunch launch, Process p, String label) {
		return newProcess(launch, p, label, null);
	}	

	public static IProcess newProcess(ILaunch launch, Process p, String label, Map<String, String> attributes) {
		IProcessFactory fact = getProcessFactory(launch, p, label, attributes);
		if (fact != null) {
			return fact.newProcess(launch, p, label, attributes);
		} else {
			return new RuntimeProcess(launch, p, label, attributes);
		}
	}
	
	protected static IProcessFactory getProcessFactory(ILaunch launch, Process p, String label, Map<String, String> attributes) {
		// TODO: implement or get rid of it
		return null;
	}

}
