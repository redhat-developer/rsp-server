/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.launching.utils.NativeEnvironmentUtils;
import org.jboss.tools.rsp.server.minishift.servertype.MinishiftPropertyUtility;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class EnvironmentUtility {
	private static final String KEY_USERNAME = "MINISHIFT_USERNAME";
	private static final String KEY_PASSWORD = "MINISHIFT_PASSWORD";
	private static final String MINISHIFT_HOME = "MINISHIFT_HOME";
	// Isolating duplicated code. Doesn't use IServer yet but will.
	private IServer server;
	public EnvironmentUtility(IServer server) {
		this.server = server;
	}
	
	public String[] getEnvironment() {
		return getEnvironment(true, true);
	}
	
	protected String[] getEnvironment(boolean appendNativeEnv, boolean appendCredentials) {
		Map<String, String> configEnv = null;
		if( appendCredentials ) 
			configEnv = getEnvironmentFromServer();
		else 
			configEnv = new HashMap<>();
		
		return NativeEnvironmentUtils.getDefault().getEnvironment(configEnv, appendNativeEnv);
	}

	protected Map<String, String> getEnvironmentFromServer() {
		HashMap<String,String> ret = new HashMap<>();
		String user = MinishiftPropertyUtility.getMinishiftUsername(server);
		String pass = MinishiftPropertyUtility.getMinishiftPassword(server);
		if( user != null && pass != null && !user.isEmpty() && !pass.isEmpty()) {
			ret.put(KEY_USERNAME, user);
			ret.put(KEY_PASSWORD, pass);
		}
		
		String msHome = MinishiftPropertyUtility.getMinishiftHome(server);
		if( msHome != null ) {
			ret.put(MINISHIFT_HOME, msHome);
		}
		return ret;
	}
}
