/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.server.wildfly.test.beans;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class MockCreationTest {
    @Test
    public void testLocateServerMockResources() {
    	String resourceName = "3.2.8.mf.twiddle.jar";
    	File resource = MockServerCreationUtilities.getServerMockResource(resourceName);
    	assertNotNull(resource);
    	assertEquals(resource.getName(), resourceName);
    	File parent = resource.getParentFile();
    	assertTrue(parent.getName().startsWith("serverMock"));
    	assertTrue(resource.exists());
    	
    	File output = MockServerCreationUtilities.getMocksBaseDir();
    	assertNotNull(output);
    }
  }