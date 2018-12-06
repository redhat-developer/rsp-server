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
package org.jboss.tools.rsp.runtime.core.model;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;

public interface IRuntimeInstaller {
	
	/**
	 * The constant representing the default installation method, extracting the archive
	 */
	public static final String EXTRACT_INSTALLER = "archive"; //$NON-NLS-1$
	
	
	/**
	 * The constant representing an alternate installation method:  running java -jar on the archive
	 */
	public static final String JAVA_JAR_INSTALLER = "installer-jar"; //$NON-NLS-1$

	public static final String BINARY_INSTALLER = "binary";
	
	/**
	 * Download and install the given runtime. 
	 * 
	 * @param dlrt
	 * @param selectedDirectory
	 * @param destinationDirectory
	 * @param deleteOnExit
	 * @param taskModel
	 * @param monitor
	 * @return
	 */
	public IStatus installRuntime(
			DownloadRuntime dlrt, String unzipDirectory, String downloadDirectory,
			final boolean deleteOnExit, TaskModel taskModel, IProgressMonitor monitor);

}
