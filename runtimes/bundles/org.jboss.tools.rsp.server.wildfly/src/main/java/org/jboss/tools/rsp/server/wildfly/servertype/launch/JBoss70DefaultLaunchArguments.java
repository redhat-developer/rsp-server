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
package org.jboss.tools.rsp.server.wildfly.servertype.launch;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.impl.util.IJBossRuntimeResourceConstants;

public class JBoss70DefaultLaunchArguments extends JBossDefaultLaunchArguments {
	public JBoss70DefaultLaunchArguments(IServer s) {
		super(s);
	}
	@Override
	public String getStartDefaultProgramArgs() {
		String ret = DASH + JB7_MP_ARG + SPACE + QUOTE 
				+ getServerHome().append(MODULES).toOSString() + QUOTE 
				+ getLoggingProgramArg() + SPACE
				+ getJaxpProvider() + SPACE
				+ getJaxpProvider()
				+ SPACE + JB7_STANDALONE_ARG;
		return ret;
	}
	
	protected String getJaxpProvider() {
		return DASH + JB7_JAXPMODULE + SPACE + JB7_JAXP_PROVIDER;
	}
	
	protected String getLoggingProgramArg() {
		return SPACE + DASH + JB7_LOGMODULE_ARG + SPACE + JB7_LOGMODULE_DEFAULT;
	}
	
	@Override
	public String getStartDefaultVMArgs() {
		return getProgramNameArgs() + getServerFlagArgs() +
				getMemoryArgs() + getResolverWarning() +
				getJavaFlags() + getJBossJavaFlags();
	}
	@Override
	protected String getMemoryArgs() {
		return "-Xms64m -Xmx512m -XX:MaxPermSize=256m "; //$NON-NLS-1$
	}
	protected String getResolverWarning() {
		return "-Dorg.jboss.resolver.warning=true ";  //$NON-NLS-1$
	}
	
	@Override
	protected String getJavaFlags() {
		return getJavaFlags(true);
	}

	protected IPath getBaseDirectory() {
		IPath serverHome = getServerHome();
		IPath base = serverHome.append(IJBossRuntimeResourceConstants.AS7_STANDALONE);
		return base;		
	}
	
	@Override
	protected String getJBossJavaFlags() {
		IPath serverHome = getServerHome();
		IPath base = getBaseDirectory();
		// TODO this can be changed to the config folder, if such a feature is added
		IPath bootLog = base.append(IJBossRuntimeResourceConstants.FOLDER_LOG).append(IJBossRuntimeResourceConstants.AS7_BOOT_LOG);
		IPath logConfig = base.append(IJBossRuntimeResourceConstants.CONFIGURATION).append(IJBossRuntimeResourceConstants.LOGGING_PROPERTIES);

		String ret = 
			"-Djava.awt.headless=true" + //$NON-NLS-1$
			SPACE + QUOTE + SYSPROP + JB7_BOOT_LOG_ARG + EQ + bootLog.toOSString() + QUOTE + 
			SPACE + QUOTE + SYSPROP + JB7_LOGGING_CONFIG_FILE + EQ + 
			"file:" + logConfig.toOSString() + QUOTE + //$NON-NLS-1$  
			SPACE + QUOTE + SYSPROP + JBOSS_HOME_DIR + EQ + serverHome.toOSString() + QUOTE + SPACE;
		return ret;
	}
	
	@Override
	public String getDefaultStopArgs() {
		IPath modules = getServerHome().append("modules");
		return "-logmodule org.jboss.logmanager -mp \"" + modules.toOSString() + "\" org.jboss.as.cli --connect command=:shutdown"; 
	}
}
