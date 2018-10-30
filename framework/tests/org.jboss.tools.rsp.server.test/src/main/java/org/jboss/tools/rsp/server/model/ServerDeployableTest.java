/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.launching.utils.IMemento;
import org.jboss.tools.rsp.launching.utils.JSONMemento;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.AbstractServerType;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.util.DataLocationSysProp;
import org.jboss.tools.rsp.server.util.generation.DeploymentGeneration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServerDeployableTest {

	private static final DataLocationSysProp dataLocation = new DataLocationSysProp();

	@BeforeClass
	public static void beforeClass() {
		dataLocation.backup().set("ServerDeployableTest");
	}

	@AfterClass
	public static void afterClass() {
		dataLocation.restore();
	}

	private ServerModel sm;
	private File war;

	@Before
	public void before() {
		this.sm = new ServerModel(mock(IServerManagementModel.class));
		this.war = createWar(sm);
	}

	protected File createWar(ServerModel sm) {
		Path deployments = null;
		File war = null;
		try {
			deployments = Files.createTempDirectory("serverdeployabletest_deployments");
			war = deployments.resolve("hello-world-war-1.0.0.war").toFile();
			if (!(new DeploymentGeneration().createWar(war))) {
				fail();
			}
		} catch (IOException e) {}
		return war != null && war.exists() && war.isFile() ? war : null;
	}

	protected File createDataLoc(ServerModel sm) {
		return createDataLoc(sm, getInitialServerString("abc123", "wonka6"), 1);
	}
	
	protected File createDataLoc(ServerModel sm, String initial, int expectedSize) {

		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("serverdeployabletest");
			s1 = dir.resolve("s1");
			Files.write(s1, initial.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), expectedSize);
		} catch (IOException e) {
			if (s1 != null && s1.toFile().exists()) {
				s1.toFile().delete();
				s1.toFile().getParentFile().delete();
			}
			fail();
		}
		return s1.toFile();
	}

	@Test
	public void testDeployablesAddRemoveNoPublish() {
		sm.addServerType(mockServerType("wonka6"));
		createDataLoc(sm);
		ServerHandle handle = sm.getServerHandles()[0];
		IServer server = sm.getServer(handle.getId());
		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.isEmpty());

		DeployableReference reference = new DeployableReference("some.name", war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());

		deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.size() == 1);

		IStatus removed = sm.removeDeployable(server, reference);
		assertNotNull(removed);
		assertTrue(removed.isOK());

		deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.isEmpty());

	}
	
	@Test
	public void testDeployablesAddSaveRemoveSave() {
		sm.addServerType(mockServerType("wonka6"));
		File serverFile = createDataLoc(sm);
		ServerHandle handle = sm.getServerHandles()[0];
		IServer server = sm.getServer(handle.getId());

		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.isEmpty());

		DeployableReference reference = new DeployableReference("some.name", war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());

		deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.size() == 1);

		try {
			sm.saveServers();
			JSONMemento memento = JSONMemento.loadMemento(new FileInputStream(serverFile));
			IMemento[] modules = memento.getChildren("modules");
			assertNotNull(modules);
			assertEquals(1, modules.length);
			IMemento[] module = modules[0].getChildren("module");
			assertNotNull(module);
			assertEquals(1, module.length);
			assertEquals("some.name", module[0].getString("id"));
			assertEquals(war.getAbsolutePath(), module[0].getString("path"));
		} catch(IOException | CoreException ioe) {
			ioe.printStackTrace();
			fail();
		}
		
		

		IStatus removed = sm.removeDeployable(server, reference);
		assertNotNull(removed);
		assertTrue(removed.isOK());

		deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.isEmpty());
		

		try {
			sm.saveServers();
			JSONMemento memento = JSONMemento.loadMemento(new FileInputStream(serverFile));
			IMemento[] modules = memento.getChildren("modules");
			boolean dne = (modules == null || modules.length == 0);
			assertTrue(dne);
		} catch(IOException | CoreException ioe) {
			ioe.printStackTrace();
			fail();
		}

	}

	
	
	@Test
	public void testDeployablesLoadFromData() {
		sm.addServerType(mockServerType("wonka6"));
		createDataLoc(sm, getServerWithDeployableString("abc123", "wonka6"), 1);
		ServerHandle handle = sm.getServerHandles()[0];
		IServer server = sm.getServer(handle.getId());

		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.size() == 1);

		DeployableState ds1 = deployables.get(0);
		assertNotNull(ds1);
		assertEquals(ds1.getState(), ServerManagementAPIConstants.STATE_UNKNOWN);
		assertEquals(ds1.getPublishState(), ServerManagementAPIConstants.PUBLISH_STATE_FULL);
		assertEquals(ds1.getReference().getId(), "some.name");
	}
	
	
	private String getInitialServerString(String name, String type) {
		String contents = "{id:\"" + name + "\", id-set:\"true\", " 
				+ "org.jboss.tools.rsp.server.typeId=\"" + type
				+ "\"}\n";
		return contents;
	}

	private String getServerWithDeployableString(String name, String type) {
		String contents = "{\n" + 
				"  \"id-set\": \"true\",\n" + 
				"  \"org.jboss.tools.rsp.server.typeId\": \"" + type  + "\",\n" + 
				"  \"id\": \"" + name + "\",\n" + 
				"  \"modules\": {\n" + 
				"    \"module\": {\n" + 
				"      \"id\": \"some.name\",\n" + 
				"      \"path\": \"/tmp/serverdeployabletest_deployments1557855048044620815/hello-world-war-1.0.0.war\"\n" + 
				"    }\n" + 
				"  }\n" + 
				"}\n" + 
				"";
		return contents;
	}


	@Test
	public void testDefaultPublishImplementation() {
		sm.addServerType(mockServerType("wonka6"));
		File serverFile = createDataLoc(sm);
		ServerHandle handle = sm.getServerHandles()[0];
		IServer server = sm.getServer(handle.getId());

		DeployableReference reference = new DeployableReference("some.name", war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());

		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.size() == 1);
		
		ServerState ss = sm.getServer(handle.getId()).getDelegate().getServerState();
		List<DeployableState> dState = ss.getModuleState();
		assertNotNull(dState);
		assertEquals(dState.size(), 1);
		DeployableState oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(oneState.getPublishState(), ServerManagementAPIConstants.PUBLISH_STATE_ADD);
		assertEquals(oneState.getState(), ServerManagementAPIConstants.STATE_UNKNOWN);
		
		// Now do the publish
		try {
			sm.publish(server, ServerManagementAPIConstants.PUBLISH_FULL);
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		
		// Verify module is set to no publish required and module is started
		ss = sm.getServer(handle.getId()).getDelegate().getServerState();
		dState = ss.getModuleState();
		assertNotNull(dState);
		assertEquals(dState.size(), 1);
		oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(oneState.getPublishState(), ServerManagementAPIConstants.PUBLISH_STATE_NONE);
		assertEquals(oneState.getState(), ServerManagementAPIConstants.STATE_STARTED);
	}
	
	private CountDownLatch startSignal1;
	private CountDownLatch doneSignal1;
	private CountDownLatch startSignal2;
	private CountDownLatch doneSignal2;

	@Test
	public void testDefaultPublishImplementationWithDelay() {
		ServerModel sm = new ServerModel(mock(IServerManagementModel.class));
		sm.addServerType(mockServerType("wonka6", 2));
		createDataLoc(sm);
		ServerHandle handle = sm.getServerHandles()[0];
		IServer server = sm.getServer(handle.getId());

		DeployableReference reference = new DeployableReference("some.name", war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());

		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.size() == 1);
		
		ServerState ss = sm.getServer(handle.getId()).getDelegate().getServerState();
		List<DeployableState> dState = ss.getModuleState();
		assertNotNull(dState);
		assertEquals(dState.size(), 1);
		DeployableState oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(oneState.getPublishState(), ServerManagementAPIConstants.PUBLISH_STATE_ADD);
		assertEquals(oneState.getState(), ServerManagementAPIConstants.STATE_UNKNOWN);
		
		// Now do the publish
		startSignal1 = new CountDownLatch(1);
		doneSignal1 = new CountDownLatch(1);
		startSignal2 = new CountDownLatch(1);
		doneSignal2 = new CountDownLatch(1);

		try {
			sm.publish(server, ServerManagementAPIConstants.PUBLISH_FULL);
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		
		// Verify module is set to no publish required and module is started
		ss = sm.getServer(handle.getId()).getDelegate().getServerState();
		dState = ss.getModuleState();
		assertNotNull(dState);
		assertEquals(dState.size(), 1);
		oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(oneState.getPublishState(), ServerManagementAPIConstants.PUBLISH_STATE_ADD);
		assertEquals(oneState.getState(), ServerManagementAPIConstants.STATE_UNKNOWN);
		
		// countdown once
		startSignal1.countDown();
		try {
			doneSignal1.await();
		} catch(InterruptedException ie) {}
		
		ss = sm.getServer(handle.getId()).getDelegate().getServerState();
		dState = ss.getModuleState();
		assertNotNull(dState);
		assertEquals(dState.size(), 1);
		oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(oneState.getPublishState(), ServerManagementAPIConstants.PUBLISH_STATE_NONE);
		assertEquals(oneState.getState(), ServerManagementAPIConstants.STATE_UNKNOWN);
		
		// countdown once
		startSignal2.countDown();
		try {
			doneSignal2.await();
		} catch(InterruptedException ie) {}
		
		ss = sm.getServer(handle.getId()).getDelegate().getServerState();
		dState = ss.getModuleState();
		assertNotNull(dState);
		assertEquals(dState.size(), 1);
		oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(oneState.getPublishState(), ServerManagementAPIConstants.PUBLISH_STATE_NONE);
		assertEquals(oneState.getState(), ServerManagementAPIConstants.STATE_STARTED);

	}
	
	
