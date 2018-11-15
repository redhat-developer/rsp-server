/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jboss.tools.rsp.itests.RSPTestCase;
import org.jboss.tools.rsp.itests.util.TestClient;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author jrichter
 */
public class WildFlyLaunchingTest extends RSPTestCase {
    
    private final TestClient client = launcher.getClient();
    
    @Test
    public void testGetLaunchModes() throws Exception {
        List<ServerLaunchMode> modes = serverProxy.getLaunchModes(wfly13Type).get();
        
        ServerLaunchMode expectedRun = new ServerLaunchMode("run", "A launch mode indicating a simple run.");
        ServerLaunchMode expectedDebug = new ServerLaunchMode("debug",
            "A launch mode indicating a debug launch, which can add the appropriate debugging flags or system properties as required.");
        
        assertTrue(modes.contains(expectedRun));
        assertTrue(modes.contains(expectedDebug));
    }
    
    @Test
    public void testGetLaunchModesInvalid() throws Exception {
        List<ServerLaunchMode> modes = serverProxy.getLaunchModes(new ServerType("foo", "foo", "foo")).get();
        
        assertNull(modes);
    }
    
    @Test
    public void testGetLaunchModesNull() throws Exception {
        List<ServerLaunchMode> modes = serverProxy.getLaunchModes(null).get();
        
        assertNull(modes);
    }
    
    @Test
    public void testGetRequiredLaunchAttributes() throws Exception {
        LaunchAttributesRequest req = new LaunchAttributesRequest(wfly13Type.getId(), "run");
        Attributes attr = serverProxy.getRequiredLaunchAttributes(req).get();
        
        assertNotNull(attr);
    }
    
    @Test
    public void testGetRequiredLaunchAttributesInvalid() throws Exception {
        LaunchAttributesRequest req = new LaunchAttributesRequest("foo", "run");
        Attributes attr = serverProxy.getRequiredLaunchAttributes(req).get();
        
        assertNull(attr);
    }
    
    @Test
    public void testGetRequiredLaunchAttributesNull() throws Exception {
        Attributes attr = serverProxy.getRequiredLaunchAttributes(null).get();
        
        assertNull(attr);
    }
    
    @Test
    public void testGetOptionalLaunchAttributes() throws Exception {
        LaunchAttributesRequest req = new LaunchAttributesRequest(wfly13Type.getId(), "run");
        Attributes attr = serverProxy.getOptionalLaunchAttributes(req).get();
        
        assertNotNull(attr);
    }
    
    @Test
    public void testGetOptionalLaunchAttributesInvalid() throws Exception {
        LaunchAttributesRequest req = new LaunchAttributesRequest("foo", "run");
        Attributes attr = serverProxy.getOptionalLaunchAttributes(req).get();
        
        assertNull(attr);
    }
    
    @Test
    public void testGetOptionalLaunchAttributesNull() throws Exception {
        Attributes attr = serverProxy.getOptionalLaunchAttributes(null).get();
        
        assertNull(attr);
    }
    
