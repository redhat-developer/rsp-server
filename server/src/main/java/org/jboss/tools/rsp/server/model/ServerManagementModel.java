/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import org.jboss.tools.rsp.eclipse.jdt.launching.VMInstallRegistry;
import org.jboss.tools.rsp.server.discovery.DiscoveryPathModel;
import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanTypeManager;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathModel;
import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeManager;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.IServerModel;

public class ServerManagementModel implements IServerManagementModel {
	private static ServerManagementModel instance;
	public static ServerManagementModel getDefault() {
		return instance;
	}
	
	private DiscoveryPathModel rpm;
	private ServerBeanTypeManager serverBeanTypeManager;
	private ServerModel serverModel;
	private VMInstallRegistry vmModel;
	
	public ServerManagementModel() {
		rpm = new DiscoveryPathModel();
		serverBeanTypeManager = new ServerBeanTypeManager();
		serverModel = new ServerModel();
		vmModel = new VMInstallRegistry();
		vmModel.addActiveVM();
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
}
