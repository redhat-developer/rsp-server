/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.tools.rsp.api.dao.GetServerJsonResponse;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.UpdateServerRequest;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.itests.RSPCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Test class covering editing server API function calls.
 * @author odockal
 *
 */
public class WildflyEditServerTest extends RSPCase {

	private static final String SERVER_ID = "wildfly";
	private ServerHandle handle;
	
	private static final String SERVER_CONFIG_HOME = "server.home.dir";

	private static final String SERVER_CONFIG_ID = "id";
	private static final String SERVER_HANDLE_NULL = "Server handle cannot be null";
	private static final String SERVER_TYPE_NULL = "Update server request's server type cannot be null";
	private static final String SERVER_TYPE_UNKNOWN = "Update server request contains unknown server type";
	private static final String NULL_STRING = "Update Failed: Error while reading server string: null";
	
	@Before
	public void before() throws Exception {
		createServer(WILDFLY_ROOT, SERVER_ID);
		handle = new ServerHandle(SERVER_ID, wildflyType);
	}

	@After
	public void after() throws Exception {
		deleteServer(SERVER_ID);
	}

	@Test
	public void testGetServerAsJson() throws InterruptedException, ExecutionException, TimeoutException {
		GetServerJsonResponse response = serverProxy.getServerAsJson(handle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.OK, response.getStatus().getSeverity());
		assertEquals(handle, response.getServerHandle());
		assertNotNull(response.getServerJson());
		/*
		{
  "server.home.dir": "/home/odockal/Apps/Servers/wildfly-17.0.0.Final",
  "id-set": "true",
  "org.jboss.tools.rsp.server.typeId": "org.jboss.ide.eclipse.as.wildfly.170",
  "id": "wildfly",
  "wildfly.publish.restart.pattern": "\\.class$|\\.jar$",
  "args.override.boolean": "false"
}
		 */
		JsonObject json = new JsonParser().parse(response.getServerJson()).getAsJsonObject();
		assertFalse(json.get("server.home.dir").getAsString().isEmpty());
		assertFalse(json.get("id-set").getAsString().isEmpty());
		assertEquals(WILDFLY_SERVER_ID, json.get("org.jboss.tools.rsp.server.typeId").getAsString());
		assertEquals(SERVER_ID, json.get("id").getAsString());
		assertFalse(json.get("wildfly.publish.restart.pattern").getAsString().isEmpty());
		assertFalse(json.get("args.override.boolean").getAsString().isEmpty());
	}
	
