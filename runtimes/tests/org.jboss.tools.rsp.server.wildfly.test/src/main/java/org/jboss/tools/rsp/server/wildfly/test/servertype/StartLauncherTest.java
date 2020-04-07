/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.servertype;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMInstallRegistry;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.ServerManagementServerLauncher;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.JBossASServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.JBossASStartLauncher;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerTypes;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyStartLauncher;
import org.junit.Test;

public class StartLauncherTest {

	@Test
	public void testWildfly() throws CoreException, IOException  {
		final VMInstallRegistry reg = new VMInstallRegistry();
		reg.addActiveVM();
		
		
		ServerManagementServerLauncher launcher = mock(ServerManagementServerLauncher.class);
		LauncherSingleton.getDefault().setLauncher(launcher);
		IServerManagementModel model = mock(IServerManagementModel.class);
		doReturn(model).when(launcher).getModel();
		doReturn(reg).when(model).getVMInstallModel();
		
		IServer server = mock(IServer.class);
		IServerType sType = WildFlyServerTypes.WF19_SERVER_TYPE;
		doReturn(sType).when(server).getServerType();
		doReturn(getClass().getName()).when(server).getName();
		File tmp = Files.createTempDirectory(getClass().getName()).toFile();
		new File(tmp, "bin").mkdirs();
		doReturn(tmp.getAbsolutePath()).when(server).getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		WildFlyServerDelegate delegate = new WildFlyServerDelegate(server);
		WildFlyStartLauncher wflyLauncher = new WildFlyStartLauncher(delegate) {
			@Override
			protected IVMInstall getVMInstall(IServerDelegate delegate) {
				return reg.getDefaultVMInstall();
			}
			@Override
			protected void saveProperty(String key, String val) {
				// do nothing
			}
		};
		CommandLineDetails details = wflyLauncher.getLaunchCommand("run");
		String[] line = details.getCmdLine();
		String asOne = String.join(" ", line);
		assertFalse(asOne.contains("default"));
	}
	
	@Test
	public void testLegacyJboss() throws CoreException, IOException  {
		final VMInstallRegistry reg = new VMInstallRegistry();
		reg.addActiveVM();
		
		
		ServerManagementServerLauncher launcher = mock(ServerManagementServerLauncher.class);
		LauncherSingleton.getDefault().setLauncher(launcher);
		IServerManagementModel model = mock(IServerManagementModel.class);
		doReturn(model).when(launcher).getModel();
		doReturn(reg).when(model).getVMInstallModel();
		
		IServer server = mock(IServer.class);
		IServerType sType = WildFlyServerTypes.AS42_SERVER_TYPE;
		doReturn(sType).when(server).getServerType();
		doReturn(getClass().getName()).when(server).getName();
		File tmp = Files.createTempDirectory(getClass().getName()).toFile();
		new File(tmp, "bin").mkdirs();
		doReturn(tmp.getAbsolutePath()).when(server).getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		JBossASServerDelegate delegate = new JBossASServerDelegate(server);
		JBossASStartLauncher wflyLauncher = new JBossASStartLauncher(delegate) {
			@Override
			protected IVMInstall getVMInstall(IServerDelegate delegate) {
				return reg.getDefaultVMInstall();
			}
			@Override
			protected void saveProperty(String key, String val) {
				// do nothing
			}
		};
		CommandLineDetails details = wflyLauncher.getLaunchCommand("run");
		String[] line = details.getCmdLine();
		String asOne = String.join(" ", line);
		assertFalse(asOne.contains("--server-config"));
	}
}
