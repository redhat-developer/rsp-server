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
import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class JBoss6xDefaultLaunchArguments extends JBoss5xDefaultLaunchArguments {
	public JBoss6xDefaultLaunchArguments(IServer server) {
		super(server);
	}

	protected String getShutdownServerUrl() {
		return "service:jmx:rmi:///jndi/rmi://" + getHost() + ":" + getJMXRMIPort() + "/jmxrmi"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private int getJMXRMIPort() {
		return 1090;
	}

	protected String getJBossJavaFlags() {
		String ret = super.getJBossJavaFlags();
		
		// It's possible the remote server is of a different version, but we 
		// EXPECT the local dev copy is the same distribution. 
		IPath home = getServerHome();
		/// use the local version to know what version, since we can't actually look at the remote
		String version = new ServerBeanLoader(getServerHome().toFile()).getFullServerVersion();
		if( version.startsWith("6.1")) {
			// Only relevent for as6.1
			ret += SYSPROP + LOGGING_CONFIG_PROP + EQ + QUOTE + FILE_COLON + 
					home.append(BIN).append(LOGGING_PROPERTIES).toOSString() + QUOTE + SPACE;
		}
		
		return ret;
	}
}
