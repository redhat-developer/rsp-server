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

public class JBoss71DefaultLaunchArguments extends JBoss70DefaultLaunchArguments {
	public JBoss71DefaultLaunchArguments(IServer s) {
		super(s);
	}
	@Override
	protected String getLoggingProgramArg() {
		// logging params removed
		return new String();
	}
	
	protected String getJBossJavaFlags() {
		return "-Djboss.modules.system.pkgs=org.jboss.byteman " + //$NON-NLS-1$
				super.getJBossJavaFlags();
	}
	
	@Override
	public String getDefaultStopArgs() {
		IPath modules = getServerHome().append("modules");
		return "-mp \"" + modules.toOSString() + "\" org.jboss.as.cli --connect command=:shutdown"; 
	}
}
