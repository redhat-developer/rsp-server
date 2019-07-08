/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.itests.RSPCase;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.junit.Test;

/**
 *
 * @author jrichter
 */
public class WildFlyServerModelTest extends RSPCase {
    
    @Test
    public void testWildflySupport() throws Exception {
        List<ServerType> types = serverProxy.getServerTypes().get();
        
        ServerType wfly10 = new ServerType("org.jboss.ide.eclipse.as.wildfly.100",
                "WildFly 10.x", "A server adapter capable of discovering and controlling a WildFly 10.x runtime instance.");
        ServerType wfly11 = new ServerType("org.jboss.ide.eclipse.as.wildfly.110",
                "WildFly 11.x", "A server adapter capable of discovering and controlling a WildFly 11.x runtime instance.");
        ServerType wfly12 = new ServerType("org.jboss.ide.eclipse.as.wildfly.120",
                "WildFly 12.x", "A server adapter capable of discovering and controlling a WildFly 12.x runtime instance.");
        ServerType wfly13 = new ServerType("org.jboss.ide.eclipse.as.wildfly.130",
                "WildFly 13.x", "A server adapter capable of discovering and controlling a WildFly 13.x runtime instance.");
        ServerType wfly14 = new ServerType("org.jboss.ide.eclipse.as.wildfly.140",
                "WildFly 14.x", "A server adapter capable of discovering and controlling a WildFly 14.x runtime instance.");
        ServerType wfly15 = new ServerType("org.jboss.ide.eclipse.as.wildfly.150",
                "WildFly 15.x", "A server adapter capable of discovering and controlling a WildFly 15.x runtime instance.");
        ServerType wfly16 = new ServerType("org.jboss.ide.eclipse.as.wildfly.160",
                "WildFly 16.x", "A server adapter capable of discovering and controlling a WildFly 16.x runtime instance.");
        ServerType wfly17 = new ServerType("org.jboss.ide.eclipse.as.wildfly.170",
                "WildFly 17.x", "A server adapter capable of discovering and controlling a WildFly 17.x runtime instance.");

        
        assertTrue(types.contains(wfly10));
        assertTrue(types.contains(wfly11));
        assertTrue(types.contains(wfly12));
        assertTrue(types.contains(wfly13));
        assertTrue(types.contains(wfly14));
        assertTrue(types.contains(wfly15));
        assertTrue(types.contains(wfly16));
        assertTrue(types.contains(wfly17));
        assertTrue(types.contains(wildflyType));
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
        ServerType eap72 = new ServerType("org.jboss.ide.eclipse.as.eap.72",
                "JBoss EAP 7.2", "A server adapter capable of discovering and controlling a JBoss EAP 7.2 runtime instance.");
        
        assertTrue(types.contains(eap60));
        assertTrue(types.contains(eap61));
        assertTrue(types.contains(eap70));
        assertTrue(types.contains(eap71));
        assertTrue(types.contains(eap72));
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
        Attributes attr = serverProxy.getRequiredAttributes(wildflyType).get();
        
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
        Attributes attr = serverProxy.getOptionalAttributes(wildflyType).get();
        
        Map<String, Attribute> expected = new HashMap<>();
        expected.put("vm.install.path", new Attribute("string",
                "A string representation pointing to a java home. If not set, java.home will be used instead.", null));
        
        expected.put(IJBossServerAttributes.AUTOPUBLISH_ENABLEMENT,
        		new Attribute(
					ServerManagementAPIConstants.ATTR_TYPE_BOOL, 
					"Enable the autopublisher.", 
					IJBossServerAttributes.AUTOPUBLISH_ENABLEMENT_DEFAULT)
        		);
        expected.put(IJBossServerAttributes.AUTOPUBLISH_INACTIVITY_LIMIT,
        		new Attribute(
					ServerManagementAPIConstants.ATTR_TYPE_INT, 
					"Set the inactivity limit before the autopublisher runs.", 
					IJBossServerAttributes.AUTOPUBLISH_INACTIVITY_LIMIT_DEFAULT)
        		);
        expected.put(IJBossServerAttributes.JBOSS_SERVER_HOST, 
        		new Attribute(
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"Set the host you want your JBoss / WildFly instance to bind to. Use 0.0.0.0 for all.", 
					IJBossServerAttributes.JBOSS_SERVER_HOST_DEFAULT)
        		);
        expected.put(IJBossServerAttributes.JBOSS_SERVER_PORT,
        		new Attribute(
					ServerManagementAPIConstants.ATTR_TYPE_INT, 
					"Set the port you want your JBoss / WildFly instance to bind to", 
					IJBossServerAttributes.JBOSS_SERVER_PORT_DEFAULT)
        		);
        expected.put(IJBossServerAttributes.WILDFLY_CONFIG_FILE, 
        		new Attribute(
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"Set the configuration file you want your WildFly instance to use.", 
					IJBossServerAttributes.WILDFLY_CONFIG_FILE_DEFAULT)
        		);
		for( String k : expected.keySet()) {
			Attribute expectedAttr = expected.get(k);
			Attribute actualAttr = attr.getAttributes().get(k);
			assertEquals(expectedAttr.getDescription(), actualAttr.getDescription());
			assertEquals(expectedAttr.getType(), actualAttr.getType());
			assertEquals(expectedAttr.isSecret(), actualAttr.isSecret());
			assertEqualsExceptDoubleShouldBeInt(
					expectedAttr.getDefaultVal(), actualAttr.getDefaultVal());
		}
    }

