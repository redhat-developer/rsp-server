/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.beans;

import static org.jboss.tools.rsp.server.wildfly.beans.impl.IJBossServerResourceConstants.ID_AS;
import static org.jboss.tools.rsp.server.wildfly.beans.impl.IJBossServerResourceConstants.ID_EAP;
import static org.jboss.tools.rsp.server.wildfly.beans.impl.IJBossServerResourceConstants.ID_WILDFLY;
import static org.jboss.tools.rsp.server.wildfly.beans.impl.IJBossServerResourceConstants.NAME_AS;
import static org.jboss.tools.rsp.server.wildfly.beans.impl.IJBossServerResourceConstants.NAME_EAP;
import static org.jboss.tools.rsp.server.wildfly.beans.impl.IJBossServerResourceConstants.NAME_WILDFLY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.test.util.MatrixUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * @author odockal
 *
 */
@RunWith(value = Parameterized.class)
public class ServerBeanTypeTest {
	
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return MatrixUtils.asCollection(MockServerCreationUtilities.getJBossServerTypeParameters());
	}
	
	protected String serverType;
	
	public ServerBeanTypeTest(String type) {
		this.serverType = type;
	}
	
	private Map<String, ServerBeanTypeMock> jbossMap = getTypes();
	
	public Map<String, ServerBeanTypeMock> getTypes() {
		Map<String, ServerBeanTypeMock> map = new HashMap<>();
		map.put(IServerConstants.SERVER_AS_32, new ServerBeanTypeMock(ID_AS, NAME_AS, IServerConstants.SERVER_AS_32, "3.2"));
		map.put(IServerConstants.SERVER_AS_40, new ServerBeanTypeMock(ID_AS, NAME_AS, IServerConstants.SERVER_AS_40, "4.0"));
		map.put(IServerConstants.SERVER_AS_42, new ServerBeanTypeMock(ID_AS, NAME_AS, IServerConstants.SERVER_AS_42, "4.2"));
		map.put(IServerConstants.SERVER_AS_50, new ServerBeanTypeMock(ID_AS, NAME_AS, IServerConstants.SERVER_AS_50, "5.0"));
		map.put(IServerConstants.SERVER_AS_51, new ServerBeanTypeMock(ID_AS, NAME_AS, IServerConstants.SERVER_AS_51, "5.1"));
		map.put(IServerConstants.SERVER_AS_60, new ServerBeanTypeMock(ID_AS, NAME_AS, IServerConstants.SERVER_AS_60, "6.0"));
		map.put(IServerConstants.SERVER_AS_70, new ServerBeanTypeMock(ID_AS, NAME_AS, IServerConstants.SERVER_AS_70, "7.0"));
		map.put(IServerConstants.SERVER_AS_71, new ServerBeanTypeMock(ID_AS, NAME_AS, IServerConstants.SERVER_AS_71, "7.1"));
		map.put(IServerConstants.SERVER_EAP_43, new ServerBeanTypeMock("EAP_STD", NAME_EAP, IServerConstants.SERVER_EAP_43, "4.3"));
		map.put(IServerConstants.SERVER_EAP_50, new ServerBeanTypeMock("EAP_STD", NAME_EAP, IServerConstants.SERVER_EAP_50, "5.1"));
		map.put(IServerConstants.SERVER_EAP_60, new ServerBeanTypeMock(ID_EAP, NAME_EAP, IServerConstants.SERVER_EAP_60, "6.0"));
		map.put(IServerConstants.SERVER_EAP_61, new ServerBeanTypeMock(ID_EAP, NAME_EAP, IServerConstants.SERVER_EAP_61, "6.1"));
		map.put(IServerConstants.SERVER_EAP_70, new ServerBeanTypeMock(ID_EAP, NAME_EAP, IServerConstants.SERVER_EAP_70, "7.0"));
		map.put(IServerConstants.SERVER_EAP_71, new ServerBeanTypeMock(ID_EAP, NAME_EAP, IServerConstants.SERVER_EAP_71, "7.1"));
		map.put(IServerConstants.SERVER_EAP_72, new ServerBeanTypeMock(ID_EAP, NAME_EAP, IServerConstants.SERVER_EAP_72, "7.2"));
		map.put(IServerConstants.SERVER_EAP_73, new ServerBeanTypeMock(ID_EAP, NAME_EAP, IServerConstants.SERVER_EAP_73, "7.3"));
		map.put(IServerConstants.SERVER_WILDFLY_80, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_80, "8.0"));
		map.put(IServerConstants.SERVER_WILDFLY_90, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_90, "9.0"));
		map.put(IServerConstants.SERVER_WILDFLY_100, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_100, "10.0"));
		map.put(IServerConstants.SERVER_WILDFLY_110, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_110, "11.0"));
		map.put(IServerConstants.SERVER_WILDFLY_120, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_120, "12.0"));
		map.put(IServerConstants.SERVER_WILDFLY_130, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_130, "13.0"));
		map.put(IServerConstants.SERVER_WILDFLY_140, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_140, "14.0"));
		map.put(IServerConstants.SERVER_WILDFLY_150, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_150, "15.0"));
		map.put(IServerConstants.SERVER_WILDFLY_160, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_160, "16.0"));
		map.put(IServerConstants.SERVER_WILDFLY_170, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_170, "17.0"));
		map.put(IServerConstants.SERVER_WILDFLY_180, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_180, "18.0"));
		map.put(IServerConstants.SERVER_WILDFLY_190, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_190, "19.0"));
		map.put(IServerConstants.SERVER_WILDFLY_200, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_200, "20.0"));
		map.put(IServerConstants.SERVER_WILDFLY_210, new ServerBeanTypeMock(ID_WILDFLY, NAME_WILDFLY, IServerConstants.SERVER_WILDFLY_210, "21.0"));
		// NEW_SERVER_ADAPTER
		return map;
	}
	
	@Test
	public void testServerBeanLoaderTypeFromProvider() {
		ServerBeanTypeMock expected = jbossMap.get(serverType);
		if( expected == null )
			fail("Test Case needs to be updated for new adapter or mock test");
		
		File serverDir = (MockServerCreationUtilities.createMockServerLayout(serverType));
		ServerBeanLoader loader = MockServerCreationUtilities.createMockServerBeanLoader(serverType, serverDir);
		verifyServerBeanType(loader, serverDir, expected);
	}
	
	protected void verifyServerBeanType(ServerBeanLoader loader, File location, ServerBeanTypeMock expected) {
		ServerBeanType actual = loader.getServerBeanType();
		
		assertEquals(expected.serverTypeId, actual.getServerAdapterTypeId(actual.getFullVersion(location)));
		assertEquals(expected.id, actual.getId());
		assertTrue(actual.getFullVersion(location).startsWith(expected.version));
		assertEquals(expected.name, actual.getName());
		assertTrue(actual.getServerBeanName(location).startsWith(expected.serverTypeId));
		assertEquals(expected.id, actual.getUnderlyingTypeId(location));
		assertTrue("Expected + " + location + " to be root of server", actual.isServerRoot(location));
		assertTrue(!actual.getFullVersion(location).isEmpty() && actual.getFullVersion(location) != null);
	}
	
	private class ServerBeanTypeMock {
		public String version;
		public String serverTypeId;
		public String name;
		public String id;
		
		public ServerBeanTypeMock(String id, String name, String stid, String version) {
			this.serverTypeId = stid;
			this.version = version;
			this.name = name;
			this.id = id;
		}
	}
}
