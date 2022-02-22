/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.ArgumentUtils;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.foundation.core.launchers.CommandConfig;
import org.jboss.tools.rsp.launching.java.ILaunchModes;
import org.jboss.tools.rsp.launching.utils.NativeEnvironmentUtils;
import org.jboss.tools.rsp.server.spi.launchers.GenericServerProcessRunner;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.IEapXpServerAttributes;

public class EapXpCommandLineStartLauncher extends AbstractCommandLineLauncher {
	private String mode;

	public EapXpCommandLineStartLauncher(IServerDelegate msDelegate, String mode) {
		super(msDelegate);
		this.mode = mode;
	}

	@Override
	@Deprecated
	public String getProgramArguments() {
		// Ignore, never call
		return "";
	}
	
	public String getProgramArguments(String mode, int port) {
		//-Pbootable-jar wildfly-jar:dev-watch -Dwildfly.bootable.debug=true -Dwildfly.bootable.debug.port=65101
		String runArgs = "-Pbootable-jar wildfly-jar:dev-watch";
		if (mode.equals(ILaunchModes.RUN)) {
			return runArgs;
		}
		if (mode.equals(ILaunchModes.DEBUG)) {
			return runArgs + " -Dwildfly.bootable.debug=true -Dwildfly.bootable.debug.port=" + port;
		}
		return null;
	}

	@Override
	protected CommandConfig getCommandConfig(String mode) {
		String cmd = getDelegate().getServer().getAttribute(IEapXpServerAttributes.MAVEN_BIN, (String) null);
		int port = -1;
		try {
			port = allocateLocalPort();
		} catch( CoreException ce ) {
			// Cannot allocate a local port? 
			return null;
		}
		String args = getProgramArguments(mode, port);
		String[] parsed = ArgumentUtils.parseArguments(args);
		String wd = getWorkingDirectory();
		Map<String, String> envMap = NativeEnvironmentUtils.getDefault().getNativeEnvironment();
		String javaHome = getDelegate().getServer().getAttribute(IEapXpServerAttributes.VM_INSTALL_PATH, (String) null);
		if (javaHome != null) {
			envMap.put("JAVA_HOME", javaHome);
		}
		String[] env = NativeEnvironmentUtils.getDefault().getEnvironment(envMap, false);
		CommandConfig details = new CommandConfig(cmd, wd, parsed, env);
		return details;
	}

	@Override
	public String getWorkingDirectory() {
		String wd = getDelegate().getServer().getAttribute(IEapXpServerAttributes.PROJECT_HOME, (String) null);
		return wd;
	}

	private int allocateLocalPort() throws CoreException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.BUNDLE_ID, e.getLocalizedMessage()));
		}
	}

	public GenericServerProcessRunner configureRunner(String mode) {
		if( runner == null ) {
			CommandConfig config = getCommandConfig(mode);
			runner = new GenericServerProcessRunner(getDelegate(), config);
		}
		return runner;
	}
	public ILaunch launch(String mode) throws CoreException {
		getLaunchCommand(mode);
		configureRunner(mode);
		launchedDetails = runner.runWithDetails(launch, new NullProgressMonitor());
		return launch;
	}

}