	private void assertEqualsExceptDoubleShouldBeInt(
			Object expected, Object actual) {
		if( expected instanceof Integer && actual instanceof Double) {
			assertTrue(((Double)actual).intValue() == ((Integer)expected).intValue());
		} else {
			assertEquals(expected, actual);
		}
		
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
    	String serverName = createUniqueName("wfly");
    	Status status = createServer(WILDFLY_ROOT, serverName);
                
        assertEquals(IStatus.OK, status.getSeverity());
        assertEquals("ok", status.getMessage());
        
        List<ServerHandle> handles = serverProxy.getServerHandles().get();
        ServerHandle expected = new ServerHandle(serverName, wildflyType);
        assertTrue(handles.contains(expected));
    }
    
    @Test
    public void testCreateServerInvalid() throws Exception {
        ServerAttributes attr = new ServerAttributes("UNKNOWN", "fly", null);
        Status status = serverProxy.createServer(attr).get().getStatus();
        
        assertEquals(IStatus.ERROR, status.getSeverity());
        assertEquals("Server Type UNKNOWN not found", status.getMessage());
    }
    
    @Test
    public void testCreateServerNull() throws Exception {
        Status status = serverProxy.createServer(null).get().getStatus();
        
        assertEquals(IStatus.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }
    
    @Test
    public void testCreateServerTwice() throws Exception {
    	String serverName = createUniqueName("wfly1");
    	Status status1 = createServer(WILDFLY_ROOT, serverName);
        Status status2 = createServer(WILDFLY_ROOT, serverName);
        
        assertEquals(IStatus.OK, status1.getSeverity());
        assertEquals("ok", status1.getMessage());
        assertEquals(IStatus.ERROR, status2.getSeverity());
        assertTrue(status2.getMessage().contains("already exists"));
    }
    
    @Test
    public void testDeleteServer() throws Exception {
        createServer(WILDFLY_ROOT, "wfly2");
        
        ServerHandle handle = new ServerHandle("wfly2", wildflyType);
        Status status = serverProxy.deleteServer(handle).get();
        
        assertEquals(IStatus.OK, status.getSeverity());
        assertEquals("ok", status.getMessage());
    }
    
    @Test
    public void testDeleteNonexistentServer() throws Exception {
        ServerHandle handle = new ServerHandle("wfly3", wildflyType);
        Status status = serverProxy.deleteServer(handle).get();
        
        assertEquals(IStatus.ERROR, status.getSeverity());
        assertEquals("Server wfly3 does not exist", status.getMessage());
    }
    
    @Test
    public void testDeleteServerInvalid() throws Exception {
        ServerHandle handle = new ServerHandle("wfly4", new ServerType("foo", "foo", "foo"));
        Status status = serverProxy.deleteServer(handle).get();
        
        assertEquals(IStatus.ERROR, status.getSeverity());
        assertEquals("Invalid Request: Server type not found.", status.getMessage());
    }
    
    @Test
    public void testDeleteServerNull() throws Exception {
        Status status = serverProxy.deleteServer(null).get();
        
        assertEquals(IStatus.ERROR, status.getSeverity());
        assertEquals(MISSING_SERVER_HANDLE, status.getMessage());
    }
    
    @Test
    public void testGetServerHandles() throws Exception {
        createServer(WILDFLY_ROOT, "wfly5");
        createServer(WILDFLY_ROOT, "wfly6");
        
        List<ServerHandle> handles = serverProxy.getServerHandles().get();
        
        assertTrue(handles.contains(new ServerHandle("wfly5", wildflyType)));
        assertTrue(handles.contains(new ServerHandle("wfly6", wildflyType)));
    }
    
    private static String createUniqueName(String prefix) {
    	return prefix + System.currentTimeMillis();
    }
}
