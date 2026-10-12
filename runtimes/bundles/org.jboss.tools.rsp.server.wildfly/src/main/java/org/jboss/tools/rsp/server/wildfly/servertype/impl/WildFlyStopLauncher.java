/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractLauncher;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.JBossVMRegistryDiscovery;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JavaLaunchArgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WildFlyStopLauncher extends AbstractLauncher implements IServerShutdownLauncher{
	private static final Logger LOG = LoggerFactory.getLogger(WildFlyStopLauncher.class);

	public WildFlyStopLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}

	public ILaunch launch(boolean force) throws CoreException {
		IServerDelegate delegate = getDelegate();
		ILaunch launch = (ILaunch) delegate.getSharedData(AbstractJBossServerDelegate.START_LAUNCH_SHARED_DATA);
		if( force && terminateProcesses(launch)) {
			return null;
		}
		return launch("run");
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
		IPath home = new Path(serverHome).append("jboss-modules.jar");
		return new String[] { home.toOSString() };
	}

	protected String getVMArguments() {
		boolean shouldOverride = getServer().getAttribute(IJBossServerAttributes.SHUTDOWN_LAUNCH_OVERRIDE_BOOLEAN, false);
		if (shouldOverride) {
			String overrideArgs = getServer().getAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_SHUTDOWN_VM_ARGS, (String) null);
			if (overrideArgs != null && overrideArgs.trim().length() > 0) {
				return overrideArgs;
			}
		}

		String ret = JavaLaunchArgUtil.getJavaVMArgs(new JBossVMRegistryDiscovery().findVMInstall(getDelegate()));
		if (shouldOverride) {
			saveProperty(IJBossServerAttributes.LAUNCH_OVERRIDE_SHUTDOWN_VM_ARGS, ret);
		}
		return ret;
	}

	protected String getProgramArguments() {
		boolean shouldOverride = getServer().getAttribute(IJBossServerAttributes.SHUTDOWN_LAUNCH_OVERRIDE_BOOLEAN, false);
		if (shouldOverride) {
			String overrideArgs = getServer().getAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_SHUTDOWN_PROGRAM_ARGS, (String) null);
			if (overrideArgs != null && overrideArgs.trim().length() > 0) {
				return overrideArgs;
			}
		}

		String ret = calculateProgramArgs();
		if (shouldOverride) {
			saveProperty(IJBossServerAttributes.LAUNCH_OVERRIDE_SHUTDOWN_PROGRAM_ARGS, ret);
		}
		return ret;
	}

	private String calculateProgramArgs() {
		IDefaultLaunchArguments largs = getLaunchArgs();
		if (largs != null) {
			return largs.getDefaultStopArgs();
		}
		return "";
	}

	private boolean isEqual(String one, String two) {
		return one == null ? two == null : one.equals(two);
	}

	private void saveProperty(String key, String val) {
		IServerWorkingCopy wc = getServer().createWorkingCopy();
		wc.setAttribute(key, val);
		try {
			wc.save(new NullProgressMonitor());
		} catch (CoreException ce) {
			LOG.error(ce.getMessage(), ce);
		}
	}

	@Override
	public IServer getServer() {
		return getDelegate().getServer();
	}
}
