/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.ssp.server.wildfly.servertype.IJBossStartLauncher;
import org.jboss.tools.ssp.server.wildfly.servertype.ILauncher;

public class LauncherDiscovery {
	private static LauncherDiscovery instance = new LauncherDiscovery();

	public static LauncherDiscovery getDefault() {
		return instance;
	}

	private LauncherDiscovery() {
		// Private constructor
	}

	public IJBossStartLauncher getStartupLauncher(IServer server) {
		String typeId = server.getServerType().getId();

		if (typeId.equals(IServerConstants.SERVER_AS_32)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_40)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_42)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_50)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_51)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_60)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_70)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_71)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_80)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_90)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_100)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_110)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_120)) {
			return new WildFlyStartLauncher(server.getDelegate());
		} else if (typeId.equals(IServerConstants.SERVER_EAP_43)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_50)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_60)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_61)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_70)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_71)) {
		}
		return null;
	}

	public ILauncher getShutdownLauncher(IServer server) {
		String typeId = server.getServerType().getId();

		if (typeId.equals(IServerConstants.SERVER_AS_32)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_40)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_42)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_50)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_51)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_60)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_70)) {
		} else if (typeId.equals(IServerConstants.SERVER_AS_71)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_80)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_90)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_100)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_110)) {
		} else if (typeId.equals(IServerConstants.SERVER_WILDFLY_120)) {
			return new WildFlyStopLauncher(server.getDelegate());
		} else if (typeId.equals(IServerConstants.SERVER_EAP_43)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_50)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_60)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_61)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_70)) {
		} else if (typeId.equals(IServerConstants.SERVER_EAP_71)) {
		}
		return null;
	}
}
