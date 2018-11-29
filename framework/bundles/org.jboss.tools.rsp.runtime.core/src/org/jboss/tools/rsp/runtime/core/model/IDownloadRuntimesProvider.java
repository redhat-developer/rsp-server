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

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;

/**
 * An interface representing an entity that wishes
 * to provide downloadable runtimes.
 * 
 * This interface reserves the right to add static final 
 * property strings with the PROPERTY prefix. Implementers
 * of this interface should be aware of this. 
 */
public interface IDownloadRuntimesProvider {

	/**
	 * A generic request for all of the provider's available downloadRuntimes
	 */
	public static final String PROPERTY_GENERIC_REQUEST = "IDownloadRuntimesProvider.GENERIC_REQUEST"; //$NON-NLS-1$
	
	/**
	 * A unique id to reference this provider of runtimes
	 * @return
	 */
	public String getId();
	
	
	/**
	 * A method to return the provider's list of downloadable runtimes.
	 * This returned array may be customized according to the value of requestType
	 * 
	 * @param monitor A progress Monitor
	 * @return
	 */
	public DownloadRuntime[] getDownloadableRuntimes(IProgressMonitor monitor);
}
