/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
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

public class SetupCDKLauncher extends AbstractLauncher {

	public SetupCDKLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}

	@Override
	public String getProgramArguments() {
		String args = "setup-cdk ";
		String msHome = MinishiftPropertyUtility.getMinishiftHome(getServer());
		if( msHome != null ) {
			args += "--minishift-home " + msHome + " ";
		}
		String vmDriver = MinishiftPropertyUtility.getMinishiftVMDriver(getServer());
		if( vmDriver != null ) {
			args += "--default-vm-driver " + vmDriver + " ";
		}
		args += "--force";
		return args;
	}
	
	protected boolean supportsProfiles(IServer server) {
		return true;
	}
	
	protected String getCDKCredentialArguments() {
		return ""; // no credentials for setup-cdk
	}
}