/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import java.util.Arrays;

import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractLauncher;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;

public class WildFlyStartLauncher extends AbstractLauncher {

	public WildFlyStartLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}

	protected String getWorkingDirectory() {
		String serverHome = getDelegate().getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
		return serverHome + "/bin";
	}

	protected String getMainTypeName() {
		return "org.jboss.modules.Main";
	}

	protected String[] getClasspath() {
		String serverHome = getDelegate().getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
		String jbModules = serverHome + "/jboss-modules.jar";
		return addJreClasspathEntries(Arrays.asList(new String[] {jbModules}));
	}
	protected String getVMArguments() {
		IDefaultLaunchArguments largs = getLaunchArgs();
		if( largs != null ) {
			String serverHome = getDelegate().getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
			return largs.getStartDefaultVMArgs(new Path(serverHome));
		}
		return null;
	}

	protected String getProgramArguments() {
		IDefaultLaunchArguments largs = getLaunchArgs();
		String r1 = null;
		if( largs != null ) {
			String serverHome = getDelegate().getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
			r1 = largs.getStartDefaultProgramArgs(new Path(serverHome));
		}
		return r1;
	}

	@Override
	public IServer getServer() {
		return getDelegate().getServer();
	}
}
