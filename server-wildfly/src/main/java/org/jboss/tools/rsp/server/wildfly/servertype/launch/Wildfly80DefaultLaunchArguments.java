/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.launch;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class Wildfly80DefaultLaunchArguments extends
		JBoss71DefaultLaunchArguments {
	public Wildfly80DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public String getStartDefaultVMArgs() {
		return super.getStartDefaultVMArgs() 
				+ "-Dorg.jboss.logmanager.nocolor=true "; //$NON-NLS-1$
	}
	protected String getMemoryArgs() {
		return "-Xms64m -Xmx512m -XX:MaxPermSize=256m "; //$NON-NLS-1$
	}
	
	public String getDefaultStopVMArgs() {
		IPath home = getServerHome();
		IPath loggingProp = home.append("bin").append("jboss-cli-logging.properties");
		return "-Djboss.modules.system.pkgs=com.sun.java.swing \"-Dlogging.configuration=file:"
				+ loggingProp.toOSString() + "\"";
	}
}
