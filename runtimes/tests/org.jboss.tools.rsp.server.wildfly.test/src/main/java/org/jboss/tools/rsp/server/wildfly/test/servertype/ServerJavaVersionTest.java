/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.servertype;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeManager;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.spi.util.VersionComparisonUtility;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.ExtendedServerPropertiesAdapterFactory;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.JBossExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.ServerExtendedProperties;
import org.junit.Test;

public class ServerJavaVersionTest {

	@Test
	public void testServerTypes() {
		String[] toTest = IServerConstants.ALL_JBOSS_SERVERS;
		ExtendedServerPropertiesAdapterFactory fact = new ExtendedServerPropertiesAdapterFactory();
		for (int i = 0; i < toTest.length; i++) {
			String serverType = toTest[i];
			IServer s = createServer(serverType);
			ServerExtendedProperties props = fact.getExtendedProperties(s);
			assertNotNull("Server " + serverType + " has no properties,", props);
			assertTrue(props instanceof JBossExtendedProperties);
			JBossExtendedProperties t = (JBossExtendedProperties) props;

			assertNotNull(t.getMinimumJavaVersionString());
			assertNotNull(t.getMaximumJavaVersionString());

		}
	}

	private void serverTypeTest(String typeId, String[] success, String[] fails) {
		String serverType = typeId;
		IServer s = createServer(serverType);
		ExtendedServerPropertiesAdapterFactory fact = new ExtendedServerPropertiesAdapterFactory();
		ServerExtendedProperties props = fact.getExtendedProperties(s);
		assertNotNull("Server " + serverType + " has no properties,", props);
		assertTrue(props instanceof JBossExtendedProperties);
		JBossExtendedProperties t = (JBossExtendedProperties) props;

		String min = t.getMinimumJavaVersionString();
		String max = t.getMaximumJavaVersionString();
		
		// Bad code but want to make lines shorter
		VersionComparisonUtility v = new VersionComparisonUtility(); 
		
		for( int i = 0; i < fails.length; i++ ) {
			assertFalse(v.isJavaCompatible(fails[i], min, max));
		}

		for( int i = 0; i < success.length; i++ ) {
			assertTrue(v.isJavaCompatible(success[i], min, max));
		}
	}
	
	private static final String JAVA_2 = "1.2.6";
	private static final String JAVA_3 = "1.3.6";
	private static final String JAVA_4 = "1.4.6";
	private static final String JAVA_5 = "1.5.6";
	private static final String JAVA_6 = "1.6.6";
	private static final String JAVA_7 = "1.7.6";
	private static final String JAVA_8 = "1.8.6";
	private static final String JAVA_9 = "9.6.1";
	private static final String JAVA_10 = "10.6.1";
	private static final String JAVA_11 = "11.6.0";
	private static final String JAVA_12 = "12.6.0";
	
	
	public void test__AS_32() {
		serverTypeTest(IServerConstants.SERVER_AS_32, 
				new String[] {JAVA_3, JAVA_4, JAVA_5}, 
				new String[] {JAVA_2, JAVA_6});
	}

	@Test
	public void test__AS_40() {
		serverTypeTest(IServerConstants.SERVER_AS_40, 
				new String[] {JAVA_4, JAVA_5}, 
				new String[] {JAVA_2, JAVA_3, JAVA_6});

	}

