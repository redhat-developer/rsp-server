/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import static org.jboss.tools.rsp.itests.util.ServerStateUtil.waitForServerState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.itests.RSPCase;
import org.jboss.tools.rsp.itests.util.DummyClient;
import org.junit.Test;


/**
 *
 * @author jrichter
 */
public class WildFlyLaunchingTest extends RSPCase {

	private final DummyClient client = launcher.getClient();

	@Test
    public void testGetLaunchModes() throws Exception {
        List<ServerLaunchMode> modes = serverProxy.getLaunchModes(wildflyType).get();
        
        ServerLaunchMode expectedRun = new ServerLaunchMode(MODE_RUN, "A launch mode indicating a simple run.");
        ServerLaunchMode expectedDebug = new ServerLaunchMode(MODE_DEBUG,
            "A launch mode indicating a debug launch, which can add the appropriate debugging flags or system properties as required.");
        
        assertTrue(modes.contains(expectedRun));
        assertTrue(modes.contains(expectedDebug));
    }

    @Test
    public void testGetLaunchModesInvalid() throws Exception {
        List<ServerLaunchMode> modes = serverProxy.getLaunchModes(new ServerType("foo", "foo", "foo")).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNull(modes);
    }

    @Test
    public void testGetLaunchModesNull() throws Exception {
        List<ServerLaunchMode> modes = serverProxy.getLaunchModes(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNull(modes);
    }

    @Test
    public void testGetRequiredLaunchAttributes() throws Exception {
        LaunchAttributesRequest req = new LaunchAttributesRequest(wildflyType.getId(), MODE_RUN);
        Attributes attr = serverProxy.getRequiredLaunchAttributes(req).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNotNull(attr);
    }

    @Test
    public void testGetRequiredLaunchAttributesInvalid() throws Exception {
        LaunchAttributesRequest req = new LaunchAttributesRequest("foo", MODE_RUN);
        Attributes attr = serverProxy.getRequiredLaunchAttributes(req).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNull(attr);
    }

    @Test
    public void testGetRequiredLaunchAttributesNull() throws Exception {
        Attributes attr = serverProxy.getRequiredLaunchAttributes(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNull(attr);
    }

    @Test
    public void testGetOptionalLaunchAttributes() throws Exception {
        LaunchAttributesRequest req = new LaunchAttributesRequest(wildflyType.getId(), MODE_RUN);
        Attributes attr = serverProxy.getOptionalLaunchAttributes(req).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNotNull(attr);
    }

    @Test
    public void testGetOptionalLaunchAttributesInvalid() throws Exception {
        LaunchAttributesRequest req = new LaunchAttributesRequest("foo", MODE_RUN);
        Attributes attr = serverProxy.getOptionalLaunchAttributes(req).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNull(attr);
    }

    @Test
    public void testGetOptionalLaunchAttributesNull() throws Exception {
        Attributes attr = serverProxy.getOptionalLaunchAttributes(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNull(attr);
    }

    @Test
    public void testGetLaunchCommand() throws Exception {
        createServer(WILDFLY_ROOT, "wildfly");
        
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wildflyType.getId(), "wildfly", new HashMap<>()), MODE_RUN);
        
        CommandLineDetails cmd = serverProxy.getLaunchCommand(params).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertFalse(cmd.getWorkingDir().isEmpty());
        assertTrue(cmd.getCmdLine().length > 0);
    }

    @Test
    public void testGetLaunchCommandInvalid() throws Exception {
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes("foo", "bar", new HashMap<>()), MODE_RUN);
        
        CommandLineDetails cmd = serverProxy.getLaunchCommand(params).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNull(cmd);
    }

    @Test
    public void testGetLaunchCommandNull() throws Exception {
        CommandLineDetails cmd = serverProxy.getLaunchCommand(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertNull(cmd);
    }

    @Test
    public void testServerStartingByClient() throws Exception {
        createServer(WILDFLY_ROOT, "wildfly1");
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wildflyType.getId(), "wildfly1", new HashMap<>()), MODE_RUN);
        ServerStartingAttributes attr = new ServerStartingAttributes(params, false);
        
