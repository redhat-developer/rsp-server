/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.minishift.servertype.impl;

import org.jboss.tools.ssp.server.minishift.servertype.AbstractLauncher;
import org.jboss.tools.ssp.server.minishift.servertype.MinishiftPropertyUtility;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;

public class MinishiftStartLauncher extends AbstractLauncher {

	public MinishiftStartLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}

	@Override
	public String getProgramArguments() {
		String vmDriver = MinishiftPropertyUtility.getMinishiftVMDriver(getServer());
		if( vmDriver == null || vmDriver.isEmpty()) {
			return "start " + getCredentialsArguments();
		}
		return "start --vm-driver=" + vmDriver  + getCredentialsArguments();
	}
	
	protected String getCredentialsArguments() {
		return "";
	}
	
	protected String getCDKCredentialArguments() {
		String user = MinishiftPropertyUtility.getMinishiftUsername(getServer());
		String pass = MinishiftPropertyUtility.getMinishiftPassword(getServer());
		String credentials = "";
		if( isEmpty(user) || isEmpty(pass)) {
			credentials = " --skip-registration";
		} else {
			credentials = " --username " + user + " --password " + pass;
		}
		return credentials;
	}
	
	private boolean isEmpty(String s) {
		return s == null ? true : s.length() == 0;
	}
}