    @Test
    public void testGetLaunchCommand() throws Exception {
        createServer(WILDFLY13_ROOT, "wildfly");
        
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wfly13Type.getId(), "wildfly", new HashMap<>()), "run");
        
        CommandLineDetails cmd = serverProxy.getLaunchCommand(params).get();
        
        assertFalse(cmd.getWorkingDir().isEmpty());
        assertTrue(cmd.getCmdLine().length > 0);
    }
    
    @Test
    public void testGetLaunchCommandInvalid() throws Exception {
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes("foo", "bar", new HashMap<>()), "run");
        
        CommandLineDetails cmd = serverProxy.getLaunchCommand(params).get();
        
        assertNull(cmd);
    }
    
    @Test
    public void testGetLaunchCommandNull() throws Exception {
        CommandLineDetails cmd = serverProxy.getLaunchCommand(null).get();
        
        assertNull(cmd);
    }
    
    @Test
    public void testServerStartingByClient() throws Exception {
        createServer(WILDFLY13_ROOT, "wildfly1");
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wfly13Type.getId(), "wildfly1", new HashMap<>()), "run");
        ServerStartingAttributes attr = new ServerStartingAttributes(params, false);
        
        Status status = serverProxy.serverStartingByClient(attr).get();
        
        assertEquals(0, status.getSeverity());
        assertEquals("ok", status.getMessage());
    }
    
    @Test
    public void testServerStartingByClientInvalid() throws Exception {
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes("foo", "bar", new HashMap<>()), "run");
        ServerStartingAttributes attr = new ServerStartingAttributes(params, false);
        
        Status status = serverProxy.serverStartingByClient(attr).get();
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals("Server bar does not exist", status.getMessage());
    }
    
    @Test
    public void testServerStartingByClientNull() throws Exception {
        Status status = serverProxy.serverStartingByClient(null).get();
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }
    
    @Test
    public void testServerStartedByClient() throws Exception {
        createServer(WILDFLY13_ROOT, "wildfly2");
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wfly13Type.getId(), "wildfly2", new HashMap<>()), "run");
        
        Status status = serverProxy.serverStartedByClient(params).get();
        
        assertEquals(0, status.getSeverity());
        assertEquals("ok", status.getMessage());
    }
    
    @Test
    public void testServerStartedByClientInvalid() throws Exception {
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes("foo", "bar", new HashMap<>()), "run");
        
        Status status = serverProxy.serverStartedByClient(params).get();
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals("Server bar does not exist", status.getMessage());
    }
    
    @Test
    public void testServerStartedByClientNull() throws Exception {
        Status status = serverProxy.serverStartedByClient(null).get();
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }
    
    
    @Test
    public void testStartServerInvalid() throws Exception {
        LaunchParameters params = new LaunchParameters(new ServerAttributes("foo", "foo", new HashMap<>()), "run");
        StartServerResponse response = serverProxy.startServerAsync(params).get();
        
        assertEquals(Status.ERROR, response.getStatus().getSeverity());
        assertEquals("Server foo does not exist", response.getStatus().getMessage());
    }
    
    @Test
    public void testStartServerNull() throws Exception {
        StartServerResponse response = serverProxy.startServerAsync(null).get();
        
        assertEquals(Status.ERROR, response.getStatus().getSeverity());
        assertEquals("Invalid Parameter", response.getStatus().getMessage());
    }

    @Test
    public void testStartServer() throws Exception {
        createServer(WILDFLY13_ROOT, "wildfly3");
        Map<String, Object> attr = new HashMap<>();
        attr.put("server.home.dir", WILDFLY13_ROOT);
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wfly13Type.getId(), "wildfly3", attr), "run");
        
        StartServerResponse response = serverProxy.startServerAsync(params).get();
        
        assertEquals(0, response.getStatus().getSeverity());
        assertEquals("ok", response.getStatus().getMessage());
        waitForServerState("started", 10);
        
        serverProxy.stopServerAsync(new StopServerAttributes("wildfly3", true)).get();
        waitForServerState("stopped", 10);
    }
    
    @Test
    public void testStartServerTwice() throws Exception {
        createServer(WILDFLY13_ROOT, "wildfly4");
        Map<String, Object> attr = new HashMap<>();
        attr.put("server.home.dir", WILDFLY13_ROOT);
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wfly13Type.getId(), "wildfly4", attr), "run");
        
        serverProxy.startServerAsync(params).get();
        waitForServerState("started", 10);
        StartServerResponse response = serverProxy.startServerAsync(params).get();
        
        assertEquals(Status.CANCEL, response.getStatus().getSeverity());
        
        serverProxy.stopServerAsync(new StopServerAttributes("wildfly4", true)).get();
        waitForServerState("stopped", 10);
    }
    @Test
    public void testStopServer() throws Exception {
        createServer(WILDFLY13_ROOT, "wildfly5");
        Map<String, Object> attr = new HashMap<>();
        attr.put("server.home.dir", WILDFLY13_ROOT);
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wfly13Type.getId(), "wildfly5", attr), "run");
        
        serverProxy.startServerAsync(params).get();
        waitForServerState("started", 10);
        
        Status status = serverProxy.stopServerAsync(new StopServerAttributes("wildfly5", false)).get();
        waitForServerState("stopped", 10);
        
        assertEquals(Status.OK, status.getSeverity());
        assertEquals("ok", status.getMessage());
    }
    
    @Test
    public void testStopStoppedServer() throws Exception {
        createServer(WILDFLY13_ROOT, "wildfly6");
        Status status = serverProxy.stopServerAsync(new StopServerAttributes("wildfly6", false)).get();
        
        assertEquals("The server is already marked as stopped. If you wish to force a stop request, please set the force flag to true.", status.getMessage());
        assertEquals(Status.ERROR, status.getSeverity());
    }
    
    @Test
    public void testStopNonexistingServer() throws Exception {
        Status status = serverProxy.stopServerAsync(new StopServerAttributes("wildfly7", false)).get();
        
        assertEquals("Server wildfly7 does not exist", status.getMessage());
        assertEquals(Status.ERROR, status.getSeverity());
    }
    
    @Test
    public void testStopServerNull() throws Exception {
        Status status = serverProxy.stopServerAsync(null).get();
        
        assertEquals(INVALID_PARAM, status.getMessage());
        assertEquals(Status.ERROR, status.getSeverity());
    }
    
    private void waitForServerState(String serverState, int timeout) throws Exception {
        int tries = timeout;
        
        while(tries > 0) {
            tries--;
            if (serverState.equals(client.getStateString())) {
                return;
            }
            Thread.sleep(1000);
        }
        throw new AssertionError("Waiting for server state to change to " + serverState + " timed out");
    }
}
