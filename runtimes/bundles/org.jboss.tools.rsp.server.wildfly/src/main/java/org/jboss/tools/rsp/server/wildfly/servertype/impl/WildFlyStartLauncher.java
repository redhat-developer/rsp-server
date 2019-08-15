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

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.launching.java.ArgsUtil;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractLauncher;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WildFlyStartLauncher extends AbstractLauncher {
	private static final Logger LOG = LoggerFactory.getLogger(WildFlyStartLauncher.class);

	public WildFlyStartLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}

	@Override
	protected String getWorkingDirectory() {
		String serverHome = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
		return serverHome + "/bin";
	}

	@Override
	protected String getMainTypeName() {
		return "org.jboss.modules.Main";
	}

	@Override
	protected String[] getClasspath() {
		String serverHome = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
		String jbModules = serverHome + "/jboss-modules.jar";
		return addJreClasspathEntries(Arrays.asList(jbModules));
	}

	@Override
	protected String getVMArguments() {
		boolean shouldOverride = getServer().getAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_BOOLEAN, false);
		String overrideArgs = getServer().getAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_VM_ARGS, (String)null);
		if( shouldOverride && overrideArgs != null && overrideArgs.trim().length() > 0 ) {
			return overrideArgs;
		}
		
		// Using defaults
		String ret = calculateVMArgs();
		if( !isEqual(ret, overrideArgs)) {
			// Save these in the server just so that we have it
			saveProperty(IJBossServerAttributes.LAUNCH_OVERRIDE_VM_ARGS, ret);
		}
		return ret;
	}
	
	private String calculateVMArgs() {
		IDefaultLaunchArguments largs = getLaunchArgs();
		String ret = null;
		if( largs != null ) {
			String serverHome = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
			ret = largs.getStartDefaultVMArgs(new Path(serverHome));
			int port = getServer().getAttribute(
					IJBossServerAttributes.JBOSS_SERVER_PORT, (int)-1);
			if( port > 0) {
				ret = ArgsUtil.setSystemProperty(ret, "jboss.http.port", ""+port);
			}
		}
		return ret;
	}
	
	@Override
	protected String getProgramArguments() {
		boolean shouldOverride = getServer().getAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_BOOLEAN, false);
		String overrideArgs = getServer().getAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_PROGRAM_ARGS, (String)null);
		if( shouldOverride && overrideArgs != null && overrideArgs.trim().length() > 0 ) {
			return overrideArgs;
		}
		
		String ret = getCalculatedProgramArgs();
		if( !isEqual(ret, overrideArgs)) {
			// Save these in the server just so that we have it
			saveProperty(IJBossServerAttributes.LAUNCH_OVERRIDE_PROGRAM_ARGS, ret);
		}
		return ret;
	}
	
	private boolean isEqual(String one, String two) {
		return one == null ? two == null : one.equals(two);
	}
	
	private void saveProperty(String key, String val) {
		IServerWorkingCopy wc = getServer().createWorkingCopy();
		wc.setAttribute(key, val);
		try {
			wc.save(new NullProgressMonitor());
		} catch(CoreException ce) {
			LOG.error(ce.getMessage(), ce);
		}
	}
	
	private String getCalculatedProgramArgs() {
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
			
			String configFile = getServer().getAttribute(
					IJBossServerAttributes.WILDFLY_CONFIG_FILE, 
					IJBossServerAttributes.WILDFLY_CONFIG_FILE_DEFAULT);
			r1 = ArgsUtil.setArg(r1, null, "--server-config", configFile);
		}
		return r1;
	}

	
	@Override
	public IServer getServer() {
		return getDelegate().getServer();
	}
}
