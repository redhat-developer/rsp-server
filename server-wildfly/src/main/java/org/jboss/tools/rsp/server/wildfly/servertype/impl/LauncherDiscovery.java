/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import org.jboss.tools.rsp.server.spi.launchers.IShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;

public class LauncherDiscovery {
	private static LauncherDiscovery instance = new LauncherDiscovery();

	public static LauncherDiscovery getDefault() {
		return instance;
	}

	private LauncherDiscovery() {
		// Private constructor
	}

	public IStartLauncher getStartupLauncher(IServer server) {
		String typeId = server.getServerType().getId();
		if( isJBossModules(typeId)) {
			return new WildFlyStartLauncher(server.getDelegate());
		}
		return new JBossASStartLauncher(server.getDelegate());
	}

	private boolean isJBossModules(String typeId) {
		if (typeId.equals(IServerConstants.SERVER_AS_32) || typeId.equals(IServerConstants.SERVER_AS_40)
				|| typeId.equals(IServerConstants.SERVER_AS_42) || typeId.equals(IServerConstants.SERVER_AS_50)
				|| typeId.equals(IServerConstants.SERVER_AS_51) || typeId.equals(IServerConstants.SERVER_AS_60)
				|| typeId.equals(IServerConstants.SERVER_EAP_43)
				|| typeId.equals(IServerConstants.SERVER_EAP_50)) {
			return false;
		}
		return true;
	}
	

	public IShutdownLauncher getShutdownLauncher(IServer server) {
		String typeId = server.getServerType().getId();
		if( isJBossModules(typeId)) {
			return new WildFlyStopLauncher(server.getDelegate());
		}
		return new JBossASStopLauncher(server.getDelegate());
	}
}
