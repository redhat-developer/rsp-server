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

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBoss6xDefaultLaunchArguments;

public class JBossAS6ExtendedProperties extends JBossExtendedProperties {

	public JBossAS6ExtendedProperties(IServer adaptable) {
		super(adaptable);
	}
	
	public String getRuntimeTypeVersionString() {
		return "6.x"; //$NON-NLS-1$
	}

	
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		return new JBoss6xDefaultLaunchArguments(server);
	}
}
