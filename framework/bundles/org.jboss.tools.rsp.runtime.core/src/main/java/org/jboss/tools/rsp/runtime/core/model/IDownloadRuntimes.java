/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core.model;

import java.util.Map;

/**
 * A poorly named interface for some object which is
 * capable of downloading runtimes. 
 */ 
public interface IDownloadRuntimes {
	/**
	 * Should be a ui Shell object 
	 */
	public static final String SHELL = "download.runtimes.shell"; //$NON-NLS-1$
	
	/**
	 * May be used to limit the number of items showing up in the
	 * download runtime dialog. 
	 */
	public static final String RUNTIME_FILTER = "download.runtimes.filter"; //$NON-NLS-1$

	/**
	 * Is the download initialized (true) or canceled (false)?
	 */
	public static final String DOWNLOAD_LAUNCHED = "is.download.launched"; //$NON-NLS-1$
	

	/**
	 * Begin the workflow of downloading DownloadRuntime objects
	 * based on the properties in the given map. 
	 * 
	 * @param data
	 */ 
	public void execute(Map<String, Object> data);
}
