/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jboss.tools.rsp.eclipse.jdt.launching;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMRunnerConfiguration;



/**
 * A VM runner starts a Java VM running a Java program.
 * <p>
 * Clients may implement this interface to launch a new kind of VM.
 * </p>
 */
public interface IVMRunner {

	/**
	 * Launches a Java VM as specified in the given configuration,
	 * contributing results (debug targets and processes), to the
	 * given launch.
	 *
	 * @param configuration the configuration settings for this run
	 * @param launch the launch to contribute to
	 * @param monitor progress monitor or <code>null</code> A cancelable progress monitor is provided by the Job
	 *  framework. It should be noted that the setCanceled(boolean) method should never be called on the provided
	 *  monitor or the monitor passed to any delegates from this method; due to a limitation in the progress monitor
	 *  framework using the setCanceled method can cause entire workspace batch jobs to be canceled, as the canceled flag
	 *  is propagated up the top-level parent monitor. The provided monitor is not guaranteed to have been started.
	 * @exception CoreException if an exception occurs while launching
	 */
	public void run(VMRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException;

}
