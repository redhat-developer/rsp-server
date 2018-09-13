/******************************************************************************* 
 * Copyright (c) 2012-2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities;

import java.io.File;

import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util.URLUtil;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBoss5xDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBossDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBossSoa5xDefaultLaunchArguments;

/**
 * The superclass containing most functionality, to be overridden as necessary.
 * The contents of this are all sorts of errata that do not really fit anywhere
 * else, but need to be customized on a per-server or per-server-type basis
 */
public class JBossExtendedProperties extends ServerExtendedProperties {

	private static final String[] AS5_TYPES = new String[]{
			IServerConstants.SERVER_AS_50,
			IServerConstants.SERVER_AS_51,
			IServerConstants.SERVER_EAP_50,
	};
	
	public JBossExtendedProperties(IServer adaptable) {
		super(adaptable);
	}

	public boolean runtimeSupportsBindingToAllInterfaces() {
		return true;
	}

	public boolean runtimeSupportsExposingManagement() {
		return false;
	}

	protected ServerBeanLoader getServerBeanLoader() {
		return new ServerBeanLoader(getServerHomeFile());
	}
	
	protected String getServerHome() {
		return server.getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
	}
	
	protected File getServerHomeFile() {
		return new File(getServerHome());
	}

	@Override
	public int getJMXProviderType() {
		return JMX_OVER_JNDI_PROVIDER;
	}
	
	@Override
	public boolean hasWelcomePage() {
		return true;
	}
	
	@Override
	public String getWelcomePageUrl() {
		int webPort = getWebPort();
		return URLUtil.createSafeURLString("http", getHost(), webPort, null); //$NON-NLS-1$
	}
	
	protected String getHost() {
		return "localhost";
	}
	
	protected int getWebPort() {
		return 8080;
	}

	@Override
	public int getMultipleDeployFolderSupport() {
		return DEPLOYMENT_SCANNER_JMX_SUPPORT;
	}

	@Override
	public boolean canVerifyRemoteModuleState() {
		return true;
	}
	
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		// to avoid making too many classes with one method, 
		// we'll handle below 6 here. Otherwise we need another 
		// almost-empty extended properties class
		// If we're AS 5, return the 5x args
		if (isAS5(server)) {
			if (isSoa5x()) {
				return new JBossSoa5xDefaultLaunchArguments(server);
			} else {
				return new JBoss5xDefaultLaunchArguments(server);
			}
		} else {
			// else return the < 5 launch args
			return new JBossDefaultLaunchArguments(server);
		}
	}

	private boolean isAS5(IServer server) {
		boolean isAS5 = false;
		if (server == null
				|| server.getServerType() == null) {
			return false;
		}
		String id = server.getServerType().getId();
		for (int i = 0; i < AS5_TYPES.length; i++) {
			if (AS5_TYPES[i].equals(id)) {
				isAS5 = true;
				break;
			}
		}
		return isAS5;
	}

	private boolean isSoa5x() {
		return false;
// TODO: re-enable
//		// Special case workaround for soa-p 5.3.1
//		ServerBean sb = new ServerBeanLoader(getServerHomeFile()).getServerBean();
//		if( sb.getTypeCategory().equals(JBossServerBeanTypeProvider.EAP_STD.getId())) {
//			// load from the parent folder
//			sb = new ServerBeanLoader(getServerHomeFile().getParentFile()).getServerBean();
//			if( sb != null && "SOA-P".equals(
//				sb.getTypeCategory()) && sb.getVersion().startsWith("5.")) {  //$NON-NLS-1$
//				return true;
//			}
//		}
//		return false;
	}

	/*
	 * support for execution environment was removed:
	 * IExecutionEnvironment getDefaultExecutionEnvironment()
	 * IExecutionEnvironment getMinimumExecutionEnvironment()
	 * IExecutionEnvironment getMaximumExecutionEnvironment()
	 */

	public boolean requiresJDK() {
		return false;
	}

	@Override
	public int getFileStructure() {
		return FILE_STRUCTURE_SERVER_CONFIG_DEPLOY;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * For JBoss servers allow only for servers which are know to support this according to AS7-4704.
	 */
	@Override
	public boolean allowExplodedModulesInWarLibs() {
		return false;
	}

	@Override
	public boolean allowExplodedModulesInEars() {
		return false;
	}

}
