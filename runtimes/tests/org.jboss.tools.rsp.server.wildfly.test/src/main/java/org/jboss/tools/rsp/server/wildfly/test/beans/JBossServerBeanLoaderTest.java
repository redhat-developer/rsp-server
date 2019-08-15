/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.beans;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanLoader;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.impl.JBossServerBeanTypeProvider;
import org.jboss.tools.rsp.server.wildfly.test.util.MatrixUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.TestCase;
/**
 * This test will create run the server bean loader on all runtimes that are marked as jboss runtimes, 
 * which are all astools runtime types currently. If a type is found which does not have a cmd line arg, 
 * that test will fail.  It will verify that the proper type and version for serverbean is found
 * for the given server home. 
 * 
 *  It will also make MOCK structures for all server types, including 'subtypes' like 
 *  jpp, gatein, etc. This will help verify the mocks are created with the minimal requirements
 *  that the server bean loader looks for. 
 * 
 */
@RunWith(value = Parameterized.class)
public class JBossServerBeanLoaderTest extends TestCase {
	@Parameters(name = "{0}")
	 public static Collection<Object[]> data() {
		 return MatrixUtils.asCollection(MockServerCreationUtilities.getJBossServerTypeParametersPlusAdditionalMocks());
	 }

	protected HashMap<String, Data> expected = new HashMap<String, Data>();
	protected class Data {
		private ServerBeanType type;
		private String version, overrideId;
		public Data(ServerBeanType type, String version) {
			this.type = type;
			this.version = version;
			this.overrideId = null;
		}
		public Data(ServerBeanType type, String version, String oid) {
			this(type, version);
			this.overrideId = oid;
		}
	}
	
	
	protected String serverType;
	public JBossServerBeanLoaderTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() {
		expected.put(IServerConstants.SERVER_AS_32, new Data(JBossServerBeanTypeProvider.AS, "3.2."));
		expected.put(IServerConstants.SERVER_AS_40, new Data(JBossServerBeanTypeProvider.AS, "4.0."));
		expected.put(IServerConstants.SERVER_AS_42, new Data(JBossServerBeanTypeProvider.AS, "4.2."));
		expected.put(IServerConstants.SERVER_AS_50, new Data(JBossServerBeanTypeProvider.AS, "5.0."));
		expected.put(IServerConstants.SERVER_AS_51, new Data(JBossServerBeanTypeProvider.AS, "5.1."));
		expected.put(IServerConstants.SERVER_AS_60, new Data(JBossServerBeanTypeProvider.AS, "6.0."));
		expected.put(IServerConstants.SERVER_AS_70, new Data(JBossServerBeanTypeProvider.AS7, "7.0."));
		expected.put(IServerConstants.SERVER_AS_71, new Data(JBossServerBeanTypeProvider.AS7, "7.1."));
		expected.put(IServerConstants.SERVER_WILDFLY_80, new Data(JBossServerBeanTypeProvider.WILDFLY80, "8.0."));
		expected.put(IServerConstants.SERVER_WILDFLY_90, new Data(JBossServerBeanTypeProvider.WILDFLY90, "9.0."));
		expected.put(IServerConstants.SERVER_WILDFLY_100, new Data(JBossServerBeanTypeProvider.WILDFLY100, "10.0."));
		expected.put(IServerConstants.SERVER_WILDFLY_110, new Data(JBossServerBeanTypeProvider.WILDFLY110, "11.0."));
		expected.put(IServerConstants.SERVER_WILDFLY_120, new Data(JBossServerBeanTypeProvider.WILDFLY120, "12.0."));
		expected.put(IServerConstants.SERVER_WILDFLY_130, new Data(JBossServerBeanTypeProvider.WILDFLY130, "13.0."));
		expected.put(IServerConstants.SERVER_WILDFLY_140, new Data(JBossServerBeanTypeProvider.WILDFLY140, "14.0."));
		expected.put(IServerConstants.SERVER_WILDFLY_150, new Data(JBossServerBeanTypeProvider.WILDFLY150, "15.0."));
		expected.put(IServerConstants.SERVER_WILDFLY_160, new Data(JBossServerBeanTypeProvider.WILDFLY160, "16.0."));
		expected.put(IServerConstants.SERVER_WILDFLY_170, new Data(JBossServerBeanTypeProvider.WILDFLY170, "17.0."));
		expected.put(IServerConstants.SERVER_EAP_43, new Data(JBossServerBeanTypeProvider.EAP_STD, "4.3."));
		expected.put(IServerConstants.SERVER_EAP_50, new Data(JBossServerBeanTypeProvider.EAP_STD, "5.1."));
		expected.put(IServerConstants.SERVER_EAP_60, new Data(JBossServerBeanTypeProvider.EAP6, "6.0."));
		expected.put(IServerConstants.SERVER_EAP_61, new Data(JBossServerBeanTypeProvider.EAP61, "6.1."));
		expected.put(IServerConstants.SERVER_EAP_70, new Data(JBossServerBeanTypeProvider.EAP70, "7.0."));
		expected.put(IServerConstants.SERVER_EAP_71, new Data(JBossServerBeanTypeProvider.EAP71, "7.1."));
		expected.put(IServerConstants.SERVER_EAP_72, new Data(JBossServerBeanTypeProvider.EAP72, "7.2."));
		expected.put(MockServerCreationUtilities.TEST_SERVER_TYPE_EAP_65, new Data(JBossServerBeanTypeProvider.EAP61, "6.5."));
		expected.put(MockServerCreationUtilities.TEST_SERVER_TYPE_GATEIN_34, new Data(JBossServerBeanTypeProvider.AS7GateIn, "3.4."));
		expected.put(MockServerCreationUtilities.TEST_SERVER_TYPE_GATEIN_35, new Data(JBossServerBeanTypeProvider.AS7GateIn, "3.5."));
		expected.put(MockServerCreationUtilities.TEST_SERVER_TYPE_GATEIN_36, new Data(JBossServerBeanTypeProvider.AS7GateIn, "3.6."));
		expected.put(MockServerCreationUtilities.TEST_SERVER_TYPE_JPP_60, new Data(JBossServerBeanTypeProvider.JPP6, "6.0."));
		expected.put(MockServerCreationUtilities.TEST_SERVER_TYPE_JPP_61, new Data(JBossServerBeanTypeProvider.JPP61, "6.1.", "JPP"));
		expected.put(MockServerCreationUtilities.TEST_SERVER_TYPE_WONKA_1, new Data(JBossServerBeanTypeProvider.UNKNOWN_AS72_PRODUCT, "1.0.", "WONKA"));
		// NEW_SERVER_ADAPTER
	}

