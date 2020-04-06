/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import java.util.Arrays;

import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.launching.java.ArgsUtil;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;

public class JBossASStartLauncher extends WildFlyStartLauncher {

	public JBossASStartLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}

	@Override
	protected String getMainTypeName() {
		return "org.jboss.Main";
	}


	@Override
	protected String getCalculatedProgramArgs() {
		IDefaultLaunchArguments largs = getLaunchArgs();
		String r1 = null;
		if( largs != null ) {
			String serverHome = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
			r1 = largs.getStartDefaultProgramArgs(new Path(serverHome));
			
			String host = getServer().getAttribute(
					IJBossServerAttributes.JBOSS_SERVER_HOST, (String)null);
			if( host != null ) {
				r1 = ArgsUtil.setArg(r1, "-b", null, host);
			}
		}
		return r1;
	}

	
	@Override
	protected String[] getClasspath() {
		String serverHome = getDelegate().getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
		String runJar = new Path(serverHome).append("bin").append("run.jar").toOSString();
		return addJreClasspathEntries(Arrays.asList(runJar));
	}
}