	@Test
	public void testNullGetServerAsJson() throws InterruptedException, ExecutionException, TimeoutException {
		GetServerJsonResponse response = serverProxy.getServerAsJson(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		verifyInvalidGetServerAsJsonRequest(response, MISSING_SERVER_HANDLE);
	}
	
	@Test
	public void testInvalidGetServerAsJson() throws InterruptedException, ExecutionException, TimeoutException {
		ServerHandle inHandle = new ServerHandle("foo.server.id",
				new ServerType("some.id", "my.server", "Random server type definition"));
		GetServerJsonResponse response = serverProxy.getServerAsJson(inHandle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		verifyInvalidGetServerAsJsonRequest(response, SERVER_TYPE_NOT_FOUND);
		inHandle = new ServerHandle("foo.server.id", null);
		response = serverProxy.getServerAsJson(inHandle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		verifyInvalidGetServerAsJsonRequest(response, MISSING_SERVER_TYPE);
		inHandle = new ServerHandle("foo.server.id", new ServerType(null, "my.server", "Random server type definition"));
		response = serverProxy.getServerAsJson(inHandle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		verifyInvalidGetServerAsJsonRequest(response, MISSING_SERVER_TYPE_ID);
	}
	
	@Test
	public void testUpdateServer() throws InterruptedException, ExecutionException, TimeoutException {
		GetServerJsonResponse jsonResponse = serverProxy.getServerAsJson(handle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		String formerString = jsonResponse.getServerJson();
		JsonObject json = new JsonParser().parse(jsonResponse.getServerJson()).getAsJsonObject();
		UpdateServerRequest request = new UpdateServerRequest();
		request.setHandle(handle);
		
		// add new value into server config
		request.setServerJson(parseJsonAndAddObject(formerString, "custom.property", "stringValue"));
		UpdateServerResponse response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.OK, response.getValidation().getStatus().getSeverity());
		jsonResponse = serverProxy.getServerAsJson(handle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		json = new JsonParser().parse(jsonResponse.getServerJson()).getAsJsonObject();
		assertEquals("stringValue", json.get("custom.property").getAsString());
		
		// remove field from server config
		request.setServerJson(parseJsonAndRemoveObject(formerString, "custom.property"));
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.OK, response.getValidation().getStatus().getSeverity());
		jsonResponse = serverProxy.getServerAsJson(handle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		String newconfig = jsonResponse.getServerJson();
		assertFalse(newconfig.contains("custom.property"));
		
		// update field from server config
		request.setServerJson(parseJsonAndAddObject(formerString, "args.override.boolean", "true"));
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.OK, response.getValidation().getStatus().getSeverity());
		jsonResponse = serverProxy.getServerAsJson(handle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		json = new JsonParser().parse(jsonResponse.getServerJson()).getAsJsonObject();
		assertEquals(true, json.get("args.override.boolean").getAsBoolean());
	}
	
	@Test
	public void testUpdateServerNullRequest() throws InterruptedException, ExecutionException, TimeoutException {
		GetServerJsonResponse jsonResponse = serverProxy.getServerAsJson(handle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		String formerString = jsonResponse.getServerJson();
		// null request
		UpdateServerResponse response = serverProxy.updateServer(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals("Update server request cannot be null", response.getValidation().getStatus().getMessage());
		assertNull(response.getHandle());
		// null handle in request
		UpdateServerRequest request = new UpdateServerRequest();
		request.setHandle(null);
		request.setServerJson(formerString);
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals(SERVER_HANDLE_NULL, response.getValidation().getStatus().getMessage());
		assertNull(response.getHandle());
		// Null in handle - null id
		ServerHandle inHandle = new ServerHandle(null, new ServerType("some.id", "my.server", "Random server type definition"));
		request.setHandle(inHandle);
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertTrue(response.getValidation().getStatus().getMessage().contains("not found in model"));
		
		// null in handle - null server type
		inHandle = new ServerHandle(SERVER_ID, null);
		request.setHandle(inHandle);
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals(SERVER_TYPE_NULL, response.getValidation().getStatus().getMessage());
		
		// null json string in request
		request.setHandle(handle);
		request.setServerJson(null);
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals(NULL_STRING, response.getValidation().getStatus().getMessage());
		assertNull(response.getHandle());
	}
	
	@Test
	public void testUpdateServerInvalidRequest() throws InterruptedException, ExecutionException, TimeoutException {
		GetServerJsonResponse jsonResponse = serverProxy.getServerAsJson(handle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		String formerString = jsonResponse.getServerJson();
		UpdateServerRequest request = new UpdateServerRequest();
		// test invalid handle - non existing id
		ServerHandle inHandle = new ServerHandle("foo.server.id",
				new ServerType("some.id", "my.server", "Random server type definition"));
		request.setHandle(inHandle);
		request.setServerJson(formerString);
		UpdateServerResponse response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertTrue(response.getValidation().getStatus().getMessage().contains("not found in model"));
		
		// test invalid server type in handle
		inHandle = new ServerHandle(SERVER_ID,
				new ServerType("some.id", "my.server", "Random server type definition"));
		request.setHandle(inHandle);
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals(SERVER_TYPE_UNKNOWN, response.getValidation().getStatus().getMessage());
	}
		
	@Test
	public void testUpdateServerRequestValidation() throws InterruptedException, ExecutionException, TimeoutException {
		// get former config as a json
		GetServerJsonResponse jsonResponse = serverProxy.getServerAsJson(handle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		String formerString = jsonResponse.getServerJson();
		JsonObject json = new JsonParser().parse(jsonResponse.getServerJson()).getAsJsonObject();
		UpdateServerRequest request = new UpdateServerRequest();
		// test changing unchangeable fields:
		// id
		String target = json.get(SERVER_CONFIG_ID).getAsString();
		String replacement = formerString.replace("\"" + target + "\"", "\"" + target + "NEW\"");
		request.setServerJson(replacement);
		request.setHandle(handle);
		UpdateServerResponse response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals("Field " + SERVER_CONFIG_ID + " may not be changed", response.getValidation().getStatus().getMessage());
		// id-set
		target = json.get("id-set").getAsString();
		replacement = formerString.replace("\"" + target + "\"", "\"" + target + "NEW\"");
		request.setServerJson(replacement);
		request.setHandle(handle);
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals("Field id-set may not be changed", response.getValidation().getStatus().getMessage());
		// org.jboss.tools.rsp.server.typeId
		target = json.get("org.jboss.tools.rsp.server.typeId").getAsString();
		replacement = formerString.replace("\"" + target + "\"", "\"org.jboss.ide.eclipse.as.wildfly.80\"");
		request.setServerJson(replacement);
		request.setHandle(handle);
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals("Field org.jboss.tools.rsp.server.typeId may not be changed", response.getValidation().getStatus().getMessage());
		// unknown server type
		target = json.get("org.jboss.tools.rsp.server.typeId").getAsString();
		replacement = formerString.replace("\"" + target + "\"", "\"org.jboss.tools.random.server.55\"");
		request.setServerJson(replacement);
		request.setHandle(handle);
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals("Update Failed: null", response.getValidation().getStatus().getMessage());

		// send invalid json, values, etc. mainly for server.home.dir
		request.setServerJson(parseJsonAndAddObject(formerString, SERVER_CONFIG_HOME, System.getProperty("user.dir") + File.pathSeparator + "randomDir123"));
		request.setHandle(handle);
		response = serverProxy.updateServer(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, response.getValidation().getStatus().getSeverity());
		assertEquals("Field " + SERVER_CONFIG_HOME + " may not be changed", response.getValidation().getStatus().getMessage());
	}
	
	private static void verifyInvalidGetServerAsJsonRequest(GetServerJsonResponse response, String message) {
		assertEquals(Status.ERROR, response.getStatus().getSeverity());
		assertEquals(message, response.getStatus().getMessage());
		assertNull(response.getServerHandle());
		assertNull(response.getServerJson());
	}
	
	private static String parseJsonAndAddObject(String jsonAsString, String field, String newValue) {
		JsonObject json = new JsonParser().parse(jsonAsString).getAsJsonObject();
		json.addProperty(field, newValue);
		return json.toString();
	}
	
	private static String parseJsonAndRemoveObject(String jsonAsString, String field) {
		JsonObject json = new JsonParser().parse(jsonAsString).getAsJsonObject();
		json.remove(field);
		return json.toString();
	}
}
