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
import static org.mockito.Mockito.mock;

import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.RSPClient;
import org.junit.Test;

public class ServerManagementModelTest {

	@Test
	public void testDependentModelsNotNull() {
		ServerManagementModel model = new ServerManagementModel();
		assertNotNull(model);
		assertNotNull(model.getCapabilityManagement());
		assertNotNull(model.getDiscoveryPathModel());
		assertNotNull(model.getServerBeanTypeManager());
		assertNotNull(model.getServerModel());
		assertNotNull(model.getSecureStorageProvider());
		assertNotNull(model.getVMInstallModel());
	}

	@Test
	public void testClientAdded() {
		ServerManagementModel model = new ServerManagementModel();
		RSPClient client = mock(RSPClient.class);
		model.clientAdded(client);

		// Test default capabilities
		String version = model.getCapabilityManagement().getCapabilityProperty(client,
				ICapabilityKeys.STRING_PROTOCOL_VERSION);
		assertNotNull(version);
		assertEquals(ICapabilityKeys.PROTOCOL_VERSION_0_9_0, version);
		String prompt = model.getCapabilityManagement().getCapabilityProperty(client,
				ICapabilityKeys.BOOLEAN_STRING_PROMPT);
		assertNotNull(prompt);
		assertEquals("false", prompt);
	}

}
