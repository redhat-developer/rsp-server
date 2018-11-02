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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
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

	private static final String SERVER_FILENAME = "s1";
	private static final String SERVERS_DIR = "serverdeployabletest";
	private static final String SERVER_ID = "abc123";
	private static final String SERVER_TYPE = "wonka6";
	private static final String DEPLOYMENTS_DIR = SERVERS_DIR + "_deployments";
	private static final String DEPLOYABLE_LABEL = "some.name";
	private static final String WAR_FILENAME = "hello-world-war-1.0.0.war";

	private static final DataLocationSysProp dataLocation = new DataLocationSysProp();

	/* TODO: remove duplication from Server */
	private static final String MEMENTO_DEPLOYABLES = "deployables";
	private static final String MEMENTO_DEPLOYABLE = "deployable";
	private static final String MEMENTO_DEPLOYABLE_LABEL = "label";
	private static final String MEMENTO_DEPLOYABLE_PATH = "path";

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
	private Path serversDir;
	private File serverFile;
	private IServer server;

	@Before
	public void before() throws IOException {
		this.serversDir = Files.createTempDirectory(SERVERS_DIR);
		this.war = createWar();
		this.sm = new ServerModel(mock(IServerManagementModel.class));
		sm.addServerType(mockServerType(SERVER_TYPE, TestServerDelegate::new));
		this.serverFile = createServerFile(SERVER_FILENAME, getServerWithoutDeployablesString(SERVER_ID, SERVER_TYPE));
		sm.loadServers(serversDir.toFile());
		this.server = sm.getServer(SERVER_ID);
	}

	@Test
	public void testCannotAddDeployable() {
		ServerModel sm = createServerModel(
				(IServer server) -> {
					IServerDelegate spy = spy(new TestServerDelegate(server));
					doReturn(Status.CANCEL_STATUS).when(spy).canAddDeployable(any(DeployableReference.class));
					return spy;
				},
				getServerWithoutDeployablesString(SERVER_ID, SERVER_TYPE));

		IServer server = sm.getServer(SERVER_ID);
		assertTrue(sm.getDeployables(server).isEmpty());
		DeployableReference reference = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertFalse(added.isOK());
		assertTrue(sm.getDeployables(server).isEmpty());
	}

	@Test
	public void testCannotRemoveDeployable() {
		ServerModel sm = createServerModel(
				(IServer server) -> {
					IServerDelegate spy = spy(new TestServerDelegate(server));
					doReturn(Status.CANCEL_STATUS).when(spy).canRemoveDeployable(any(DeployableReference.class));
					return spy;
				},
				getServerWithoutDeployablesString(SERVER_ID, SERVER_TYPE));

		IServer server = sm.getServer(SERVER_ID);
		assertTrue(sm.getDeployables(server).isEmpty());
		DeployableReference reference = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());
		assertEquals(1, sm.getDeployables(server).size());

		IStatus removed = sm.removeDeployable(server, reference);
		assertNotNull(removed);
		assertFalse(removed.isOK());
		assertEquals(1, sm.getDeployables(server).size());
	}

	@Test
	public void testCanAddMultipleDeployables() {
		assertTrue(sm.getDeployables(server).isEmpty());
		DeployableReference reference = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());
		assertNotNull(sm.getDeployables(server));
		assertEquals(1, sm.getDeployables(server).size());

		DeployableReference reference2 = new DeployableReference(DEPLOYABLE_LABEL, "/smurfette/in/da/woods");
		added = sm.addDeployable(server, reference2);
		assertNotNull(added);
		assertTrue(added.isOK());
		assertEquals(2, sm.getDeployables(server).size());
	}

	@Test
	public void testCanNotAddSamePath() {
		assertTrue(sm.getDeployables(server).isEmpty());
		DeployableReference reference = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());
		assertNotNull(sm.getDeployables(server));
		assertEquals(1, sm.getDeployables(server).size());

		DeployableReference reference2 = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		added = sm.addDeployable(server, reference2);
		assertNotNull(added);
		assertFalse(added.isOK());
		assertEquals(1, sm.getDeployables(server).size());

		reference2 = new DeployableReference("papa-smurf", war.getAbsolutePath());
		added = sm.addDeployable(server, reference2);
		assertNotNull(added);
		assertFalse(added.isOK());
		assertEquals(1, sm.getDeployables(server).size());
	}

	@Test
	public void testCannotRemoveInexistantPath() {		
		assertTrue(sm.getDeployables(server).isEmpty());
		DeployableReference reference = new DeployableReference(DEPLOYABLE_LABEL, "/gargamel/in/da/castle");
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());
		assertNotNull(sm.getDeployables(server));
		assertEquals(1, sm.getDeployables(server).size());

		DeployableReference reference2 = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		IStatus removed = sm.removeDeployable(server, reference2);
		assertNotNull(removed);
		assertFalse(removed.isOK());
		assertEquals(1, sm.getDeployables(server).size());

		reference2 = new DeployableReference("papa-smurf", war.getAbsolutePath());
		removed = sm.removeDeployable(server, reference2);
		assertNotNull(removed);
		assertFalse(removed.isOK());
		assertEquals(1, sm.getDeployables(server).size());
	}

	@Test
	public void testDeployablesAddRemoveNoPublish() {
		IServer server = sm.getServer(SERVER_ID);
		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.isEmpty());

		DeployableReference reference = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());

		deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertEquals(1, deployables.size());

		IStatus removed = sm.removeDeployable(server, reference);
		assertNotNull(removed);
		assertTrue(removed.isOK());

		deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.isEmpty());
	}
	
	@Test
	public void testDeployablesAddSaveRemoveSave() {
		IServer server = sm.getServer(SERVER_ID);

		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertTrue(deployables.isEmpty());

		DeployableReference reference = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());

		deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertEquals(1, deployables.size());

		try {
			sm.saveServers();
			JSONMemento memento = JSONMemento.loadMemento(new FileInputStream(serverFile));
			IMemento[] deployablesMemento = memento.getChildren(MEMENTO_DEPLOYABLES);
			assertNotNull(deployablesMemento);
			assertEquals(1, deployablesMemento.length);
			IMemento[] deployableMemento = deployablesMemento[0].getChildren(MEMENTO_DEPLOYABLE);
			assertNotNull(deployableMemento);
			assertEquals(1, deployableMemento.length);
			assertEquals(DEPLOYABLE_LABEL, deployableMemento[0].getString(MEMENTO_DEPLOYABLE_LABEL));
			assertEquals(war.getAbsolutePath(), deployableMemento[0].getString(MEMENTO_DEPLOYABLE_PATH));
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
			IMemento[] deployablesMemento = memento.getChildren(MEMENTO_DEPLOYABLES);
			boolean dne = (deployablesMemento == null || deployablesMemento.length == 0);
			assertTrue(dne);
		} catch(IOException | CoreException ioe) {
			ioe.printStackTrace();
			fail();
		}
	}

	@Test
	public void testDeployablesLoadFromData() {
		ServerModel sm = createServerModel(TestServerDelegate::new, getServerWithDeployablesString(SERVER_ID, SERVER_TYPE));
		IServer server = sm.getServer(SERVER_ID);
		
		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertEquals(1, deployables.size());

		DeployableState ds1 = deployables.get(0);
		assertNotNull(ds1);
		assertEquals(ServerManagementAPIConstants.STATE_UNKNOWN, ds1.getState());
		assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_FULL, ds1.getPublishState());
		assertEquals(DEPLOYABLE_LABEL, ds1.getReference().getLabel());
	}

	@Test
	public void testDefaultPublishImplementation() {
		IServer server = sm.getServer(SERVER_ID);

		DeployableReference reference = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());

		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertEquals(1, deployables.size());
		
		ServerState ss = server.getDelegate().getServerState();
		List<DeployableState> dState = ss.getDeployableStates();
		assertNotNull(dState);
		assertEquals(1, dState.size());
		DeployableState oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_ADD, oneState.getPublishState());
		assertEquals(ServerManagementAPIConstants.STATE_UNKNOWN, oneState.getState());
		
		// Now do the publish
		try {
			sm.publish(server, ServerManagementAPIConstants.PUBLISH_FULL);
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		
		// Verify module is set to no publish required and module is started
		ss = server.getDelegate().getServerState();
		dState = ss.getDeployableStates();
		assertNotNull(dState);
		assertEquals(1, dState.size());
		oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_NONE, oneState.getPublishState());
		assertEquals(ServerManagementAPIConstants.STATE_STARTED, oneState.getState());
	}
	
	private CountDownLatch startSignal1;
	private CountDownLatch doneSignal1;
	private CountDownLatch startSignal2;
	private CountDownLatch doneSignal2;

	@Test
	public void testDefaultPublishImplementationWithDelay() {
		ServerModel sm = createServerModel(TestServerDelegateWithDelay::new, getServerWithoutDeployablesString(SERVER_ID, SERVER_TYPE));
		IServer server = sm.getServer(SERVER_ID);

		DeployableReference reference = new DeployableReference(DEPLOYABLE_LABEL, war.getAbsolutePath());
		IStatus added = sm.addDeployable(server, reference);
		assertNotNull(added);
		assertTrue(added.isOK());

		List<DeployableState> deployables = sm.getDeployables(server);
		assertNotNull(deployables);
		assertEquals(1, deployables.size());
		
		ServerState ss = server.getDelegate().getServerState();
		List<DeployableState> dState = ss.getDeployableStates();
		assertNotNull(dState);
		assertEquals(1, dState.size());
		DeployableState oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_ADD, oneState.getPublishState());
		assertEquals(ServerManagementAPIConstants.STATE_UNKNOWN, oneState.getState());

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
		ss = server.getDelegate().getServerState();
		dState = ss.getDeployableStates();
		assertNotNull(dState);
		assertEquals(1, dState.size());
		oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_ADD, oneState.getPublishState());
		assertEquals(ServerManagementAPIConstants.STATE_UNKNOWN, oneState.getState());
		
		// countdown once
		startSignal1.countDown();
		try {
			doneSignal1.await();
		} catch(InterruptedException ie) {}
		
		ss = server.getDelegate().getServerState();
		dState = ss.getDeployableStates();
		assertNotNull(dState);
		assertEquals(1, dState.size());
		oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_NONE, oneState.getPublishState());
		assertEquals(ServerManagementAPIConstants.STATE_UNKNOWN, oneState.getState());
		
		// countdown once
		startSignal2.countDown();
		try {
			doneSignal2.await();
		} catch(InterruptedException ie) {}
		
		ss = server.getDelegate().getServerState();
		dState = ss.getDeployableStates();
		assertNotNull(dState);
		assertEquals(1, dState.size());
		oneState = dState.get(0);
		assertNotNull(oneState);
		assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_NONE, oneState.getPublishState());
		assertEquals(ServerManagementAPIConstants.STATE_STARTED, oneState.getState());
	}

	private IServerType mockServerType(String typeId, Function<IServer, IServerDelegate> delegateProvider) {
		return new TestServerType(typeId, typeId + ".name", typeId + ".desc", delegateProvider);
	}

	private class TestServerDelegateWithDelay extends AbstractServerDelegate {
		public TestServerDelegateWithDelay(IServer server) {
			super(server);
		}
		@Override
		public CommandLineDetails getStartLaunchCommand(String mode, ServerAttributes params) {
			return null;
		}
		protected void publishDeployable(DeployableReference reference, int publishType, int modulePublishType) throws CoreException {
			new Thread("Test publish") {
				public void run() {
					try {
						startSignal1.await();
					} catch(InterruptedException ie) {}
					setDeployablePublishState2(reference, ServerManagementAPIConstants.PUBLISH_STATE_NONE);
					doneSignal1.countDown();
					
					try {
						startSignal2.await();
					} catch(InterruptedException ie) {}
					setDeployableState2(reference, ServerManagementAPIConstants.STATE_STARTED);
					doneSignal2.countDown();
				}
			}.start();
		}
		
		protected void setDeployablePublishState2(DeployableReference reference, int publishState) {
			setDeployablePublishState(reference, publishState);
		}

		protected void setDeployableState2(DeployableReference reference, int runState) {
			setDeployableState(reference, runState);
		}
	}

	public class TestServerDelegate extends AbstractServerDelegate {

		public TestServerDelegate(IServer server) {
			super(server);
		}

		@Override
		public CommandLineDetails getStartLaunchCommand(String mode, ServerAttributes params) {
			return null;
		}
	}
	
	public class TestServerType extends AbstractServerType {

		private Function<IServer, IServerDelegate> delegateProvider;

		public TestServerType(String id, String name, String desc, Function<IServer, IServerDelegate> delegateProvider) {
			super(id, name, desc);
			this.delegateProvider = delegateProvider;
		}

		@Override
		public IServerDelegate createServerDelegate(IServer server) {
			return delegateProvider.apply(server);
		}
	}

	protected File createWar() {
		Path deployments = null;
		File war = null;
		try {
			deployments = Files.createTempDirectory(DEPLOYMENTS_DIR);
			war = deployments.resolve(WAR_FILENAME).toFile();
			if (!(new DeploymentGeneration().createWar(war))) {
				fail();
			}
		} catch (IOException e) {}
		return war != null && war.exists() && war.isFile() ? war : null;
	}

	protected File createServerFile(String filename, String initial) {
		Path s1 = null;
		try {
			s1 = serversDir.resolve(filename);
			Files.write(s1, initial.getBytes());
		} catch (IOException e) {
			if (s1 != null && s1.toFile().exists()) {
				s1.toFile().delete();
				s1.toFile().getParentFile().delete();
			}
			fail();
		}
		return s1.toFile();
	}

	private ServerModel createServerModel(Function<IServer, IServerDelegate> serverDelegateProvider, String serverString) {
		ServerModel sm = new ServerModel(mock(IServerManagementModel.class));
		sm.addServerType(mockServerType(SERVER_TYPE, serverDelegateProvider));
		createServerFile(SERVER_FILENAME, serverString);
		sm.loadServers(serversDir.toFile());
		return sm;
	}

	private String getServerWithoutDeployablesString(String name, String type) {
		String contents = "{id:\"" + name + "\", id-set:\"true\", " 
				+ "org.jboss.tools.rsp.server.typeId=\"" + type
				+ "\"}\n";
		return contents;
	}

	private String getServerWithDeployablesString(String name, String type) {
		String contents = "{\n" + 
				"  \"id-set\": \"true\",\n" + 
				"  \"org.jboss.tools.rsp.server.typeId\": \"" + type  + "\",\n" + 
				"  \"id\": \"" + name + "\",\n" + 
				"  \"deployables\": {\n" + 
				"    \"deployable\": {\n" + 
				"      \"label\": \"some.name\",\n" + 
				"      \"path\": \"/tmp/serverdeployabletest_deployments1557855048044620815/hello-world-war-1.0.0.war\"\n" + 
				"    }\n" + 
				"  }\n" + 
				"}\n" + 
				"";
		return contents;
	}

}
