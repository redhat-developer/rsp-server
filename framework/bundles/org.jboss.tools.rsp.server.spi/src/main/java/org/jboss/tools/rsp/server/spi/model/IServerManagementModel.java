/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesModel;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathModel;
import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeManager;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherService;
import org.jboss.tools.rsp.server.spi.jobs.IJobManager;

public interface IServerManagementModel {
	/**
	 * Get the sub-model relating to server beans 
	 * @return
	 */
	public IServerBeanTypeManager getServerBeanTypeManager();
	
	/**
	 * Get the sub-model relating to servers
	 * @return
	 */
	public IServerModel getServerModel();
	
	/**
	 * Get the sub-model for the file-watcher service
	 * @return
	 */
	public IFileWatcherService getFileWatcherService();
	
	/**
	 * Get the sub-model for discovery paths
	 * @return
	 */
	public IDiscoveryPathModel getDiscoveryPathModel();
	
	/**
	 * Get the sub-model for VMs
	 * @return
	 */
	public IVMInstallRegistry getVMInstallModel();
	
	/**
	 * Get the sub-model for client capability management
	 * @return
	 */
	public ICapabilityManagement getCapabilityManagement();
	
	/**
	 * Get the secure storage model
	 * @return
	 */
	public ISecureStorageProvider getSecureStorageProvider();
	
	/**
	 * Get the sub model for downloading runtimes
	 * @return
	 */
	public IDownloadRuntimesModel getDownloadRuntimeModel();
	
	
	/**
	 * Get the sub-model for managing long-running tasks
	 * @return
	 */
	public IJobManager getJobManager();
	
	/**
	 * A client has been removed disconnected from the server 
	 * @param client
	 */
	public void clientRemoved(RSPClient client);
	
	/**
	 * A client has been added / connected to the server
	 * @param client
	 */
	public void clientAdded(RSPClient client);
	
}
