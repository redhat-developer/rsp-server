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
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMInstallRegistry;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
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

	private static final String SECURESTORAGE_DIRECTORY = "securestorage";

	private static ServerManagementModel instance;


	/** for testing purposes **/
	public static ServerManagementModel getDefault() {
		if (instance == null) {
			instance = new ServerManagementModel();
		}
		return instance;
	}

	private ISecureStorageProvider secureStorage;
	private ICapabilityManagement capabilities;

	private IDiscoveryPathModel rpm;
	private IServerBeanTypeManager serverBeanTypeManager;
	private IServerModel serverModel;
	private IVMInstallRegistry vmModel;

	private ServerManagementModel() {
		this(LaunchingCore.getDataLocation());
	}
	
	/** protected for testing purposes **/
	protected ServerManagementModel(File dataLocation) {
		this.capabilities = createCapabilityManagement();
		this.secureStorage = createSecureStorageProvider(getSecureStorageFile(dataLocation), capabilities);
		this.rpm = createDiscoveryPathModel();
		this.serverBeanTypeManager = createServerBeanTypeManager();
		this.serverModel = createServerModel(secureStorage);
		this.vmModel = createVMInstallRegistry();
		this.vmModel.addActiveVM();
	}
	
	public ISecureStorageProvider getSecureStorageProvider() {
		return secureStorage;
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

	public IVMInstallRegistry getVMInstallModel() {
		return vmModel;
	}

	public ICapabilityManagement getCapabilityManagement() {
		return capabilities;
	}

	public void removeClient(RSPClient client) {
		capabilities.clientRemoved(client);
		if( secureStorage instanceof SecureStorageGuardian ) 
			((SecureStorageGuardian)secureStorage).removeClient(client);
	}

	public void clientAdded(RSPClient client) {
		capabilities.clientAdded(client);
	}

	private File getSecureStorageFile(File dataLocation) {
		File secure = new File(dataLocation, SECURESTORAGE_DIRECTORY);
		return secure;
	}

	/*
	 * Following methods are for tests / subclasses to override. This is not advised
	 * for clients / extenders, since this class appears to behave as a singleton.
	 */
	protected ISecureStorageProvider createSecureStorageProvider(File file, ICapabilityManagement mgmt) {
		return new SecureStorageGuardian(file, mgmt);
	}

	protected ICapabilityManagement createCapabilityManagement() {
		return new CapabilityManagement();
	}

	protected IDiscoveryPathModel createDiscoveryPathModel() {
		return new DiscoveryPathModel();
	}

	protected VMInstallRegistry createVMInstallRegistry() {
		return new VMInstallRegistry();
	}

	protected IServerModel createServerModel(ISecureStorageProvider secure) {
		return new ServerModel(secure);
	}

	protected IServerBeanTypeManager createServerBeanTypeManager() {
		return new ServerBeanTypeManager();
	}
}
