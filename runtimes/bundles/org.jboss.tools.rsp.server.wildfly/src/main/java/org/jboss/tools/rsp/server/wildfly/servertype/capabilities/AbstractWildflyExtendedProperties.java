/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
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
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBossDefaultLaunchArguments;

public class AbstractWildflyExtendedProperties extends JBossAS710ExtendedProperties {

	private final String runtimeVersion;
	private final String minJavaVersion;
	private final String maxJavaVersion;
	private final String jxmScheme;
	private final JBossDefaultLaunchArguments launchArguments;

	public AbstractWildflyExtendedProperties(String runtimeVersion, String minJavaVersion, String maxJavaVersion, 
			String jmxScheme, JBossDefaultLaunchArguments launchArguments, IServer obj) {
		super(obj);
		this.runtimeVersion = runtimeVersion;
		this.minJavaVersion = minJavaVersion;
		this.maxJavaVersion = maxJavaVersion;
		this.jxmScheme = jmxScheme;
		this.launchArguments = launchArguments;
	}

	@Override
	public String getRuntimeTypeVersionString() {
		return runtimeVersion;
	}
	
	@Override
	public boolean requiresJDK() {
		return true;
	}

	@Override
	public String getJMXUrl() {
			return getJMXUrl(getManagementPort(), this.jxmScheme);
	}
	
	@Override
	public int getManagementPort() {
		return 9990;
	}
	
	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		return launchArguments;
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
//		return IJBoss7ManagerService.WILDFLY_VERSION_900;
//	}
//
	@Override
	public String getMinimumJavaVersionString() {
		return minJavaVersion;
	}

	@Override
	public String getMaximumJavaVersionString() {
		return maxJavaVersion;
	}

}
