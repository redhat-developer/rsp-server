package org.jboss.tools.rsp.server.minishift.servertype;

import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class MinishiftPropertyUtility {
	public static String getMinishiftCommand(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, (String) null);
	}

	public static String getMinishiftVMDriver(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_VM_DRIVER, (String) null);
	}

	public static String getMinishiftProfile(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_PROFILE,
				IMinishiftServerAttributes.MINISHIFT_PROFILE_DEFAULT);
	}

	public static String getMinishiftUsername(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, (String) null);
	}

	public static String getMinishiftPassword(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, (String) null);
	}

}
