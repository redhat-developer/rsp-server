/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.servertype.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.server.wildfly.servertype.actions.ShowInBrowserActionHandler;
import org.junit.Test;

public class ShowInBrowserActionTest {

	@Test
	public void testActionInitialWorkflowNoDeployments() {
		TestableShowInBrowserActionHandler testable = new TestableShowInBrowserActionHandler(
				"http://127.0.0.1:8080", Collections.emptyList());
		assertActionDetails(testable, 1, new String[] { 
				ShowInBrowserActionHandler.ACTION_SHOW_IN_BROWSER_SELECT_SERVER_ROOT
		});
	}

	@Test
	public void testActionInitialWorkflowOneDeployment() throws IOException {
		List<DeployableState> states = new ArrayList<>();
		File tmpFolder = Files.createTempDirectory(getClass().getName()).toFile();
		File tmpFile = new File(tmpFolder, "sample.war");
		DeployableState ds = createState("server1", "wonka", 
				"dumbLabel", tmpFile.getAbsolutePath(), null);
		states.add(ds);
		
		TestableShowInBrowserActionHandler testable = new TestableShowInBrowserActionHandler(
				"http://127.0.0.1:8080", states);
		assertActionDetails(testable, 2, new String[] { 
				ShowInBrowserActionHandler.ACTION_SHOW_IN_BROWSER_SELECT_SERVER_ROOT,
				tmpFile.getAbsolutePath()
		});
	}

	@Test
	public void testActionHandlingSelectNull() throws IOException {
		WorkflowResponse workflowResponse = runTestActionHandlingSelect(null);
		assertNotNull(workflowResponse);
		assertNotNull(workflowResponse.getStatus());
		assertEquals(workflowResponse.getStatus().getSeverity(), IStatus.CANCEL);
	}

	@Test
	public void testActionHandlingSelectRoot() throws IOException {
		WorkflowResponse workflowResponse = runTestActionHandlingSelect(
				ShowInBrowserActionHandler.ACTION_SHOW_IN_BROWSER_SELECT_SERVER_ROOT);
		assertNotNull(workflowResponse);
		assertNotNull(workflowResponse.getStatus());
		assertEquals(workflowResponse.getStatus().getSeverity(), IStatus.OK);
		assertNotNull(workflowResponse.getItems());
		assertNotNull(workflowResponse.getItems().get(0));
		WorkflowResponseItem item = workflowResponse.getItems().get(0);
		assertEquals(item.getItemType(), ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_BROWSER);
		assertEquals(item.getContent(), "http://127.0.0.1:8080");
	}

	@Test
	public void testActionHandlingSelectNonNullGarbage() throws IOException {
		WorkflowResponse workflowResponse = runTestActionHandlingSelect("randomString");
		assertNotNull(workflowResponse);
		assertNotNull(workflowResponse.getStatus());
		assertEquals(workflowResponse.getStatus().getSeverity(), IStatus.CANCEL);
		assertNotNull(workflowResponse.getItems());
		assertTrue(workflowResponse.getItems().size() == 0);
	}

	@Test
	public void testActionHandlingSelectDeployment() throws IOException {
		File tmpFolder = Files.createTempDirectory(getClass().getName()).toFile();
		File tmpFile = new File(tmpFolder, "sample.war");

		
		WorkflowResponse workflowResponse = runTestActionHandlingSelect(
				tmpFile, tmpFile.getAbsolutePath());
		assertNotNull(workflowResponse);
		assertNotNull(workflowResponse.getStatus());
		assertEquals(workflowResponse.getStatus().getSeverity(), IStatus.OK);
		assertNotNull(workflowResponse.getItems());
		assertNotNull(workflowResponse.getItems().get(0));
		WorkflowResponseItem item = workflowResponse.getItems().get(0);
		assertEquals(item.getItemType(), ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_BROWSER);
		assertEquals(item.getContent(), "http://127.0.0.1:8080/sample");
	}

