package org.jboss.tools.ssp.server.model;

import java.io.File;

import org.jboss.tools.ssp.server.discovery.RuntimePathModel;
import org.jboss.tools.ssp.server.discovery.serverbeans.ServerBeanTypeManager;

public class ServerManagementModel {
	private static ServerManagementModel instance;
	public static ServerManagementModel getDefault() {
		return instance;
	}
	
	private RuntimePathModel rpm;
	private ServerBeanTypeManager serverBeanTypeManager;
	public ServerManagementModel() {
		rpm = new RuntimePathModel();
		serverBeanTypeManager = new ServerBeanTypeManager();
		instance = this;
	}
	
	public RuntimePathModel getRuntimePathModel() {
		return rpm;
	}
	
	public ServerBeanTypeManager getServerBeanTypeManager() {
		return serverBeanTypeManager;
	}
}
