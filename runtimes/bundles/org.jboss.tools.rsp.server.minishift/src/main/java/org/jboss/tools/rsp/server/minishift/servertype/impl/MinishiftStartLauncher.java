/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.server.minishift.servertype.AbstractLauncher;
import org.jboss.tools.rsp.server.minishift.servertype.MinishiftPropertyUtility;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class MinishiftStartLauncher extends AbstractLauncher {

	public MinishiftStartLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}

	@Override
	public String getProgramArguments() {
		String vmDriver = MinishiftPropertyUtility.getMinishiftVMDriver(getServer());
		String vmd = isEmpty(vmDriver) ? "" : "--vm-driver=" + vmDriver;
		String profileFlags = "";
		if( supportsProfiles(getServer())) {
			String profile = MinishiftPropertyUtility.getMinishiftProfile(getServer());
			profileFlags = " --profile " + profile;
		}

		String skipReg = getCDKCredentialArguments();
		skipReg = isEmpty(skipReg) ? "" : " " + skipReg;
		
		String append = getAppendedArguments();
		append = isEmpty(append) ? "" : " " + append;
		
		return "start " + vmd + profileFlags + skipReg + append;
	}
	protected String getAppendedArguments() {
		String append = getServer().getAttribute(
				MinishiftServerDelegate.STARTUP_PROGRAM_ARGS_STRING, (String)null);
		return append == null ? "" : append;
	}
	
	protected boolean supportsProfiles(IServer server) {
		// TODO some earlier versions don't support profiles
		return true; 
	}
	
	protected String getCDKCredentialArguments() {
		String user = MinishiftPropertyUtility.getMinishiftUsername(getServer());
		String pass = MinishiftPropertyUtility.getMinishiftPassword(getServer());
		String credentials = "";
		if( isEmpty(user) || isEmpty(pass)) {
			credentials = " --skip-registration";
		}
		return credentials;
	}
	
	private boolean isEmpty(String s) {
		return s == null ? true : s.isEmpty();
	}
}
