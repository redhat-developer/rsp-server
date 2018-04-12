package org.jboss.tools.ssp.server.model;

import org.jboss.tools.ssp.server.discovery.RuntimePathModel;

public class ServerManagementModel {
	private RuntimePathModel rpm;
	public ServerManagementModel() {
		rpm = new RuntimePathModel();
	}
	
	public RuntimePathModel getRuntimePathModel() {
		return rpm;
	}
}
