/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities;

import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeAS7GateIn;
import org.jboss.tools.rsp.server.wildfly.impl.util.IJBossRuntimeResourceConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util.URLUtil;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.GateIn33AS71DefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBoss71DefaultLaunchArguments;

public class JBossAS710ExtendedProperties extends JBossAS7ExtendedProperties { //implements IJMXURLProvider {

	public JBossAS710ExtendedProperties(IServer obj) {
		super(obj);
	}
	
	public String getRuntimeTypeVersionString() {
		return "7.1"; //$NON-NLS-1$
	}

	public int getJMXProviderType() {
		return JMX_OVER_AS_MANAGEMENT_PORT_PROVIDER;
	}
	public boolean runtimeSupportsBindingToAllInterfaces() {
		return true;
	}
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null) {
			ServerBeanLoader l = new ServerBeanLoader(getServerHomeFile());
			if( l.getServerBeanType().getName().equals(ServerBeanTypeAS7GateIn.NAME_GATEIN)) {
				String version = l.getServerBean().getVersion();
				if( "3.3".equals(version) 
						|| "3.4".equals(version) ) {
					return new GateIn33AS71DefaultLaunchArguments(server);
				}
			}
			return new JBoss71DefaultLaunchArguments(server);
		}
		return null;
	}
	
	@Override
	public String getJBossAdminScript() {
		return IJBossRuntimeResourceConstants.AS_71_MANAGEMENT_SCRIPT;
	}
	
	/**
	 * @since 3.0
	 */
	public String getJMXUrl() {
		return getJMXUrl(getManagementPort(), "service:jmx:remoting-jmx"); //$NON-NLS-1$
	}
	
	/**
	 * @since 3.0
	 */
	protected String getJMXUrl(int defaultPort, String jmxScheme) {
		String ret = URLUtil.createSafeURLString(jmxScheme, getHost(), getManagementPort(), null);
		return ret;
	}
	
	protected int getManagementPort() {
		return 9999;
	}

//
//	public String getManagerServiceId() {
//		return IJBoss7ManagerService.AS_VERSION_71x;
//	}
	
}
