/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.server.spi.launchers.IShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;

public class JBossASServerDelegate extends AbstractJBossServerDelegate {
	public JBossASServerDelegate(IServer server) {
		super(server);
		setServerState(ServerManagementAPIConstants.STATE_STOPPED);
	}
	protected IStartLauncher getStartLauncher() {
		return LauncherDiscovery.getDefault().getStartupLauncher(getServer());
	}
	
	protected IShutdownLauncher getStopLauncher() {
		return LauncherDiscovery.getDefault().getShutdownLauncher(getServer());
	}
	@Override
	protected String getPollURL(IServer server) {
		// TODO?
		return "http://localhost:8080";
	}
}