	public WorkflowResponse runTestActionHandlingSelect(String responseData) throws IOException {
		File tmpFolder = Files.createTempDirectory(getClass().getName()).toFile();
		File tmpFile = new File(tmpFolder, "sample.war");
		return runTestActionHandlingSelect(tmpFile, responseData);
	}
	public WorkflowResponse runTestActionHandlingSelect(File tmpFile, String responseData) throws IOException {
		List<DeployableState> states = new ArrayList<>();
		DeployableState ds = createState("server1", "wonka", 
				"dumbLabel", tmpFile.getAbsolutePath(), null);
		states.add(ds);
		
		TestableShowInBrowserActionHandler testable = new TestableShowInBrowserActionHandler(
				"http://127.0.0.1:8080", states);
		assertActionDetails(testable, 2, new String[] { 
				ShowInBrowserActionHandler.ACTION_SHOW_IN_BROWSER_SELECT_SERVER_ROOT,
				tmpFile.getAbsolutePath()
		});
		
		ServerActionRequest req = new ServerActionRequest();
		req.setActionId(ShowInBrowserActionHandler.ACTION_SHOW_IN_BROWSER_ID);
		req.setServerId("server1");
		Map<String, Object> data = new HashMap<>();
		data.put(ShowInBrowserActionHandler.ACTION_SHOW_IN_BROWSER_SELECTED_PROMPT_ID, 
				responseData);
		req.setData(data);
		
		WorkflowResponse workflowResponse = testable.handle(req);
		return workflowResponse;
	}

	private DeployableState createState(String serverId, String serverTypeId,
			String deployLabel, String deployPath,
			Map<String,Object> options) {
		DeployableState ds = new DeployableState();
		ds.setPublishState(0);
		ds.setState(0);
		ServerHandle sh = new ServerHandle();
		sh.setId(serverId);
		ServerType st = new ServerType();
		st.setId(serverTypeId);
		st.setVisibleName("SomeName");
		sh.setType(st);
		ds.setServer(sh);
		
		DeployableReference ref = new DeployableReference();
		ref.setLabel(deployLabel);
		ref.setPath(deployPath);
		ref.setOptions(options);
		ds.setReference(ref);
		
		return ds;
	}
	
	private void assertActionDetails(TestableShowInBrowserActionHandler testable, int numResponses, String[] options) {
		ServerActionWorkflow workflow = testable.getInitialWorkflowInternal();
		assertNotNull(workflow);
		assertNotNull(workflow.getActionWorkflow());
		assertNotNull(workflow.getActionId());
		assertNotNull(workflow.getActionLabel());
		assertNotNull(workflow.getActionWorkflow().getStatus());
		assertEquals(workflow.getActionWorkflow().getStatus().getSeverity(), 
				IStatus.INFO);
		assertNotNull(workflow.getActionWorkflow().getItems());
		assertTrue(workflow.getActionWorkflow().getItems().size() == 1);
		
		WorkflowResponseItem item1 = workflow.getActionWorkflow().getItems().get(0);
		assertNotNull(item1.getId());
		assertNotNull(item1.getItemType());
		assertNotNull(item1.getLabel());
		assertNull(item1.getProperties());

		assertNotNull(item1.getPrompt());
		assertNotNull(item1.getPrompt().getResponseType());
		assertNotNull(item1.getPrompt().getValidResponses());
		assertTrue(item1.getPrompt().getValidResponses().size() > 0);
		assertEquals(item1.getPrompt().getValidResponses().size(), numResponses);
		
		for( int i = 0; i < numResponses; i++ ) {
			String resp1 = item1.getPrompt().getValidResponses().get(i);
			assertEquals(resp1, options[i]);
		}
	}
	
	
	
	private class TestableShowInBrowserActionHandler extends ShowInBrowserActionHandler {

		private String baseUrl;
		private List<DeployableState> states;

		public TestableShowInBrowserActionHandler(String baseUrl, List<DeployableState> states) {
			super(null);
			this.baseUrl = baseUrl;
			this.states = states;
		}

		protected String getBaseUrl() {
			return baseUrl;
		}
		
		protected List<DeployableState> getDeployableStates() {
			return states;
		}
		public ServerActionWorkflow getInitialWorkflowInternal() {
			return super.getInitialWorkflowInternal();
		}
	}
}
