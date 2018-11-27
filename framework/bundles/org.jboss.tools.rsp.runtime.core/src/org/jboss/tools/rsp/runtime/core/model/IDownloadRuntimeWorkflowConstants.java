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

/**
 * Shared Constants for use by the task wizard and 
 * also used by the installer API / extraction algos.  
 * 
 */
public interface IDownloadRuntimeWorkflowConstants {
	/**
	 * A taskmodel key for a Map<String,DownloadRuntime>  
	 * full of downloadRuntime objects
	 */
	public static final String DL_RUNTIME_MAP = "dl.runtime.map.prop";  //$NON-NLS-1$
	/**
	 * A taskmodel key for accessing the currently-selected
	 * DownloadRuntime object
	 */
	public static final String DL_RUNTIME_PROP = "dl.runtime.prop"; //$NON-NLS-1$

	
	/**
	 * A taskmodel key for accessing the currently-selected
	 * DownloadRuntime object's URL in the event that 
	 * an authenticator WizardFragment has acquired a URL for use.
	 * The value should be of type String
	 */
	public static final String DL_RUNTIME_URL = "dl.runtime.url"; //$NON-NLS-1$

	/**
	 * Username key
	 */
	public static final String USERNAME_KEY = "dl.runtime.username"; //$NON-NLS-1$

	/**
	 * Password key
	 */
	public static final String PASSWORD_KEY = "dl.runtime.password"; //$NON-NLS-1$


	/**
	 * A key to suppress creation of the runtimes, and to only
	 * perform the download and the unzip.  The value of this key 
	 * should be a Boolean or boolean. 
	 */
	public static final String SUPPRESS_RUNTIME_CREATION = "dl.runtime.suppressCreation"; //$NON-NLS-1$


	/**
	 * A key used to hold the job that is executed once the wizard is completed.
	 * This job is downloading, unzipping, and (possibly) creating the runtimes.   
	 */
	public static final String DOWNLOAD_JOB = "dl.runtime.downloadJob"; //$NON-NLS-1$

	
	/**
	 * This is a delegating progress monitor so that 2 monitors can
	 * receive the same updates. 
	 */
	public static final String DOWNLOAD_JOB_DELEGATING_PROGRESS_MONITOR = "dl.runtime.progress.monitor"; //$NON-NLS-1$
	
	
	/**
	 * A field to be added to the task model that indicates where the unzipped runtime lives.
	 */
	public static final String UNZIPPED_SERVER_HOME_DIRECTORY = "dl.runtime.unzipped.home.dir"; //$NON-NLS-1$
	
	/**
	 * A field to be added to the task model that indicates where a download binary file's full path is.
	 */
	public static final String UNZIPPED_SERVER_BIN = "dl.runtime.unzipped.home.dir.bin"; //$NON-NLS-1$
	

	/**
	 * A constant representing an IOverwrite object to be queried for questions on overwriting files
	 */
	public static final String OVERWRITE = "dl.runtime.overwrite"; //$NON-NLS-1$
	
}