	@Test
	public void test__AS_42() {
		serverTypeTest(IServerConstants.SERVER_AS_42, 
				new String[] {JAVA_5,JAVA_6}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_7, JAVA_8});
	}

	@Test
	public void test__AS_50() {
		serverTypeTest(IServerConstants.SERVER_AS_50, 
				new String[] {JAVA_4,JAVA_5}, 
				new String[] {JAVA_2, JAVA_3, JAVA_7, JAVA_6, JAVA_8});
	}

	@Test
	public void test__AS_51() {
		serverTypeTest(IServerConstants.SERVER_AS_51, 
				new String[] {JAVA_5,JAVA_6}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_7, JAVA_8});
	}

	@Test
	public void test__AS_60() {
		serverTypeTest(IServerConstants.SERVER_AS_60, 
				new String[] {JAVA_6,JAVA_7,JAVA_8}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_9});
	}

	@Test
	public void test__AS_70() {
		serverTypeTest(IServerConstants.SERVER_AS_70, 
				new String[] {JAVA_6,JAVA_7,JAVA_8,JAVA_9}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_10});
	}

	@Test
	public void test__AS_71() {
		serverTypeTest(IServerConstants.SERVER_AS_71, 
				new String[] {JAVA_6,JAVA_7,JAVA_8,JAVA_9}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_10});
	}

	@Test
	public void test__WILDFLY_80() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_80, 
				new String[] {JAVA_6,JAVA_7,JAVA_8,JAVA_9}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_10, JAVA_11});
	}

	@Test
	public void test__WILDFLY_90() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_90, 
				new String[] {JAVA_6,JAVA_7,JAVA_8,JAVA_9}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_10, JAVA_11});
	}

	@Test
	public void test__WILDFLY_100() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_100, 
				new String[] {JAVA_8,JAVA_9, JAVA_10, JAVA_11}, 
				new String[] {JAVA_6, JAVA_7, JAVA_12});
	}

	@Test
	public void test__WILDFLY_110() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_110, 
				new String[] {JAVA_8,JAVA_9, JAVA_10, JAVA_11}, 
				new String[] {JAVA_6, JAVA_7, JAVA_12});
	}

	@Test
	public void test__WILDFLY_120() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_120, 
				new String[] {JAVA_8,JAVA_9, JAVA_10, JAVA_11}, 
				new String[] {JAVA_6, JAVA_7, JAVA_12});
	}

	@Test
	public void test__WILDFLY_130() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_130, 
				new String[] {JAVA_8,JAVA_9, JAVA_10, JAVA_11}, 
				new String[] {JAVA_6, JAVA_7, JAVA_12});
	}

	@Test
	public void test__WILDFLY_140() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_140, 
				new String[] {JAVA_8,JAVA_9, JAVA_10, JAVA_11}, 
				new String[] {JAVA_6, JAVA_7, JAVA_12});
	}

	@Test
	public void test__WILDFLY_150() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_150, 
				new String[] {JAVA_8,JAVA_9, JAVA_10, JAVA_11}, 
				new String[] {JAVA_6, JAVA_7, JAVA_12});
	}

	@Test
	public void test__WILDFLY_160() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_160, 
				new String[] {JAVA_8,JAVA_9, JAVA_10, JAVA_11}, 
				new String[] {JAVA_6, JAVA_7, JAVA_12});
	}

	@Test
	public void test__EAP_43() {
		serverTypeTest(IServerConstants.SERVER_EAP_43, 
				new String[] {JAVA_6, JAVA_7}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_5, JAVA_8});
	}

	@Test
	public void test__EAP_50() {
		serverTypeTest(IServerConstants.SERVER_EAP_50, 
				new String[] {JAVA_6, JAVA_7}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_5, JAVA_8});
	}

	@Test
	public void test__EAP_60() {
		serverTypeTest(IServerConstants.SERVER_EAP_60, 
				new String[] {JAVA_6,JAVA_7,JAVA_8}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_9, JAVA_10, JAVA_11});
	}

	@Test
	public void test__EAP_61() {
		serverTypeTest(IServerConstants.SERVER_EAP_61, 
				new String[] {JAVA_6,JAVA_7,JAVA_8}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_9, JAVA_10, JAVA_11});
	}

	@Test
	public void test__EAP_70() {
		serverTypeTest(IServerConstants.SERVER_EAP_70, 
				new String[] {JAVA_8}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_6,JAVA_7, JAVA_9, JAVA_10, JAVA_11});
	}

	@Test
	public void test__EAP_71() {
		serverTypeTest(IServerConstants.SERVER_EAP_71, 
				new String[] {JAVA_8}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_6,JAVA_7, JAVA_9, JAVA_10, JAVA_11});
	}

	@Test
	public void test__EAP_72() {
		serverTypeTest(IServerConstants.SERVER_EAP_71, 
				new String[] {JAVA_8,JAVA_9, JAVA_10, JAVA_11}, 
				new String[] {JAVA_2, JAVA_3, JAVA_4, JAVA_6,JAVA_7,JAVA_12});
	}

	private IServer createServer(String type) {
		IServer s = mock(IServer.class);
		IServerType st = mock(IServerType.class);
		doReturn(st).when(s).getServerType();
		doReturn(type).when(st).getId();
		doReturn(".").when(s).getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);

		IServerManagementModel managementModel = mockServerManagementModel();
		doReturn(managementModel).when(s).getServerManagementModel();

		return s;
	}

	private IServerManagementModel mockServerManagementModel() {
		IServerManagementModel managementModel = mock(IServerManagementModel.class);
		IServerBeanTypeManager beanTypeManager = mockServerBeanTypeManager();
		doReturn(beanTypeManager).when(managementModel).getServerBeanTypeManager();
		return managementModel;
	}

	private IServerBeanTypeManager mockServerBeanTypeManager() {
		IServerBeanTypeManager beanTypeManager = mock(IServerBeanTypeManager.class);
		doReturn(new ServerBeanType[] {}).when(beanTypeManager).getAllRegisteredTypes();
		return beanTypeManager;
	}

}
