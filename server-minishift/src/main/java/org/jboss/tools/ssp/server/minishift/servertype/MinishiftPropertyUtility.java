package org.jboss.tools.ssp.server.minishift.servertype;

import org.jboss.tools.ssp.server.spi.servertype.IServer;

public class MinishiftPropertyUtility {
	public static String getMinishiftCommand(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, (String) null);
	}

	public static String getMinishiftVMDriver(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_VM_DRIVER, (String) null);
	}

	public static String getMinishiftUsername(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, (String) null);
	}

	public static String getMinishiftPassword(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, (String) null);
	}

}
