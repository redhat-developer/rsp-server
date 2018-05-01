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
