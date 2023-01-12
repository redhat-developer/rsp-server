/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBossEAP72DefaultLaunchArguments;

public class JBossEAP80ExtendedProperties extends JBossAS710ExtendedProperties {

	public JBossEAP80ExtendedProperties(IServer obj) {
		super(obj);
	}

	@Override
	public String getRuntimeTypeVersionString() {
		return "8.0"; //$NON-NLS-1$
	}
	
	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		return new JBossEAP72DefaultLaunchArguments(server);
	}

	@Override
	public String getJMXUrl() {
			return getJMXUrl(getManagementPort(), "service:jmx:remote+http"); //$NON-NLS-1$
	}
	
	@Override
	public boolean requiresJDK() {
		return true;
	}
	
	@Override
	public boolean allowExplodedDeploymentsInWarLibs() {
		return true;
	}

	@Override
	public boolean allowExplodedDeploymentsInEars() {
		return true;
	}
//	@Override
//	public String getManagerServiceId() {
//		return IJBoss7ManagerService.WILDFLY_VERSION_110;
//	}
	@Override
	public String getMinimumJavaVersionString() {
		return "11.";
	}
	@Override
	public String getMaximumJavaVersionString() {
		return "21.";
	}

}
