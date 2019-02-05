/*************************************************************************************
 * Copyright (c) 2013-2018 Red Hat, Inc. and others.
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

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;

public interface IDownloadRuntimesModel {

	/**
	 * Get an array of the currently-registered download runtime providers
	 * 
	 * @return 
	 */
	public IDownloadRuntimesProvider[] getDownloadRuntimeProviders();

	/**
	 * Add a download runtime provider to the model
	 * 
	 * @param provider
	 */
	public void addDownloadRuntimeProvider(IDownloadRuntimesProvider provider);
	
	/**
	 * Remove a download runtime provider from the model
	 * 
	 * @param provider
	 */
	public void removeDownloadRuntimeProvider(IDownloadRuntimesProvider provider);


	/**
	 * Get a map of the current download runtimes id -> DownloadRuntime
	 * 
	 * If the current model's map is empty, execute the possibly long-running 
	 * operation to ensure the model is full. 
	 * 
	 * @param monitor
	 * @return
	 */
	public Map<String, DownloadRuntime> getOrLoadDownloadRuntimes(IProgressMonitor monitor);
	
	/**
	 * Find a download runtime with the given id.
	 *  
	 * If the model is empty, this method will execute the possibly 
	 * long-running operation to ensure that the model is full.
	 *  
	 * @param id
	 * @param monitor
	 * @return
	 */
	public DownloadRuntime findDownloadRuntime(String id, IProgressMonitor monitor);
	
	/**
	 * Find the IDownloadRuntimesProvider for the given runtime id. 
	 * If the model is empty or incomplete, return null. 
	 * 
	 * @param id
	 * @return
	 */
	public IDownloadRuntimesProvider findProviderForRuntime(String id);
	
	/**
	 * Find the IDownloadRuntimesProvider for the given runtime id.
	 *  
	 * This method will execute the potentially long-running task of 
	 * loading the model if it has not yet been loaded. 
	 * 
	 * @param id
	 * @return
	 */
	public IDownloadRuntimesProvider findProviderForRuntime(String id, IProgressMonitor monitor);
}