        Status status = serverProxy.serverStartingByClient(attr).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(0, status.getSeverity());
        assertEquals(STATUS_MESSAGE_OK, status.getMessage());
    }

    @Test
    public void testServerStartingByClientInvalid() throws Exception {
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes("foo", "bar", new HashMap<>()), MODE_RUN);
        ServerStartingAttributes attr = new ServerStartingAttributes(params, false);
        
        Status status = serverProxy.serverStartingByClient(attr).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals("Server bar does not exist", status.getMessage());
    }

    @Test
    public void testServerStartingByClientNull() throws Exception {
        Status status = serverProxy.serverStartingByClient(null).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }

    @Test
    public void testServerStartedByClient() throws Exception {
        createServer(WILDFLY_ROOT, "wildfly2");
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wildflyType.getId(), "wildfly2", new HashMap<>()), MODE_RUN);
        
        Status status = serverProxy.serverStartedByClient(params).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(0, status.getSeverity());
        assertEquals(STATUS_MESSAGE_OK, status.getMessage());
    }

    @Test
    public void testServerStartedByClientInvalid() throws Exception {
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes("foo", "bar", new HashMap<>()), MODE_RUN);
        
        Status status = serverProxy.serverStartedByClient(params).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals("Server bar does not exist", status.getMessage());
    }

    @Test
    public void testServerStartedByClientNull() throws Exception {
        Status status = serverProxy.serverStartedByClient(null).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }

    @Test
    public void testStartServerInvalid() throws Exception {
        LaunchParameters params = new LaunchParameters(new ServerAttributes("foo", "foo", new HashMap<>()), MODE_RUN);
        StartServerResponse response = serverProxy.startServerAsync(params).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, response.getStatus().getSeverity());
        assertEquals("Server foo does not exist", response.getStatus().getMessage());
    }
    
    @Test
    public void testStartServerNull() throws Exception {
        StartServerResponse response = serverProxy.startServerAsync(null).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, response.getStatus().getSeverity());
        assertEquals("Invalid Parameter", response.getStatus().getMessage());
    }

    @Test
    public void testStartServer() throws Exception {
        createServer(WILDFLY_ROOT, "wildfly3");
        Map<String, Object> attr = new HashMap<>();
        attr.put("server.home.dir", WILDFLY_ROOT);
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wildflyType.getId(), "wildfly3", attr), MODE_RUN);
        
        StartServerResponse response = serverProxy.startServerAsync(params).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(0, response.getStatus().getSeverity());
        assertEquals(STATUS_MESSAGE_OK, response.getStatus().getMessage());
        waitForServerState(ServerManagementAPIConstants.STATE_STARTED, 10, client);
        
        serverProxy.stopServerAsync(new StopServerAttributes("wildfly3", true)).get();
        waitForServerState(ServerManagementAPIConstants.STATE_STOPPED, 10, client);
    }

    @Test
    public void testStartServerTwice() throws Exception {
        createServer(WILDFLY_ROOT, "wildfly4");

        Map<String, Object> attr = new HashMap<>();
        attr.put("server.home.dir", WILDFLY_ROOT);
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wildflyType.getId(), "wildfly4", attr), MODE_RUN);
        
        serverProxy.startServerAsync(params).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        waitForServerState(ServerManagementAPIConstants.STATE_STARTED, 10, client);
        StartServerResponse response = serverProxy.startServerAsync(params).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.CANCEL, response.getStatus().getSeverity());
        
        serverProxy.stopServerAsync(new StopServerAttributes("wildfly4", true)).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        waitForServerState(ServerManagementAPIConstants.STATE_STOPPED, 10, client);
    }

    @Test
    public void testStopServer() throws Exception {
        Status status = createServer(WILDFLY_ROOT, "wildfly5");
        assertEquals(Status.OK, status.getSeverity());
        Map<String, Object> attr = new HashMap<>();
        attr.put("server.home.dir", WILDFLY_ROOT);
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wildflyType.getId(), "wildfly5", attr), MODE_RUN);
        
        serverProxy.startServerAsync(params).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        waitForServerState(ServerManagementAPIConstants.STATE_STARTED, 10, client);
        
        status = serverProxy.stopServerAsync(new StopServerAttributes("wildfly5", false)).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        waitForServerState(ServerManagementAPIConstants.STATE_STOPPED, 10, client);
        
        assertEquals(Status.OK, status.getSeverity());
        assertEquals(STATUS_MESSAGE_OK, status.getMessage());
    }

    @Test
    public void testStopStoppedServer() throws Exception {
        createServer(WILDFLY_ROOT, "wildfly6");
        Status status = serverProxy.stopServerAsync(new StopServerAttributes("wildfly6", false)).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals("The server is already marked as stopped. "
        		+ "If you wish to force a stop request, please set the force flag to true.",
        		status.getMessage());
        assertEquals(Status.ERROR, status.getSeverity());
    }

    @Test
    public void testStopNonexistingServer() throws Exception {
        Status status = serverProxy.stopServerAsync(new StopServerAttributes("wildfly7", false)).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals("Server wildfly7 does not exist", status.getMessage());
        assertEquals(Status.ERROR, status.getSeverity());
    }
    
    @Test
    public void testStopServerNull() throws Exception {
        Status status = serverProxy.stopServerAsync(null).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(INVALID_PARAM, status.getMessage());
        assertEquals(Status.ERROR, status.getSeverity());
    }
    
}
