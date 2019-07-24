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
import org.jboss.tools.rsp.server.wildfly.servertype.actions.EditServerConfigurationActionHandler;
import org.jboss.tools.rsp.server.wildfly.servertype.actions.ShowInBrowserActionHandler;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;
import org.junit.Test;

public class EditServerConfigActionTest {

	@Test
	public void testActionInitialWorkflowNoDeployments() throws IOException {
		File configFile = Files.createTempFile("EditServerConfigActionTest", System.currentTimeMillis() + ".txt").toFile();
		TestableEditServerConfigurationActionHandler handler = 
				new TestableEditServerConfigurationActionHandler(configFile);
		ServerActionWorkflow workflow = handler.getInitialWorkflowInternal();
		assertEquals(workflow.getActionId(), handler.ACTION_ID);
		assertEquals(workflow.getActionLabel(), handler.ACTION_LABEL);
		
		WorkflowResponse resp = workflow.getActionWorkflow();
		assertNotNull(resp);
		
		assertNotNull(resp.getStatus());
		assertEquals(resp.getStatus().getSeverity(), IStatus.OK);

		assertNotNull(resp.getItems());
		assertEquals(resp.getItems().size(), 1);
		WorkflowResponseItem item1 = resp.getItems().get(0);
		
		assertEquals(item1.getLabel(), handler.ACTION_LABEL);
		assertEquals(item1.getId(), handler.ACTION_ID);
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
		protected String getConfigurationFile() {
			return configFile.getAbsolutePath();
		}
		public ServerActionWorkflow getInitialWorkflowInternal() {
			return super.getInitialWorkflowInternal();
		}
	}
}
