/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.server.wildfly.test.beans;

import static org.junit.Assert.assertNotNull;

import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.wildfly.impl.JBossServerBeanTypeProvider;
import org.junit.Test;

public class ServerBeanTypeProviderTest {
    @Test
    public void testLocateServerMockResources() {
    	JBossServerBeanTypeProvider provider = new JBossServerBeanTypeProvider();
    	assertNotNull(provider);
    	ServerBeanType[] types = provider.getServerBeanTypes();
    	assertNotNull(types);
    }
}
