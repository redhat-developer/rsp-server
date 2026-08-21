/*******************************************************************************
 * Copyright (c) 2026 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.servertype;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.eclipse.jdt.launching.StandardVMType;
import org.jboss.tools.rsp.server.model.internal.publishing.ServerPublishStateModel;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.publishing.IPublishController;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.ServerTypeStringConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EditWildFlyServerTest {

	private File serverHome;
	private File standaloneDir;
	private File configDir;
	private File configFile;

	@Before
	public void setUp() throws IOException {
		Path tmpDir = Files.createTempDirectory("wfly_edit_test");
		serverHome = tmpDir.toFile();
		new File(serverHome, "bin").mkdirs();
		standaloneDir = new File(serverHome, "standalone");
		standaloneDir.mkdirs();
		configDir = new File(standaloneDir, "configuration");
		configDir.mkdirs();
		configFile = new File(configDir, "standalone.xml");
		configFile.createNewFile();
	}

	@After
	public void tearDown() {
		deleteRecursive(serverHome);
	}

	@Test
	public void testUpdateServerHomeUnchanged_succeeds() {
		IServer server = mockServer(serverHome);
		IServer dummyServer = mockServer(serverHome);

		UpdateServerResponse resp = new UpdateServerResponse();
		WildFlyServerDelegate del = (WildFlyServerDelegate) server.getDelegate();
		del.updateServer(dummyServer, resp);

		assertUpdateSucceeded(resp);
	}

	@Test
	public void testUpdateServerHomeChanged_fails() {
		IServer server = mockServer(serverHome);
		File otherHome = new File(serverHome.getParentFile(), "other_home");
		otherHome.mkdirs();
		IServer dummyServer = mockServer(otherHome);

		UpdateServerResponse resp = new UpdateServerResponse();
		WildFlyServerDelegate del = (WildFlyServerDelegate) server.getDelegate();
		del.updateServer(dummyServer, resp);

		assertNotNull(resp.getValidation().getStatus());
		assertFalse(resp.getValidation().getStatus().isOK());
		deleteRecursive(otherHome);
	}

	@Test
	public void testUpdateWithNonExistentHome_fails() {
		IServer server = mockServer(serverHome);
		File fakeHome = new File(serverHome.getParentFile(), "nonexistent");
		IServer dummyServer = mockServer(fakeHome);

		doReturn(fakeHome.getAbsolutePath()).when(dummyServer)
			.getAttribute(eq(ServerManagementAPIConstants.SERVER_HOME_DIR), anyString());
		doReturn(fakeHome.getAbsolutePath()).when(dummyServer)
			.getAttribute(ServerManagementAPIConstants.SERVER_HOME_DIR, (String) null);
		doReturn(fakeHome.getAbsolutePath()).when(dummyServer)
			.getAttribute(eq(IJBossServerAttributes.SERVER_HOME), anyString());
		doReturn(fakeHome.getAbsolutePath()).when(dummyServer)
			.getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);

		UpdateServerResponse resp = new UpdateServerResponse();
		WildFlyServerDelegate del = (WildFlyServerDelegate) server.getDelegate();
		del.updateServer(dummyServer, resp);

		assertFalse(resp.getValidation().getStatus().isOK());
	}

	@Test
	public void testUpdateWithInvalidBaseDir_fails() throws IOException {
		IServer server = mockServer(serverHome);
		IServer dummyServer = mockServer(serverHome);

		doReturn("nonexistent_base").when(dummyServer)
			.getAttribute(eq(IJBossServerAttributes.SERVER_BASE_DIR), anyString());

		UpdateServerResponse resp = new UpdateServerResponse();
		WildFlyServerDelegate del = (WildFlyServerDelegate) server.getDelegate();
		del.updateServer(dummyServer, resp);

		assertFalse(resp.getValidation().getStatus().isOK());
	}

	@Test
	public void testUpdateWithInvalidConfigFile_fails() throws IOException {
		IServer server = mockServer(serverHome);
		IServer dummyServer = mockServer(serverHome);

		doReturn("nonexistent.xml").when(dummyServer)
			.getAttribute(eq(IJBossServerAttributes.WILDFLY_CONFIG_FILE), anyString());

		UpdateServerResponse resp = new UpdateServerResponse();
		WildFlyServerDelegate del = (WildFlyServerDelegate) server.getDelegate();
		del.updateServer(dummyServer, resp);

		assertFalse(resp.getValidation().getStatus().isOK());
	}

	@Test
	public void testUpdateWithAlternateConfigFile_succeeds() throws IOException {
		File altConfig = new File(configDir, "standalone-ha.xml");
		altConfig.createNewFile();

		IServer server = mockServer(serverHome);
		IServer dummyServer = mockServer(serverHome);

		doReturn("standalone-ha.xml").when(dummyServer)
			.getAttribute(eq(IJBossServerAttributes.WILDFLY_CONFIG_FILE), anyString());

		UpdateServerResponse resp = new UpdateServerResponse();
		WildFlyServerDelegate del = (WildFlyServerDelegate) server.getDelegate();
		del.updateServer(dummyServer, resp);

		assertUpdateSucceeded(resp);
	}

	@Test
	public void testUpdateHostAttribute_succeeds() {
		IServer server = mockServer(serverHome);
		IServer dummyServer = mockServer(serverHome);

		doReturn("0.0.0.0").when(dummyServer)
			.getAttribute(eq(IJBossServerAttributes.JBOSS_SERVER_HOST), anyString());

		UpdateServerResponse resp = new UpdateServerResponse();
		WildFlyServerDelegate del = (WildFlyServerDelegate) server.getDelegate();
		del.updateServer(dummyServer, resp);

		assertUpdateSucceeded(resp);
	}

	@Test
	public void testUpdatePortAttribute_succeeds() {
		IServer server = mockServer(serverHome);
		IServer dummyServer = mockServer(serverHome);

		doReturn(9090).when(dummyServer)
			.getAttribute(eq(IJBossServerAttributes.JBOSS_SERVER_PORT), anyInt());

		UpdateServerResponse resp = new UpdateServerResponse();
		WildFlyServerDelegate del = (WildFlyServerDelegate) server.getDelegate();
		del.updateServer(dummyServer, resp);

		assertUpdateSucceeded(resp);
	}

	private static class TestWildFlyServerDelegate extends WildFlyServerDelegate {
		public TestWildFlyServerDelegate(IServer server) {
			super(server);
		}

		@Override
		public void setServerState(int state) {
			super.setServerState(state);
		}

		@Override
		public void setServerState(int state, boolean fire) {
			super.setServerState(state, fire);
		}

		@Override
		protected IServerPublishModel createServerPublishModel() {
			return new ServerPublishStateModel(this, null);
		}

		@Override
		public ServerHandle getServerHandle() {
			return new ServerHandle("test", new ServerType("test5", "test5.name", "test5.desc"));
		}

		@Override
		protected void fireStateChanged(ServerState state) {
		}

		@Override
		public IPublishController getOrCreatePublishController() {
			return super.getOrCreatePublishController();
		}
	}

	private IServer mockServer(File home) {
		IServer server = mock(IServer.class);
		IServerWorkingCopy wc = mock(IServerWorkingCopy.class);
		doReturn(wc).when(server).createWorkingCopy();

		TestWildFlyServerDelegate del = new TestWildFlyServerDelegate(server);
		doReturn(del).when(server).getDelegate();
		IServerType st = mock(IServerType.class);
		doReturn(st).when(server).getServerType();
		doReturn(ServerTypeStringConstants.WF17_ID).when(st).getId();
		doReturn("TestServer").when(server).getName();
		doReturn("TestServer").when(server).getId();

		doReturn(home.getAbsolutePath()).when(server)
			.getAttribute(eq(IJBossServerAttributes.SERVER_HOME), anyString());
		doReturn(home.getAbsolutePath()).when(server)
			.getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
		doReturn(home.getAbsolutePath()).when(server)
			.getAttribute(eq(ServerManagementAPIConstants.SERVER_HOME_DIR), anyString());
		doReturn(home.getAbsolutePath()).when(server)
			.getAttribute(ServerManagementAPIConstants.SERVER_HOME_DIR, (String) null);

		doReturn(IJBossServerAttributes.SERVER_BASE_DIR_DEFAULT).when(server)
			.getAttribute(eq(IJBossServerAttributes.SERVER_BASE_DIR), anyString());
		doReturn(IJBossServerAttributes.WILDFLY_CONFIG_FILE_DEFAULT).when(server)
			.getAttribute(eq(IJBossServerAttributes.WILDFLY_CONFIG_FILE), anyString());

		IServerManagementModel mgmtModel = mock(IServerManagementModel.class);
		doReturn(mgmtModel).when(server).getServerManagementModel();
		IVMInstallRegistry reg = mock(IVMInstallRegistry.class);
		doReturn(reg).when(mgmtModel).getVMInstallModel();

		String javaHome = System.getProperty("java.home");
		File javaHomeFile = new File(javaHome);
		if (javaHomeFile.exists()) {
			IVMInstall vmi = StandardVMType.getDefault().createVMInstall("running");
			vmi.setInstallLocation(javaHomeFile);
			doReturn(vmi).when(reg).getDefaultVMInstall();
		}
		return server;
	}

	private void assertUpdateSucceeded(UpdateServerResponse resp) {
		org.jboss.tools.rsp.api.dao.Status status = resp.getValidation().getStatus();
		assertTrue(status == null || status.isOK());
	}

	private void deleteRecursive(File f) {
		if (f == null || !f.exists()) return;
		if (f.isDirectory()) {
			File[] children = f.listFiles();
			if (children != null) {
				for (File child : children) {
					deleteRecursive(child);
				}
			}
		}
		f.delete();
	}
}
