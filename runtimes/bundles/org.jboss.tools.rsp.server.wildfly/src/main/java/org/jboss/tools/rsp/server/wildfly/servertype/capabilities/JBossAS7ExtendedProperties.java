/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.impl.util.IJBossRuntimeResourceConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBoss70DefaultLaunchArguments;

public class JBossAS7ExtendedProperties extends JBossExtendedProperties { //implements IJBossManagerServiceProvider {

	public JBossAS7ExtendedProperties(IServer obj) {
		super(obj);
	}

	@Override
	public boolean runtimeSupportsExposingManagement() {
		return true;
	}
	
	@Override
	public int getJMXProviderType() {
		return JMX_DEFAULT_PROVIDER;
	}

	@Override
	public boolean runtimeSupportsBindingToAllInterfaces() {
		String version = getServerBeanLoader().getFullServerVersion();
		if( version == null )
			return true;
		if( version.startsWith("7.0.1") || version.startsWith("7.0.0"))  //$NON-NLS-1$//$NON-NLS-2$
			return false;
		return true;
	}

	@Override
	public int getMultipleDeployFolderSupport() {
		return DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT;
	}

	@Override
	public boolean canVerifyRemoteDeploymentState() {
		return true;
	}

	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		return new JBoss70DefaultLaunchArguments(server);
	}

	public String getJBossAdminScript() {
		return IJBossRuntimeResourceConstants.AS_70_MANAGEMENT_SCRIPT;
	}
	
	@Override
	public int getFileStructure() {
		return FILE_STRUCTURE_CONFIG_DEPLOYMENTS;
	}
	

	@Override
	public String getMinimumJavaVersionString() {
		return "1.6.";
	}
	@Override
	public String getMaximumJavaVersionString() {
		return "1.7.";
	}
	
	
	
//	public IServerModuleStateVerifier getModuleStateVerifier() {
//		try {
//			IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
//			return (IServerModuleStateVerifier)beh.getController(IControllableServerBehavior.SYSTEM_MODULES);
//		} catch(CoreException ce) {
//			JBossServerCorePlugin.log(ce);
//			return null;
//		}
//	}
//	public IDeploymentScannerModifier getDeploymentScannerModifier() {
//		return new LocalJBoss7DeploymentScannerAdditions();
//	}
//
//	/**
//	 * @since 3.0
//	 */
//	public IJBoss7ManagerService getManagerService() {
//		return JBoss7ManagerUtil.getManagerService(getManagerServiceId());
//	}
//	
//	/**
//	 * @since 3.0
//	 */
//	public String getManagerServiceId() {
//		return IJBoss7ManagerService.AS_VERSION_700;
//	}
//	
//	/**
//	 * Returns the full path of a local server's server/{config}/deploy folder
//	 */
//	@Override
//	public String getServerDeployLocation() {
//		if( runtime == null )
//			return null;
//		LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)runtime.loadAdapter(LocalJBoss7ServerRuntime.class, null);
//		IPath p = new Path(jb7rt.getBaseDirectory()).append(AS7_DEPLOYMENTS);
//		return ServerUtil.makeGlobal(runtime, p).toString();
//	}
//	public String getNewXPathDefaultRootFolder() {
//		return ""; //$NON-NLS-1$
//	}
//
//	public String getNewFilesetDefaultRootFolder() {
//		if( runtime == null )
//			return IJBossRuntimeResourceConstants.AS7_STANDALONE + "/" + IJBossRuntimeResourceConstants.CONFIGURATION; //$NON-NLS-1$
//		LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)runtime.loadAdapter(LocalJBoss7ServerRuntime.class, null);
//		return jb7rt.getConfigLocation();
//	}
//	
//	public String getNewClasspathFilesetDefaultRootFolder() {
//		return IJBossRuntimeResourceConstants.AS7_MODULES + "/org"; //$NON-NLS-1$
//	}
}
