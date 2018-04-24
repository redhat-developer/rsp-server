package org.jboss.tools.ssp.server.model;

import org.jboss.tools.ssp.server.discovery.RuntimePathModel;
import org.jboss.tools.ssp.server.discovery.serverbeans.ServerBeanTypeManager;

public class ServerManagementModel {
	private static ServerManagementModel instance;
	public static ServerManagementModel getDefault() {
		return instance;
	}
	
	private RuntimePathModel rpm;
	private ServerBeanTypeManager serverBeanTypeManager;
	private ServerModel serverModel;
	
	public ServerManagementModel() {
		rpm = new RuntimePathModel();
		serverBeanTypeManager = new ServerBeanTypeManager();
		serverModel = new ServerModel();
		instance = this;
	}
	
	public RuntimePathModel getRuntimePathModel() {
		return rpm;
	}
	
	public ServerBeanTypeManager getServerBeanTypeManager() {
		return serverBeanTypeManager;
	}
	
	public ServerModel getServerModel() {
		return serverModel;
	}
}
