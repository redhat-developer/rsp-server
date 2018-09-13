package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.launching.utils.NativeEnvironmentUtils;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class EnvironmentUtility {
	// Isolating duplicated code. Doesn't use IServer yet but will.
	private IServer server;
	public EnvironmentUtility(IServer server) {
		this.server = server;
	}
	
	public String[] getEnvironment() {
		return getEnvironment(true);
	}

	protected String[] getEnvironment(boolean appendNativeEnv) {
		Map<String, String> configEnv = getEnvironmentFromServer();
		return NativeEnvironmentUtils.getDefault().getEnvironment(configEnv, appendNativeEnv);
	}

	protected Map<String, String> getEnvironmentFromServer() {
		return new HashMap<String, String>();
	}
	
}
