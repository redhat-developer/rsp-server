package org.jboss.tools.rsp.server.wildfly.test.beans;

import static org.junit.Assert.assertNotNull;

import java.io.File;

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
    	
    	File root = MockServerCreationUtilities.getServerMockResourcesRoot();
    	assertNotNull(root);
    }
}
