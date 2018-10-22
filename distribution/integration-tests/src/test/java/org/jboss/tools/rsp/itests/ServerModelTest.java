/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.Status;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author jrichter
 */
public class ServerModelTest extends RSPTestCase {
    
    @Test
    public void testWildflySupport() throws Exception {
        List<ServerType> types = serverProxy.getServerTypes().get();
        
        ServerType wfly10 = new ServerType("org.jboss.ide.eclipse.as.wildfly.100",
                "WildFly 10.x", "A server adapter capable of discovering and controlling a WildFly 10.x runtime instance.");
        ServerType wfly11 = new ServerType("org.jboss.ide.eclipse.as.wildfly.110",
                "WildFly 11.x", "A server adapter capable of discovering and controlling a WildFly 11.x runtime instance.");
        ServerType wfly12 = new ServerType("org.jboss.ide.eclipse.as.wildfly.120",
                "WildFly 12.x", "A server adapter capable of discovering and controlling a WildFly 12.x runtime instance.");
        
        assertTrue(types.contains(wfly10));
        assertTrue(types.contains(wfly11));
        assertTrue(types.contains(wfly12));
        assertTrue(types.contains(wfly13Type));
    }
    
    @Test
    public void testEAPSupport() throws Exception {
        List<ServerType> types = serverProxy.getServerTypes().get();
        
        ServerType eap60 = new ServerType("org.jboss.ide.eclipse.as.eap.60",
                "JBoss EAP 6.0", "A server adapter capable of discovering and controlling a JBoss EAP 6.0 runtime instance.");
        ServerType eap61 = new ServerType("org.jboss.ide.eclipse.as.eap.61",
                "JBoss EAP 6.1", "A server adapter capable of discovering and controlling a JBoss EAP 6.1 runtime instance.");
        ServerType eap70 = new ServerType("org.jboss.ide.eclipse.as.eap.70",
                "JBoss EAP 7.0", "A server adapter capable of discovering and controlling a JBoss EAP 7.0 runtime instance.");
        ServerType eap71 = new ServerType("org.jboss.ide.eclipse.as.eap.71",
                "JBoss EAP 7.1", "A server adapter capable of discovering and controlling a JBoss EAP 7.1 runtime instance.");
        
        assertTrue(types.contains(eap60));
        assertTrue(types.contains(eap61));
        assertTrue(types.contains(eap70));
        assertTrue(types.contains(eap71));
    }
    
    @Test
    public void testMinishiftSupport() throws Exception {
        List<ServerType> types = serverProxy.getServerTypes().get();
        
        ServerType minishift = new ServerType("org.jboss.tools.openshift.cdk.server.type.minishift.v1_12",
                "Minishift 1.12+", "A server adapter capable of controlling a Minishift 1.12+ runtime instance.");
        
        assertTrue(types.contains(minishift));
    }
    
    @Test
    public void testGetRequiredAttributes() throws Exception {
        Attributes attr = serverProxy.getRequiredAttributes(wfly13Type).get();
        
        Map<String, Attribute> expected = new HashMap<>();
        expected.put("server.home.dir", new Attribute("string", "A filesystem path pointing to a server installation's root directory", null));
        assertEquals(new Attributes(expected), attr);
    }
    
    @Test
    public void testGetRequiredAttributesInvalid() throws Exception {
        ServerType type = new ServerType("foo", "bar", "baz");
        
        Attributes attr = serverProxy.getRequiredAttributes(type).get();
        
        assertNull(attr);
    }
    
    @Test
    public void testGetRequiredAttributesNull() throws Exception {
        Attributes attr = serverProxy.getRequiredAttributes(null).get();
        
        assertNull(attr);
    }
    
    @Test
    public void testGetOptionalAttributes() throws Exception {
        Attributes attr = serverProxy.getOptionalAttributes(wfly13Type).get();
        
        Map<String, Attribute> expected = new HashMap<>();
        expected.put("vm.install.path", new Attribute("string",
                "A string representation pointing to a java home. If not set, java.home will be used instead.", null));
        assertEquals(new Attributes(expected), attr);
    }
    
    
    @Test
    public void testGetOptionalAttributesInvalid() throws Exception {
        ServerType type = new ServerType("foo", "bar", "baz");
        
        Attributes attr = serverProxy.getOptionalAttributes(type).get();
        
        assertNull(attr);
    }
    
    @Test
    public void testGetOptionalAttributesNull() throws Exception {
        Attributes attr = serverProxy.getOptionalAttributes(null).get();
        
        assertNull(attr);
    }
    
    @Test
    public void testCreateServer() throws Exception {
        Status status = createServer(WILDFLY13_ROOT, "wfly");
                
        assertEquals(0, status.getSeverity());
        assertEquals("ok", status.getMessage());
        
        List<ServerHandle> handles = serverProxy.getServerHandles().get();
        ServerHandle expected = new ServerHandle("wfly", wfly13Type);
        assertTrue(handles.contains(expected));
    }
    
    @Test
    public void testCreateServerInvalid() throws Exception {
        ServerAttributes attr = new ServerAttributes("UNKNOWN", "fly", null);
        Status status = serverProxy.createServer(attr).get().getStatus();
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals("Server Type UNKNOWN not found", status.getMessage());
    }
    
    @Test
    public void testCreateServerNull() throws Exception {
        Status status = serverProxy.createServer(null).get().getStatus();
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }
    
    @Test
    public void testCreateServerTwice() throws Exception {
        Status status1 = createServer(WILDFLY13_ROOT, "wfly1");
        Status status2 = createServer(WILDFLY13_ROOT, "wfly1");
        
        assertEquals(0, status1.getSeverity());
        assertEquals("ok", status1.getMessage());
        assertEquals(Status.ERROR, status2.getSeverity());
        assertEquals("Server with id wfly1 already exists.", status2.getMessage());
    }
    
    @Test
    public void testDeleteServer() throws Exception {
        createServer(WILDFLY13_ROOT, "wfly2");
        
        ServerHandle handle = new ServerHandle("wfly2", wfly13Type);
        Status status = serverProxy.deleteServer(handle).get();
        
        assertEquals(0, status.getSeverity());
        assertEquals("ok", status.getMessage());
    }
    
    @Test
    public void testDeleteNonexistentServer() throws Exception {
        ServerHandle handle = new ServerHandle("wfly3", wfly13Type);
        Status status = serverProxy.deleteServer(handle).get();
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals("Server not removed: wfly3", status.getMessage());
    }
    
    @Test
    public void testDeleteServerInvalid() throws Exception {
        ServerHandle handle = new ServerHandle("wfly4", new ServerType("foo", "foo", "foo"));
        Status status = serverProxy.deleteServer(handle).get();
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals("Server not removed: wfly4", status.getMessage());
    }
    
    @Test
    public void testDeleteServerNull() throws Exception {
        Status status = serverProxy.deleteServer(null).get();
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }
    
    @Test
    public void testGetServerHandles() throws Exception {
        createServer(WILDFLY13_ROOT, "wfly5");
        createServer(WILDFLY13_ROOT, "wfly6");
        
        List<ServerHandle> handles = serverProxy.getServerHandles().get();
        
        assertTrue(handles.contains(new ServerHandle("wfly5", wfly13Type)));
        assertTrue(handles.contains(new ServerHandle("wfly6", wfly13Type)));
    }
}
