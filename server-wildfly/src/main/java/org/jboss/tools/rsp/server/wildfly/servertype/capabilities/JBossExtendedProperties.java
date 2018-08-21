/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
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

import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.impl.JBossServerBeanTypeProvider;
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
 *
 */
public class JBossExtendedProperties extends ServerExtendedProperties {
	public JBossExtendedProperties(IServer adaptable) {
		super(adaptable);
	}
	
	// TODO most subclasses have this, so we might need to impl?
	/* 
	 * Get the version string for this runtime type. 
	 * Some subclasses may choose to respond with a .x suffix. 
	 */
//	public String getRuntimeTypeVersionString() {
//		return runtime.getRuntimeType().getVersion();
//	}
//	
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
	
	public int getJMXProviderType() {
		return JMX_OVER_JNDI_PROVIDER;
	}
	
	public boolean hasWelcomePage() {
		return true;
	}
	
	@Deprecated
	protected static final String WELCOME_PAGE_URL_PATTERN = "http://{0}:{1}/"; //$NON-NLS-1$
	public String getWelcomePageUrl() {
		int webPort = getWebPort();
		String consoleUrl = URLUtil.createSafeURLString("http", getHost(), webPort, null); //$NON-NLS-1$
		return consoleUrl;
	}
	
	protected String getHost() {
		return "localhost";
	}
	
	protected int getWebPort() {
		return 8080;
	}

	public int getMultipleDeployFolderSupport() {
		return DEPLOYMENT_SCANNER_JMX_SUPPORT;
	}

	public boolean canVerifyRemoteModuleState() {
		return true;
	}
	
//	public IServerModuleStateVerifier getModuleStateVerifier() {
//		return new JBossLT6ModuleStateVerifier();
//	}
//	
//	public IDeploymentScannerModifier getDeploymentScannerModifier() {
//		return new JMXServerDeploymentScannerAdditions();
//	}
	
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		// to avoid making too many classes with one method, 
		// we'll handle below 6 here. Otherwise we need another 
		// almost-empty extended properties class
		String[] as5Types = new String[]{
				IServerConstants.SERVER_AS_50,
				IServerConstants.SERVER_AS_51,
				IServerConstants.SERVER_EAP_50,
		};
		boolean isAS5 = false;
		String serverType = (server == null ? null : server.getServerType() == null ? null : server.getServerType().getId());
		for( int i = 0; i < as5Types.length; i++ ) {
			if( as5Types[i].equals(serverType) ) {
				isAS5 = true;
				break;
			}
		}
		
		// IF we're AS 5, return the 5x args
		if( isAS5 ) {
			if( isSoa5x()) {
				return new JBossSoa5xDefaultLaunchArguments(server);
			}
			return new JBoss5xDefaultLaunchArguments(server);
		}
		
		// else return the < 5 launch args
		return new JBossDefaultLaunchArguments(server);
	}
	
	private boolean isSoa5x() {
		// Special case workaround for soa-p 5.3.1
		return false;
//		ServerBean sb = new ServerBeanLoader(getServerHomeFile()).getServerBean();
//		if( sb.getTypeCategory().equals(JBossServerBeanTypeProvider.EAP_STD.getId())) {
//			// load from the parent folder
//			sb = new ServerBeanLoader(getServerHomeFile().getParentFile()).getServerBean();
//			if( sb != null && "SOA-P".equals(
//					sb.getTypeCategory()) && sb.getVersion().startsWith("5.")) {  //$NON-NLS-1$
//				return true;
//			}
//		}
//		return false;
	}

	public boolean requiresJDK() {
		return false;
	}
	
	public int getFileStructure() {
		return FILE_STRUCTURE_SERVER_CONFIG_DEPLOY;
	}
//	
//	/**
//	 * This is being used to indicate the MINIMUM execution environment, 
//	 * not just the default!
//	 * 
//	 * @param rtType
//	 * @return
//	 */
//	public IExecutionEnvironment getDefaultExecutionEnvironment() {
//		// NEW_SERVER_ADAPTER  Subclasses override this
//		return JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("J2SE-1.4"); //$NON-NLS-1$
//	}
//	
//	public IExecutionEnvironment getMinimumExecutionEnvironment() {
//		// NEW_SERVER_ADAPTER  Subclasses override this
//		return getDefaultExecutionEnvironment();
//	}
//
//	// Return an exec-env or null if it can run on any higher exec-env. 
//	public IExecutionEnvironment getMaximumExecutionEnvironment() {
//		// NEW_SERVER_ADAPTER  Subclasses override this
//		return JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("JavaSE-1.8"); //$NON-NLS-1$
//	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * For JBoss servers allow only for servers which are know to support this according to AS7-4704.
	 */
	@Override
	public boolean allowExplodedModulesInWarLibs() {
		return false;
	}
	
	public boolean allowExplodedModulesInEars() {
		return false;
	}

}
