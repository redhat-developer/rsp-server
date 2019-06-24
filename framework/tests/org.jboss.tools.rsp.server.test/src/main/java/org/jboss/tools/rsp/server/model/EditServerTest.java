/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.GetServerJsonResponse;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.UpdateServerRequest;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.server.ServerManagementServerImpl;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.util.TestServerUtils;
import org.junit.Before;
import org.junit.Test;

public class EditServerTest {

	@Before
	public void before() throws IOException {		
	}
	
	@Test
	public void testStuff() throws IOException {
		final Path serversDir = Files.createTempDirectory("servers");
		IServerManagementModel mgmtModel = new ServerManagementModel(serversDir.toFile());
		ServerManagementServerImpl impl = new ServerManagementServerImpl(null, mgmtModel);
		
		final String REQUIRED_PROP = "wonka.prop1";
		final String REQUIRED_PROP_DEF = "wonka.prop1.default";
		final String SERVER_TYPE = "wonka.1";
		final String SERVER_NAME = "wonka.name";
		
		
		// Our server type wonka.1 requires a string prop wonka.prop1
		CreateServerAttributesUtility util = new CreateServerAttributesUtility();
		util.addAttribute(REQUIRED_PROP, ServerManagementAPIConstants.ATTR_TYPE_STRING, 
				"desc", REQUIRED_PROP_DEF);
		IServerType sType = TestServerUtils.createServerType(SERVER_TYPE, 
				EditServerServerDelegate::new, util.toPojo());
		mgmtModel.getServerModel().addServerType(sType);
		
		
		// Let's first try to create it without this required prop
		Map<String, Object> att = new HashMap<>();
		ServerAttributes attrs = new ServerAttributes(SERVER_TYPE, SERVER_NAME, att);
		CreateServerResponse resp = null;
		try {
			resp = impl.createServer(attrs).get();
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertFalse(resp.getStatus().isOK());
		
		// Ok that failed. Let's try with it. 
		// Let's first try to create it without this required prop
		att = new HashMap<>();
		att.put("wonka.prop1", "wonkaval");
		attrs = new ServerAttributes("wonka.1", "wonka.name", att);
		resp = null;
		try {
			resp = impl.createServer(attrs).get();
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertTrue(resp.getStatus().isOK());
		
		ServerHandle[] handles = mgmtModel.getServerModel().getServerHandles();
		assertNotNull(handles);
		assertFalse(handles.length == 0);
		assertTrue(handles.length == 1);
		ServerHandle desired = handles[0];
		
		// Now let's get the created server as json
		GetServerJsonResponse jsonResp = null;
		try {
			jsonResp = impl.getServerAsJson(desired).get();
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(jsonResp);
		assertTrue(jsonResp.getStatus().isOK());
		
		String[] testUpdates = new String[] {
				changedName(), changedType(), withoutProp(), changedPropertyValue()
		};
		boolean[] expectedResult = new boolean[] {
				false, false, false, true
		};
		
		for( int i = 0; i < testUpdates.length; i++ ) {
			UpdateServerRequest req = new UpdateServerRequest();
			req.setHandle(desired);
			req.setServerJson(testUpdates[i]);
			UpdateServerResponse updateResp = null;
			try {
				updateResp = impl.updateServer(req).get();
			} catch(Exception e) {
				e.printStackTrace();
				fail();
			}
			assertNotNull(updateResp);
			assertNotNull(updateResp.getValidation());
			assertNotNull(updateResp.getValidation().getStatus());
			assertEquals(updateResp.getValidation().getStatus().isOK(), expectedResult[i]);
		}		
		
	}

	private String changedName() {
		return "{\n" + 
				"  \"id-set\": \"true\",\n" + 
				"  \"org.jboss.tools.rsp.server.typeId\": \"wonka.1\",\n" + 
				"  \"id\": \"wonka.name2\",\n" + 
				"  \"wonka.prop1\": \"wonkaval\"\n" + 
				"}";
	}

	private String changedType() {
		return "{\n" + 
				"  \"id-set\": \"true\",\n" + 
				"  \"org.jboss.tools.rsp.server.typeId\": \"wonka.2\",\n" + 
				"  \"id\": \"wonka.name\",\n" + 
				"  \"wonka.prop1\": \"wonkaval\"\n" + 
				"}";
	}

	private String withoutProp() {
		return "{\n" + 
				"  \"id-set\": \"true\",\n" + 
				"  \"org.jboss.tools.rsp.server.typeId\": \"wonka.1\",\n" + 
				"  \"id\": \"wonka.name\"\n" + 
				"}";
	}
	

	private String changedPropertyValue() {
		return "{\n" + 
				"  \"id-set\": \"true\",\n" + 
				"  \"org.jboss.tools.rsp.server.typeId\": \"wonka.1\",\n" + 
				"  \"id\": \"wonka.name\",\n" + 
				"  \"wonka.prop1\": \"wonkaval_55\"\n" + 
				"}";
	}
	
	public class EditServerServerDelegate extends AbstractServerDelegate {
	
		public EditServerServerDelegate(IServer server) {
			super(server);
		}

	}
}

