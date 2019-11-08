/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.server.persistence.DataLocationCore;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.junit.Before;
import org.junit.Test;

public class ServerManagementModelTest {

	private IServerManagementModel model;

	@Before
	public void before() {
		this.model = new ServerManagementModel(new DataLocationCore("27511"));
	}
	
	@Test
	public void testDependentModelsNotNull() {
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
