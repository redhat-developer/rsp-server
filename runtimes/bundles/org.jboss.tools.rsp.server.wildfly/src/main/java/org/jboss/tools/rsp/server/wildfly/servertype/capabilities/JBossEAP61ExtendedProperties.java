/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBoss72Eap61DefaultLaunchArguments;

public class JBossEAP61ExtendedProperties extends JBossAS710ExtendedProperties {
	public JBossEAP61ExtendedProperties(IServer obj) {
		super(obj);
	}
	
	public String getRuntimeTypeVersionString() {
		return "6.1+"; //$NON-NLS-1$
	}
	
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		return new JBoss72Eap61DefaultLaunchArguments(server);
	}
//
//	public String getManagerServiceId() {
//		return IJBoss7ManagerService.EAP_VERSION_61PLUS;
//	}

	@Override
	public boolean allowExplodedDeploymentsInWarLibs() {
		return true;
	}

	@Override
	public boolean requiresJDK() {
		return true;
	}
	
	@Override
	public boolean allowExplodedDeploymentsInEars() {
		return allowExplodedDeploymentsInWarLibs();
	}

}
