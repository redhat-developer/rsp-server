/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype;

import java.util.Map;

import org.jboss.tools.rsp.server.minishift.servertype.impl.MinishiftServerDelegate;
import org.jboss.tools.rsp.server.redhat.credentials.RedHatAccessCredentials;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class MinishiftPropertyUtility {

	private MinishiftPropertyUtility() {
		// inhibit instantiation
	}
	
	public static String getMinishiftAppendedProgArgs(IServer server) {
		return server.getAttribute(MinishiftServerDelegate.STARTUP_PROGRAM_ARGS_STRING, (String)null);
	}

	public static Map<String,String> getMinishiftStartupEnvironment(IServer server) {
		return (Map<String,String>)server.getAttribute(MinishiftServerDelegate.STARTUP_ENV_VARS_MAP, (Map<String,String>)null);
	}

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

	public static String getMinishiftHome(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_HOME, (String)null);
	}

	/**
	 * Get either the hard-coded username for use on this server, or, 
	 * if that is not set, get the username stored in the global settings
	 * for redhat access credentials 
	 * @param server
	 * @return
	 */
	public static String getMinishiftUsername(IServer server) {
		String name = server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, (String) null);
		if( name == null ) {
			return RedHatAccessCredentials.getGlobalRedhatUser(server.getServerManagementModel().getSecureStorageProvider());
		}
		return name;
	}

	/**
	 * Get either the hard-coded password for use on this server, or, 
	 * if that is not set, get the password stored in the global settings
	 * for redhat access credentials 
	 * @param server
	 * @return
	 */
	public static String getMinishiftPassword(IServer server) {
		String pass = server.getAttribute(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, (String) null);
		if( pass == null ) {
			return RedHatAccessCredentials.getGlobalRedhatPassword(server.getServerManagementModel().getSecureStorageProvider());
		}
		return pass;
	}

}
