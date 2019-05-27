/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import static org.jboss.tools.rsp.itests.util.ServerStateUtil.waitForDeployablePublishState;
import static org.jboss.tools.rsp.itests.util.ServerStateUtil.waitForNonNullServerState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.ServerDeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.itests.RSPCase;
import org.jboss.tools.rsp.itests.util.DeploymentGeneration;
import org.jboss.tools.rsp.itests.util.DummyClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jrichter, odockal
 */
public class WildFlyPublishingTest extends RSPCase {

	private static final String SERVER_ID = "wildfly";
	private static final String WAR_FILENAME = "test.war";
	private final DummyClient client = launcher.getClient();

	private ServerHandle handle;
	private DeployableReference reference;
	private File warFile = createWar();

	@Before
	public void before() throws Exception {
		createServer(WILDFLY_ROOT, SERVER_ID);
		startServer(client, SERVER_ID);
		handle = new ServerHandle(SERVER_ID, wildflyType);
		reference = new DeployableReference(warFile.getName(), warFile.getAbsolutePath());
		serverProxy.addDeployable(new ServerDeployableReference(handle, reference));
	}

	@After
	public void after() throws Exception {
		stopServer(client, SERVER_ID);
		deleteServer(SERVER_ID);
	}

	@Test
	public void testAddDeployment() throws Exception {
		ServerState state = waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_ADD, 10, client);
		assertDeployableState(state, ServerManagementAPIConstants.STATE_UNKNOWN,
				ServerManagementAPIConstants.PUBLISH_STATE_ADD);
	}

	@Test
	public void testPublishDeployment() throws Exception {
		sendPublishRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);
		ServerState state = waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_NONE, 10, client);
		assertDeployableState(state, ServerManagementAPIConstants.STATE_STARTED,
				ServerManagementAPIConstants.PUBLISH_STATE_NONE);
	}

	@Test
	public void testChangedDeployment() throws Exception {
		sendPublishRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);
		ServerState state = waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_NONE, 15, client);
		assertDeployableState(state, ServerManagementAPIConstants.STATE_STARTED,
				ServerManagementAPIConstants.PUBLISH_STATE_NONE);
		warFile.setLastModified(System.currentTimeMillis());
		state = waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, 10, client);
		assertDeployableState(state, ServerManagementAPIConstants.STATE_STARTED,
				ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL);
	}

	@Test
	public void testRemovingDeployment() throws Exception {
		sendPublishRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);
		ServerState state = waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_NONE, 15, client);
		assertDeployableState(state, ServerManagementAPIConstants.STATE_STARTED,
				ServerManagementAPIConstants.PUBLISH_STATE_NONE);

		serverProxy.removeDeployable(new ServerDeployableReference(handle, reference));
		state = waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE, 10, client);
		assertDeployableState(state, ServerManagementAPIConstants.STATE_STARTED,
				ServerManagementAPIConstants.PUBLISH_STATE_REMOVE);

		sendPublishRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);
		state = waitForNonNullServerState(10, client);
		DeployableState ds = getDeployableState(reference, state.getDeployableStates());
		assertNull("state for deployable " + reference.getPath() + " was still found but shouldn't.", ds);
	}

	/*
	 * Breaks the server, see
	 * https://github.com/redhat-developer/rsp-server/issues/376
	 */
	@Ignore
	@Test
	public void testNullPublishRequest() throws InterruptedException, ExecutionException {
		Status status = serverProxy.publish(null).get();
		assertEquals(Status.ERROR, status.getSeverity());
	}

	/*
	 * This test just fails, see
	 * https://github.com/redhat-developer/rsp-server/issues/376
	 */
	@Ignore
	@Test
	public void testInvalidPublishRequest() throws InterruptedException, ExecutionException {
		Status status = serverProxy.publish(new PublishServerRequest(handle, 5000)).get();
		assertEquals(Status.ERROR, status.getSeverity());
	}

	/*
	 * Breaks the server, see
	 * https://github.com/redhat-developer/rsp-server/issues/376
	 */
	@Ignore
	@Test
	public void testRemoveNullDeployment() throws InterruptedException, ExecutionException {
		Status status = serverProxy.removeDeployable(null).get();
		assertEquals(Status.ERROR, status.getSeverity());
	}

	@Test
	public void testRemoveInvalidDeployment() throws InterruptedException, ExecutionException {
		Status status = serverProxy
				.removeDeployable(
						new ServerDeployableReference(handle, new DeployableReference("labelik", "/I/dont/exist")))
				.get();
		assertEquals(Status.ERROR, status.getSeverity());
	}

	/*
	 * Breaks the server, see
	 * https://github.com/redhat-developer/rsp-server/issues/376
	 */
	@Ignore
	@Test
	public void testAddNullDeployment() throws InterruptedException, ExecutionException {
		Status status = serverProxy.addDeployable(null).get();
		assertEquals(Status.ERROR, status.getSeverity());
	}

	@Test
	public void testAddInvalidDeployment() throws InterruptedException, ExecutionException {
		/*
		 * Breaks the server, see
		 * https://github.com/redhat-developer/rsp-server/issues/376
		 */
		// Status status = serverProxy.addDeployable(new
		// ServerDeployableReference(handle, new DeployableReference(WAR_FILENAME,
		// null))).get();
		// assertEquals(Status.ERROR, status.getSeverity());
		// System.out.println(status.getMessage());
		Status status = serverProxy
				.addDeployable(
						new ServerDeployableReference(handle, new DeployableReference(WAR_FILENAME, "/I/dont/exist")))
				.get();
		assertEquals(Status.ERROR, status.getSeverity());
		status = serverProxy.addDeployable(
				new ServerDeployableReference(handle, new DeployableReference(WAR_FILENAME, getProperty("user.dir"))))
				.get();
		assertEquals(Status.ERROR, status.getSeverity());
		status = serverProxy.addDeployable(
				new ServerDeployableReference(handle, new DeployableReference("labelik", warFile.getAbsolutePath())))
				.get();
		assertEquals(Status.ERROR, status.getSeverity());

	}

	@Test
	public void testListDeploymentOptions() throws InterruptedException, ExecutionException {
		Attributes attrs = serverProxy.listDeploymentOptions(handle).get();
		assertNotNull(attrs);
	}

	/*
	 * Breaks the server, see
	 * https://github.com/redhat-developer/rsp-server/issues/376
	 */
	@Ignore
	@Test
	public void testListDeploymentOptionsInvalidAttributes() throws InterruptedException, ExecutionException {
		Attributes attrs = serverProxy.listDeploymentOptions(new ServerHandle("foo.server.id",
				new ServerType("some.id", "my.server", "Random server type definition"))).get();
		assertNull(attrs);
	}

	/*
	 * Breaks the server, see
	 * https://github.com/redhat-developer/rsp-server/issues/376
	 */
	@Ignore
	@Test
	public void testListDeploymentOptionsNullAttributes() throws InterruptedException, ExecutionException {
		Attributes attrs = serverProxy.listDeploymentOptions(null).get();
		assertNull(attrs);
	}

	/*
	 * Breaks the server, see
	 * https://github.com/redhat-developer/rsp-server/issues/376
	 */
	@Ignore
	@Test
	public void testGetDeployablesInvalidAttributes() throws InterruptedException, ExecutionException {
		List<DeployableState> states = serverProxy.getDeployables(new ServerHandle("foo.server.id",
				new ServerType("some.id", "my.server", "Random server type definition"))).get();
		assertTrue(states.isEmpty());
	}

	/*
	 * Breaks the server, see
	 * https://github.com/redhat-developer/rsp-server/issues/376
	 */
	@Ignore
	@Test
	public void testGetDeployablesNullAttributes() throws InterruptedException, ExecutionException {
		List<DeployableState> states = serverProxy.getDeployables(null).get();
		assertFalse(states.isEmpty());
	}

	@Test
	public void testGetDeployables() throws Exception {
		DeployableReference ref2 = new DeployableReference("other_id", WAR_FILENAME);
		serverProxy.addDeployable(new ServerDeployableReference(handle, ref2));
		waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_ADD, 10, client);
		List<DeployableState> states = serverProxy.getDeployables(handle).get();
		assertEquals(2, states.size());
		DeployableReference actualRef = states.get(0).getReference();
		DeployableReference actualRef2 = states.get(1).getReference();
		assertTrue(actualRef.equals(reference) && !actualRef.equals(ref2)
				|| actualRef.equals(ref2) && !actualRef.equals(reference));
		assertTrue(actualRef2.equals(ref2) && !actualRef2.equals(reference)
				|| actualRef2.equals(reference) && !actualRef2.equals(ref2));
	}

	@Test
	public void testGetDeployablesEmptyDeployment() throws Exception {
		waitForDeployablePublishState(ServerManagementAPIConstants.PUBLISH_STATE_ADD, 10, client);
		serverProxy.removeDeployable(new ServerDeployableReference(handle, reference));
		List<DeployableState> states = serverProxy.getDeployables(handle).get();
		assertTrue(states.isEmpty());
	}

	private void assertDeployableState(ServerState state, int serverState, int publishState) {
		DeployableState ds = getDeployableState(reference, state.getDeployableStates());
		assertNotNull("State for deployable " + reference.getPath() + " is null", ds);
		assertEquals(serverState, ds.getState());
		assertEquals(publishState, ds.getPublishState());
	}

	private File createWar() {
		Path deployments = null;
		File war = null;
		try {
			deployments = Files.createTempDirectory(getClass().getName() + "5");
			war = deployments.resolve(WAR_FILENAME).toFile();
			if (!(new DeploymentGeneration().createWar(war))) {
				fail("Failed to create war file");
			}
		} catch (IOException e) {
		}
		return war != null && war.exists() && war.isFile() ? war : null;
	}
}
