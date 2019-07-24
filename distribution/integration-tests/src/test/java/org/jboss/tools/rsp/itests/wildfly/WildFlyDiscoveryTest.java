/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.itests.RSPCase;
import org.junit.Test;

import static org.junit.Assert.*;

public class WildFlyDiscoveryTest extends RSPCase {    
    
    @Test
    public void testFindServerBeansWildfly() throws Exception {
        List<ServerBean> beans = serverProxy.findServerBeans(new DiscoveryPath(WILDFLY_ROOT)).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        ServerBean bean = beans.get(0);
        
        assertEquals( WILDFLY_SERVER_ID, bean.getServerAdapterTypeId());
        assertEquals("WildFly", bean.getSpecificType());
        assertEquals("WildFly", bean.getTypeCategory());
        assertEquals(WILDFLY_VERSION, bean.getFullVersion());
    }
    
    @Test
    public void testFindServerBeansInvalid() throws Exception {
        List<ServerBean> beans = serverProxy.findServerBeans(new DiscoveryPath(WILDFLY_ROOT + "/foo")).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        ServerBean bean = beans.get(0);
        
        assertEquals("UNKNOWN", bean.getTypeCategory());
        assertTrue(bean.getVersion().isEmpty());
        assertTrue(bean.getServerAdapterTypeId().isEmpty());
        assertNull(bean.getSpecificType());
        assertTrue(bean.getFullVersion().isEmpty());
    }
    
    @Test
    public void testFindServerBeansNull() throws Exception {
        List<ServerBean> beans = serverProxy.findServerBeans(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertTrue(beans.isEmpty());
    }
    
    @Test
    public void testFindServerBeansRelative() throws Exception {
        List<ServerBean> beans = serverProxy.findServerBeans(new DiscoveryPath("../foo")).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertTrue(beans.isEmpty());
    }
    
    @Test
    public void testAddDiscoveryPath() throws Exception {
        DiscoveryPath path = new DiscoveryPath(WILDFLY_ROOT + "1");
        Status status = serverProxy.addDiscoveryPath(path).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        List<DiscoveryPath> paths = serverProxy.getDiscoveryPaths().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        serverProxy.removeDiscoveryPath(path).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(0, status.getSeverity());
        assertEquals("ok", status.getMessage());
        assertTrue(paths.contains(path));
    }
    
    @Test
    public void testAddDiscoveryPathTwice() throws Exception {
        DiscoveryPath path = new DiscoveryPath(WILDFLY_ROOT + "2");
        Status status1 = serverProxy.addDiscoveryPath(path).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        Status status2 = serverProxy.addDiscoveryPath(path).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        serverProxy.removeDiscoveryPath(path).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(0, status1.getSeverity());
        assertEquals("ok", status1.getMessage());
        assertEquals(Status.ERROR, status2.getSeverity());
        assertEquals("Discovery path not added: " + WILDFLY_ROOT + "2", status2.getMessage());
    }
    
    @Test
    public void testAddNullDiscoveryPath() throws Exception {
        Status status = serverProxy.addDiscoveryPath(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }
    
    @Test
    public void testAddInvalidDiscoveryPath() throws Exception {
        Status status = serverProxy.addDiscoveryPath(new DiscoveryPath("fooo")).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }
    
    @Test
    public void testRemoveDiscoveryPath() throws Exception {
        DiscoveryPath path = new DiscoveryPath(WILDFLY_ROOT + "3");
        serverProxy.addDiscoveryPath(path).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);        
        assertTrue(serverProxy.getDiscoveryPaths().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).contains(path));
        
        Status status = serverProxy.removeDiscoveryPath(path).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        assertEquals(0, status.getSeverity());
        assertEquals("ok", status.getMessage());
        assertFalse(serverProxy.getDiscoveryPaths().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).contains(path));
    }
    
    @Test
    public void testRemoveNonexistentDiscoveryPath() throws Exception {
        Status status = serverProxy.removeDiscoveryPath(new DiscoveryPath("path")).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }
    
    @Test
    public void testRemoveNullDiscoveryPath() throws Exception {
        Status status = serverProxy.removeDiscoveryPath(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        assertEquals(Status.ERROR, status.getSeverity());
        assertEquals(INVALID_PARAM, status.getMessage());
    }
    
    @Test
    public void testGetDiscoveryPaths() throws Exception {
        DiscoveryPath path1 = new DiscoveryPath(WILDFLY_ROOT + "4");
        DiscoveryPath path2 = new DiscoveryPath(WILDFLY_ROOT + "5");
        
        serverProxy.addDiscoveryPath(path1).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        serverProxy.addDiscoveryPath(path2).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        
        List<DiscoveryPath> paths = serverProxy.getDiscoveryPaths().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        assertTrue(paths.contains(path1));
        assertTrue(paths.contains(path2));
    }
}