	/*
	 * Create a mock folder and verify the mock folder matches also
	 */
	@Test
	public void testServerBeanLoaderForMocks() {
		File serverDir = (MockServerCreationUtilities.createMockServerLayout(serverType));
		if( serverDir == null || !serverDir.exists())
			fail("Creation of mock server type " + serverType + " has failed.");
		Data p = expected.get(serverType);
		inner_testServerBeanLoaderForFolder(serverDir, p.type, p.version, p.overrideId);
	}
	
	protected void inner_testServerBeanLoaderForFolder(File serverDir, ServerBeanType expectedType, String expectedVersion, String underlyingId) {
		assertNotNull(serverType);
		if( expected.get(serverType) == null )
			fail("Test Case needs to be updated for new adapter or mock test");
		
		ServerBeanLoader loader = MockServerCreationUtilities.createMockServerBeanLoader(serverType, serverDir);
		ServerBeanType type = loader.getServerBeanType();
		assertEquals("Expected and actual server beans do not match for server type " + serverType, expectedType, type);
		String fullVersion = loader.getFullServerVersion();
		assertTrue(fullVersion + " does not begin with " + expectedVersion + " for server type " + serverType, 
				fullVersion.startsWith(expectedVersion));
		
		String expectedServerTypeId = expectedType.getServerAdapterTypeId(expectedVersion);
		assertEquals(loader.getServerAdapterId(), expectedServerTypeId);
		
		String underlying = loader.getUnderlyingTypeId();
		assertNotNull(underlying);
		if(underlyingId != null ) {
			assertEquals(underlyingId, underlying);
		} else {
			assertEquals(expectedType.getId(), underlying);
		}
	}
}
