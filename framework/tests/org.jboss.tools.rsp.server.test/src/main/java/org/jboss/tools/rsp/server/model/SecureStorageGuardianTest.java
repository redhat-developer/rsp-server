/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;
import org.junit.Test;

public class SecureStorageGuardianTest {

	@Test
	public void testSecureStorageGuardian() throws IOException {
		RSPClient client = mock(RSPClient.class);
		when(client.promptString(any())).thenReturn(CompletableFuture.completedFuture("abcde"));
		ServerManagementModel model = createServerManagementModel();
		testPromptableGuardian(model, client, true);
	}

	@Test
	public void testGuardianSecondClientWrongPassword() throws IOException {
		RSPClient client = mock(RSPClient.class);
		when(client.promptString(any())).thenReturn(CompletableFuture.completedFuture("abcde"));
		ServerManagementModel model = createServerManagementModel();
		testPromptableGuardian(model, client, true);

		// Now test a second client with the wrong password
		RSPClient client2 = mock(RSPClient.class);
		when(client2.promptString(any())).thenReturn(CompletableFuture.completedFuture("defgh"));
		testPromptableGuardian(model, client2, false);
	}

	private void testPromptableGuardian(ServerManagementModel model, RSPClient client, boolean validPassword) {

		ISecureStorageProvider provider = model.getSecureStorageProvider();
		ClientThreadLocal.setActiveClient(null);
		assertNull(provider.getSecureStorage());
		ClientThreadLocal.setActiveClient(client);
		assertNull(provider.getSecureStorage());

		ClientCapabilitiesRequest notPromptable = new ClientCapabilitiesRequest();
		model.getCapabilityManagement().registerClientCapabilities(client, notPromptable);
		assertNull(provider.getSecureStorage());
		assertNull(provider.getSecureStorage(true));
		
		// simulate a reconnect
		model.removeClient(client);
		model.clientAdded(client);

		// Register the client with the ability to be prompted... but...
		// getSecureStorage will still fail since it won't prompt the user with this api
		Map<String, String> promptable = new HashMap<String, String>();
		promptable.put(ICapabilityKeys.STRING_PROTOCOL_VERSION, ICapabilityKeys.PROTOCOL_VERSION_0_10_0);
		promptable.put(ICapabilityKeys.BOOLEAN_STRING_PROMPT, "true");
		ClientCapabilitiesRequest promptable2 = new ClientCapabilitiesRequest(promptable);
		model.getCapabilityManagement().registerClientCapabilities(client, promptable2);
		assertNull(provider.getSecureStorage());

		if (validPassword) {
			// Now try again with the signature that WILL prompt.
			assertNotNull(provider.getSecureStorage(true));

			// Now that they've been authenticated, use the API that won't prompt to make
			// sure it works
			assertNotNull(provider.getSecureStorage());
		} else {
			assertNull(provider.getSecureStorage(true));
			assertNull(provider.getSecureStorage());
		}
	}

	private ServerManagementModel createServerManagementModel() throws IOException {
		File location = Files.createTempDirectory("ServerManagementModelTest").toFile();
		return new ServerManagementModel(location) {};
	}

}
