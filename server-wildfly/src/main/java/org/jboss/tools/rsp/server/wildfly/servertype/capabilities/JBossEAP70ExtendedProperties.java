/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
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
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBossEAP70DefaultLaunchArguments;

public class JBossEAP70ExtendedProperties extends JBossAS710ExtendedProperties {
	public JBossEAP70ExtendedProperties(IServer obj) {
		super(obj);
	}
	@Override
	public String getRuntimeTypeVersionString() {
		return "7.0"; //$NON-NLS-1$
	}
//	
//	@Override
//	public IExecutionEnvironment getDefaultExecutionEnvironment() {
//		return JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("JavaSE-1.8"); //$NON-NLS-1$
//	}
//	@Override
//	public String getManagerServiceId() {
//		return IJBoss7ManagerService.WILDFLY_VERSION_900;
//	}
//	

	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		return new JBossEAP70DefaultLaunchArguments(server);
	}

	@Override
	public String getJMXUrl() {
			return getJMXUrl(9990, "service:jmx:remote+http"); //$NON-NLS-1$
	}
	
	@Override
	public boolean requiresJDK() {
		return true;
	}

	@Override
	public boolean allowExplodedModulesInWarLibs() {
		return true;
	}
	
	@Override
	public boolean allowExplodedModulesInEars() {
		return true;
	}
}
