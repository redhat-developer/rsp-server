/*******************************************************************************
 * Copyright (c) 2026 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.launch;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;

public abstract class AbstractWildFlyDefaultLaunchArguments extends JBoss71DefaultLaunchArguments {
	public AbstractWildFlyDefaultLaunchArguments(IServer s) {
		super(s);
	}

	@Override
	public String getDefaultStopArgs() {
		IPath modules = getServerHome().append("modules");
		String controllerArg = getControllerArg();
		return "-mp \"" + modules.toOSString() + "\" org.jboss.as.cli --connect"
				+ controllerArg + " command=:shutdown";
	}

	private String getControllerArg() {
		if (server == null) {
			return "";
		}
		int port = server.getAttribute(IJBossServerAttributes.WILDFLY_MANAGEMENT_PORT,
				IJBossServerAttributes.WILDFLY_MANAGEMENT_PORT_DEFAULT);
		if (port == IJBossServerAttributes.WILDFLY_MANAGEMENT_PORT_DEFAULT) {
			return "";
		}
		String host = server.getAttribute(IJBossServerAttributes.JBOSS_SERVER_HOST,
				IJBossServerAttributes.JBOSS_SERVER_HOST_DEFAULT);
		if (host == null || host.isEmpty()) {
			host = IJBossServerAttributes.JBOSS_SERVER_HOST_DEFAULT;
		}
		return " --controller=" + host + ":" + port;
	}
}
