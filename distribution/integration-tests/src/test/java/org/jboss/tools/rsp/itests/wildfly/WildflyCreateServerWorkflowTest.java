/*******************************************************************************
 * Copyright (c) 2026 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CreateServerWorkflowRequest;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.itests.RSPCase;
import org.junit.Test;

/**
 * Integration tests for the createServerWorkflow API.
 */
public class WildflyCreateServerWorkflowTest extends RSPCase {

	@Test
	public void testInitialWorkflowReturnsPrompts() throws Exception {
		CreateServerWorkflowRequest req = new CreateServerWorkflowRequest();
		req.setRequestId(0);
		req.setServerTypeId(WILDFLY_SERVER_ID);

		WorkflowResponse resp = serverProxy.createServerWorkflow(req)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertEquals(IStatus.INFO, resp.getStatus().getSeverity());
		assertTrue(resp.getRequestId() > 0);

		List<WorkflowResponseItem> items = resp.getItems();
		assertNotNull(items);
		assertFalse(items.isEmpty());

		boolean hasIdPrompt = items.stream()
				.anyMatch(i -> "id".equals(i.getId()));
		assertTrue(hasIdPrompt);

		boolean hasServerHome = items.stream()
				.anyMatch(i -> ServerManagementAPIConstants.SERVER_HOME_DIR.equals(i.getId()));
		assertTrue(hasServerHome);
	}

	@Test
	public void testSubmitWorkflowCreatesServer() throws Exception {
		String serverName = "wfly_workflow_" + System.currentTimeMillis();

		CreateServerWorkflowRequest initial = new CreateServerWorkflowRequest();
		initial.setRequestId(0);
		initial.setServerTypeId(WILDFLY_SERVER_ID);
		WorkflowResponse initialResp = serverProxy.createServerWorkflow(initial)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		long requestId = initialResp.getRequestId();

		CreateServerWorkflowRequest submit = new CreateServerWorkflowRequest();
		submit.setRequestId(requestId);
		submit.setServerTypeId(WILDFLY_SERVER_ID);
		Map<String, Object> data = new HashMap<>();
		data.put("id", serverName);
		data.put(ServerManagementAPIConstants.SERVER_HOME_DIR, WILDFLY_ROOT);
		submit.setData(data);

		WorkflowResponse resp = serverProxy.createServerWorkflow(submit)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertEquals(IStatus.OK, resp.getStatus().getSeverity());

		List<ServerHandle> handles = serverProxy.getServerHandles()
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		boolean found = handles.stream().anyMatch(h -> serverName.equals(h.getId()));
		assertTrue(found);

		deleteServer(serverName);
	}

	@Test
	public void testSubmitWorkflowMissingId() throws Exception {
		CreateServerWorkflowRequest initial = new CreateServerWorkflowRequest();
		initial.setRequestId(0);
		initial.setServerTypeId(WILDFLY_SERVER_ID);
		WorkflowResponse initialResp = serverProxy.createServerWorkflow(initial)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		long requestId = initialResp.getRequestId();

		CreateServerWorkflowRequest submit = new CreateServerWorkflowRequest();
		submit.setRequestId(requestId);
		submit.setServerTypeId(WILDFLY_SERVER_ID);
		Map<String, Object> data = new HashMap<>();
		data.put(ServerManagementAPIConstants.SERVER_HOME_DIR, WILDFLY_ROOT);
		submit.setData(data);

		WorkflowResponse resp = serverProxy.createServerWorkflow(submit)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertEquals(IStatus.ERROR, resp.getStatus().getSeverity());
	}

	@Test
	public void testSubmitWorkflowMissingHome() throws Exception {
		String serverName = "wfly_nohome_" + System.currentTimeMillis();

		CreateServerWorkflowRequest initial = new CreateServerWorkflowRequest();
		initial.setRequestId(0);
		initial.setServerTypeId(WILDFLY_SERVER_ID);
		WorkflowResponse initialResp = serverProxy.createServerWorkflow(initial)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		long requestId = initialResp.getRequestId();

		CreateServerWorkflowRequest submit = new CreateServerWorkflowRequest();
		submit.setRequestId(requestId);
		submit.setServerTypeId(WILDFLY_SERVER_ID);
		Map<String, Object> data = new HashMap<>();
		data.put("id", serverName);
		submit.setData(data);

		WorkflowResponse resp = serverProxy.createServerWorkflow(submit)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertEquals(IStatus.INFO, resp.getStatus().getSeverity());
		assertNotNull(resp.getInvalidFields());
		assertTrue(resp.getInvalidFields().contains(ServerManagementAPIConstants.SERVER_HOME_DIR));
	}

	@Test
	public void testNullRequest() throws Exception {
		WorkflowResponse resp = serverProxy.createServerWorkflow(null)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertEquals(IStatus.ERROR, resp.getStatus().getSeverity());
	}

	@Test
	public void testNullServerType() throws Exception {
		CreateServerWorkflowRequest req = new CreateServerWorkflowRequest();
		req.setRequestId(0);
		req.setServerTypeId(null);

		WorkflowResponse resp = serverProxy.createServerWorkflow(req)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertEquals(IStatus.ERROR, resp.getStatus().getSeverity());
	}

	@Test
	public void testInvalidServerType() throws Exception {
		CreateServerWorkflowRequest req = new CreateServerWorkflowRequest();
		req.setRequestId(0);
		req.setServerTypeId("org.jboss.fake.server.type");

		WorkflowResponse resp = serverProxy.createServerWorkflow(req)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertEquals(IStatus.ERROR, resp.getStatus().getSeverity());
	}

	@Test
	public void testSubmitWorkflowDuplicateServer() throws Exception {
		String serverName = "wfly_dup_" + System.currentTimeMillis();
		createServer(WILDFLY_ROOT, serverName);

		CreateServerWorkflowRequest initial = new CreateServerWorkflowRequest();
		initial.setRequestId(0);
		initial.setServerTypeId(WILDFLY_SERVER_ID);
		WorkflowResponse initialResp = serverProxy.createServerWorkflow(initial)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		long requestId = initialResp.getRequestId();

		CreateServerWorkflowRequest submit = new CreateServerWorkflowRequest();
		submit.setRequestId(requestId);
		submit.setServerTypeId(WILDFLY_SERVER_ID);
		Map<String, Object> data = new HashMap<>();
		data.put("id", serverName);
		data.put(ServerManagementAPIConstants.SERVER_HOME_DIR, WILDFLY_ROOT);
		submit.setData(data);

		WorkflowResponse resp = serverProxy.createServerWorkflow(submit)
				.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertFalse(resp.getStatus().getSeverity() == IStatus.OK);

		deleteServer(serverName);
	}
}
