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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.server.wildfly.servertype.actions.EditServerConfigurationActionHandler;
import org.junit.Test;

public class EditServerConfigActionTest {
	private String ACTION_ID = EditServerConfigurationActionHandler.ACTION_ID;
	private String ACTION_LABEL = EditServerConfigurationActionHandler.ACTION_LABEL;
	
	@Test
	public void testActionInitialWorkflowNoDeployments() throws IOException {
		File configFile = Files.createTempFile("EditServerConfigActionTest", System.currentTimeMillis() + ".txt").toFile();
		TestableEditServerConfigurationActionHandler handler = 
				new TestableEditServerConfigurationActionHandler(configFile);
		ServerActionWorkflow workflow = handler.getInitialWorkflowInternal();
		assertEquals(workflow.getActionId(), ACTION_ID);
		assertEquals(workflow.getActionLabel(), ACTION_LABEL);
		
		WorkflowResponse resp = workflow.getActionWorkflow();
		assertNotNull(resp);
		
		assertNotNull(resp.getStatus());
		assertEquals(resp.getStatus().getSeverity(), IStatus.OK);

		assertNotNull(resp.getItems());
		assertEquals(resp.getItems().size(), 1);
		WorkflowResponseItem item1 = resp.getItems().get(0);
		
		assertEquals(item1.getLabel(), ACTION_LABEL);
		assertEquals(item1.getId(), ACTION_ID);
		assertEquals(item1.getItemType(), ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_EDITOR);
		assertNull(item1.getPrompt());
		assertNotNull(item1.getProperties());
		assertEquals(item1.getProperties().size(), 1);
		assertEquals(item1.getProperties().get(
				ServerManagementAPIConstants.WORKFLOW_EDITOR_PROPERTY_PATH), configFile.getAbsolutePath());
	}
	
	private static class TestableEditServerConfigurationActionHandler extends EditServerConfigurationActionHandler {
		private File configFile;
		public TestableEditServerConfigurationActionHandler(File configFile) {
			super(null);
			this.configFile = configFile;
		}
		@Override
		protected String getConfigurationFile() {
			return configFile.getAbsolutePath();
		}
		@Override
		public ServerActionWorkflow getInitialWorkflowInternal() {
			return super.getInitialWorkflowInternal();
		}
	}
}
