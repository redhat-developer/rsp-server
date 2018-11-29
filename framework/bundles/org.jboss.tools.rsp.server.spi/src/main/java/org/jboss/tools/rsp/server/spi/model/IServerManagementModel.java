/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
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

public interface IServerManagementModel {
	public IServerBeanTypeManager getServerBeanTypeManager();
	public IServerModel getServerModel();
	public IFileWatcherService getFileWatcherService();
	public IDiscoveryPathModel getDiscoveryPathModel();
	public IVMInstallRegistry getVMInstallModel();
	public ICapabilityManagement getCapabilityManagement();
	public ISecureStorageProvider getSecureStorageProvider();
	public void clientRemoved(RSPClient client);
	public void clientAdded(RSPClient client);
	public IDownloadRuntimesModel getDownloadRuntimeModel();
}
