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
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

/**
 * Used to launch arbitrary minishift commands, 
 * such as minishift openshift component add service-catalog
 */
public class MinishiftCommandLauncher extends AbstractLauncher {

	private String args;
	public MinishiftCommandLauncher(IServerDelegate jBossServerDelegate, String args) {
		super(jBossServerDelegate);
		this.args = args;
	}

	@Override
	public String getProgramArguments() {
		return args;
	}
	
	protected boolean supportsProfiles(IServer server) {
		return true;
	}
	
	protected String getCDKCredentialArguments() {
		return ""; // no credentials for setup-cdk
	}
}