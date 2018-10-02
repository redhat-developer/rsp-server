/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import java.io.File;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMInstallRegistry;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.server.CapabilityManagement;
import org.jboss.tools.rsp.server.discovery.DiscoveryPathModel;
import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanTypeManager;
import org.jboss.tools.rsp.server.secure.SecureStorageGuardian;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathModel;
import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeManager;
import org.jboss.tools.rsp.server.spi.model.ICapabilityManagement;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.IServerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerManagementModel implements IServerManagementModel {
	private static final Logger LOG = LoggerFactory.getLogger(ServerManagementModel.class);

	private static ServerManagementModel instance;
	public static ServerManagementModel getDefault() {
		return instance;
	}

	
	private SecureStorageGuardian secureStorage;
	private CapabilityManagement capabilities;

	private DiscoveryPathModel rpm;
	private ServerBeanTypeManager serverBeanTypeManager;
	private ServerModel serverModel;
	private VMInstallRegistry vmModel;
	
	public ServerManagementModel() {
		this.capabilities = new CapabilityManagement();
		this.secureStorage = new SecureStorageGuardian(getSecureStorageFile(), capabilities);
		this.rpm = new DiscoveryPathModel();
		this.serverBeanTypeManager = new ServerBeanTypeManager();
		this.serverModel = new ServerModel(secureStorage);
		this.vmModel = new VMInstallRegistry();
		this.vmModel.addActiveVM();
		
		instance = this;
	}
	
	public IDiscoveryPathModel getDiscoveryPathModel() {
		return rpm;
	}
	
	public IServerBeanTypeManager getServerBeanTypeManager() {
		return serverBeanTypeManager;
	}
	
	public IServerModel getServerModel() {
		return serverModel;
	}

	public VMInstallRegistry getVMInstallModel() {
		return vmModel;
	}
	
	public ICapabilityManagement getCapabilityManagement() {
		return capabilities;
	}

	public void removeClient(RSPClient client) {
		capabilities.clientRemoved(client);
	}

	public void clientAdded(RSPClient client) {
		capabilities.clientAdded(client);
	}
	
	private File getSecureStorageFile() {
		File data = LaunchingCore.getDataLocation();
		File secure = new File(data, "securestorage");
		return secure;
	}

}
