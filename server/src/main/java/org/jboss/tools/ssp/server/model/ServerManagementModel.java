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
	

	public static void log(Throwable t) {
		t.printStackTrace();
	}
	public static void log(String bind) {
		System.out.println(bind);
	}
	
	public static final String SYSPROP_DATA_LOCATION = "org.jboss.tools.ssp.data";
	public static final String SYSPROP_DATA_DEFAULT_LOCATION = ".org.jboss.tools.ssp.data";
	public static File getDataLocation() {
		String prop = System.getProperty(SYSPROP_DATA_LOCATION);
		File ret = null;
		if( prop != null ) {
			ret = new File(prop);
		}
		if( prop == null ) {
			String home = System.getProperty("user.home");
			File home2 = new File(home);
			ret = new File(home2, SYSPROP_DATA_DEFAULT_LOCATION);
		}
		if( ret != null && !ret.exists()) {
			ret.mkdirs();
		}
		return ret;
	}
}
