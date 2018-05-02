/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.model;

import org.jboss.tools.ssp.launching.VMInstallModel;
import org.jboss.tools.ssp.server.discovery.DiscoveryPathModel;
import org.jboss.tools.ssp.server.discovery.serverbeans.ServerBeanTypeManager;

public class ServerManagementModel {
	private static ServerManagementModel instance;
	public static ServerManagementModel getDefault() {
		return instance;
	}
	
	private DiscoveryPathModel rpm;
	private ServerBeanTypeManager serverBeanTypeManager;
	private ServerModel serverModel;
	
	public ServerManagementModel() {
		rpm = new DiscoveryPathModel();
		serverBeanTypeManager = new ServerBeanTypeManager();
		serverModel = new ServerModel();
		instance = this;
	}
	
	public DiscoveryPathModel getDiscoveryPathModel() {
		return rpm;
	}
	
	public ServerBeanTypeManager getServerBeanTypeManager() {
		return serverBeanTypeManager;
	}
	
	public ServerModel getServerModel() {
		return serverModel;
	}

	public VMInstallModel getVMInstallModel() {
		return VMInstallModel.getDefault();
	}
}