//	String original = new String(Files.readAllBytes(Paths.get(TEST_JSON_PATH)));
//	String o2 = original.replaceAll("\\s","");

	private IServerType mockServerType(String typeId) {
		return mockServerType(typeId, 1);
	}
	private IServerType mockServerType(String typeId, int type) {
		return new TestServerType(typeId, typeId + ".name", typeId + ".desc", type);
	}

	private class TestServerDelegateWithDelay extends AbstractServerDelegate {
		public TestServerDelegateWithDelay(IServer server) {
			super(server);
		}
		@Override
		public CommandLineDetails getStartLaunchCommand(String mode, ServerAttributes params) {
			return null;
		}
		protected void publishModule(DeployableReference reference, int publishType, int modulePublishType) throws CoreException {
			new Thread("Test publish") {
				public void run() {
					try {
						startSignal1.await();
					} catch(InterruptedException ie) {}
					setModulePublishState2(reference, ServerManagementAPIConstants.PUBLISH_STATE_NONE);
					doneSignal1.countDown();
					
					try {
						startSignal2.await();
					} catch(InterruptedException ie) {}
					setModuleState2(reference, ServerManagementAPIConstants.STATE_STARTED);
					doneSignal2.countDown();
				}
			}.start();
		}
		
		protected void setModulePublishState2(DeployableReference reference, int publishState) {
			setModulePublishState(reference, publishState);
		}

		protected void setModuleState2(DeployableReference reference, int runState) {
			setModuleState(reference, runState);
		}
	}
	private class TestServerDelegate extends AbstractServerDelegate {
		public TestServerDelegate(IServer server) {
			super(server);
		}
		@Override
		public CommandLineDetails getStartLaunchCommand(String mode, ServerAttributes params) {
			return null;
		}
	}
	
	private class TestServerType extends AbstractServerType {
		private int delegateType;
		public TestServerType(String id, String name, String desc, int delegate) {
			super(id, name, desc);
			this.delegateType = delegate;
		}
		@Override
		public IServerDelegate createServerDelegate(IServer server) {
			if( delegateType == 1 )
				return new TestServerDelegate(server);
			if( delegateType == 2 )
				return new TestServerDelegateWithDelay(server);
			return null;
		}
	}

}
