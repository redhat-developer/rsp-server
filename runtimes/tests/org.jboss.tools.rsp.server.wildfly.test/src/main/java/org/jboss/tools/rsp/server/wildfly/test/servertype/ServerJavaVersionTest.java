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

import java.util.Arrays;

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
	private static final String JAVA_13 = "13.0.1";

	private static final String[] ALL_JAVA = {
			JAVA_2, JAVA_3, JAVA_4, JAVA_5, JAVA_6, JAVA_7, JAVA_8, JAVA_9, JAVA_10, JAVA_11, JAVA_12, JAVA_13
	};

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

			assertNotNull("Servertype " + serverType + " has no minimum java", t.getMinimumJavaVersionString());
			assertNotNull("Servertype " + serverType + " has no maximum java", t.getMaximumJavaVersionString());

		}
	}

	@Test
	public void testAS32() {
		serverTypeTest(IServerConstants.SERVER_AS_32,
				JAVA_3, JAVA_4, JAVA_5);
	}

	@Test
	public void testAS40() {
		serverTypeTest(IServerConstants.SERVER_AS_40, 
				JAVA_4, JAVA_5);

	}

	@Test
	public void testAS42() {
		serverTypeTest(IServerConstants.SERVER_AS_42, 
				JAVA_5,JAVA_6);
	}

	@Test
	public void testAS50() {
		serverTypeTest(IServerConstants.SERVER_AS_50, 
				JAVA_5, JAVA_6);
	}

	@Test
	public void testAS51() {
		serverTypeTest(IServerConstants.SERVER_AS_51, 
				JAVA_5,JAVA_6);
	}

	@Test
	public void testAS60() {
		serverTypeTest(IServerConstants.SERVER_AS_60, 
				JAVA_6,JAVA_7,JAVA_8);
	}

	@Test
	public void testAS70() {
		/** 
		 * java 6 - 8
		 *
		 * @see https://docs.jboss.org/author/display/AS7/Getting+Started+Guide
		 */
		serverTypeTest(IServerConstants.SERVER_AS_70, 
				JAVA_6,JAVA_7);
	}

	@Test
	public void testAS71() {
		serverTypeTest(IServerConstants.SERVER_AS_71, 
				JAVA_6,JAVA_7,JAVA_8);
	}

	@Test
	public void testWildFly80() {
		/** 
		 * java 7 - 8
		 *  
		 * @see https://docs.jboss.org/author/display/WFLY8/Getting+Started+Guide
		 */
		serverTypeTest(IServerConstants.SERVER_WILDFLY_80, 
				JAVA_7,JAVA_8);
	}

	@Test
	public void testWildFly90() {
		/** 
		 * java 7 - 8
		 */
		serverTypeTest(IServerConstants.SERVER_WILDFLY_90, 
				JAVA_7,JAVA_8);
	}

	@Test
	public void testWildFly100() {
		/**
		 * java 8
		 * 
		 * @see https://docs.jboss.org/author/display/WFLY10/Getting+Started+Guide
		 */
		serverTypeTest(IServerConstants.SERVER_WILDFLY_100, 
				JAVA_8);
	}

	@Test
	public void testWildFly110() {
		/**
		 * java 8, 9
		 * 
		 * @see https://issues.jboss.org/browse/WFLY-3854
		 */
		serverTypeTest(IServerConstants.SERVER_WILDFLY_110, 
				JAVA_8,JAVA_9);
	}

	@Test
	public void testWildFly120() {
		/**
		 * java 8, 9
		 * 
		 * @see https://wildfly.org/news/2018/02/28/WildFly12-Final-Released/
		 */
		serverTypeTest(IServerConstants.SERVER_WILDFLY_120, 
				JAVA_8, JAVA_9, JAVA_10);
	}

	@Test
	/**
	 * java 8 - 10
	 * 
	 * @see https://wildfly.org/news/2018/05/30/WildFly13-Final-Released/
	 */
	public void testWildFly130() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_130, 
				JAVA_8, JAVA_9, JAVA_10);
	}

	@Test
	public void testWildFly140() {
		serverTypeTest(IServerConstants.SERVER_WILDFLY_140, 
				JAVA_8, JAVA_9, JAVA_10);
	}

	@Test
	public void testWildFly150() {
		/**
		 * java 8 - 11
		 * 
		 * @see https://wildfly.org/news/2018/12/13/WildFly15-Final-Released/
		 */
		serverTypeTest(IServerConstants.SERVER_WILDFLY_150, 
				JAVA_8, JAVA_9, JAVA_10, JAVA_11);
	}

	@Test
	public void testWildFly160() {
		/**
		 * java 8 - 12
		 * 
		 * @see https://wildfly.org/news/2019/02/27/WildFly16-Final-Released/
		 */
		serverTypeTest(IServerConstants.SERVER_WILDFLY_160, 
				JAVA_8, JAVA_9, JAVA_10, JAVA_11, JAVA_12);
	}

	@Test
	public void testWildFly170() {
		/**
		 * java 8 - 13
		 * 
		 * @see https://wildfly.org/news/2019/06/10/WildFly17-Final-Released/
		 */
		serverTypeTest(IServerConstants.SERVER_WILDFLY_170, 
				JAVA_8, JAVA_9, JAVA_10, JAVA_11, JAVA_12, JAVA_13);
	}

	@Test
	public void testEAP43() {
		serverTypeTest(IServerConstants.SERVER_EAP_43, 
				JAVA_4, JAVA_5);
	}

	@Test
	public void testEAP50() {
		serverTypeTest(IServerConstants.SERVER_EAP_50, 
				JAVA_6, JAVA_7, JAVA_8);
	}

	@Test
	public void testEAP60() {
		/**
		 * java 6 - 8
		 * 
		 * @see https://access.redhat.com/articles/111663
		 */
		serverTypeTest(IServerConstants.SERVER_EAP_60, 
				JAVA_6, JAVA_7, JAVA_8);
	}

	@Test
	public void testEAP61() {
		/**
		 * java 6 - 8
		 * 
		 * @see https://access.redhat.com/articles/111663
		 */
		serverTypeTest(IServerConstants.SERVER_EAP_61, 
				JAVA_6, JAVA_7, JAVA_8);
	}

	@Test
	public void testEAP70() {
		/**
		 * java 8
		 * 
		 * @see https://access.redhat.com/articles/2026253
		 */
		serverTypeTest(IServerConstants.SERVER_EAP_70, 
				JAVA_8);
	}

	@Test
	public void testEAP71() {
		/**
		 * java 8
		 * 
		 * @see https://access.redhat.com/articles/2026253
		 */
		serverTypeTest(IServerConstants.SERVER_EAP_71, 
				JAVA_8);
	}

	@Test
	public void testEAP72() {
		/**
		 * java 8 - 11
		 * 
		 * @see https://access.redhat.com/articles/2026253
		 */
		serverTypeTest(IServerConstants.SERVER_EAP_72, 
				JAVA_8,JAVA_9, JAVA_10, JAVA_11);
	}

	private void serverTypeTest(String typeId, String... success) {
		String serverType = typeId;
		IServer s = createServer(serverType);
		ExtendedServerPropertiesAdapterFactory fact = new ExtendedServerPropertiesAdapterFactory();
		ServerExtendedProperties props = fact.getExtendedProperties(s);
		assertNotNull("Server " + serverType + " has no properties,", props);
		assertTrue(props instanceof JBossExtendedProperties);
		JBossExtendedProperties t = (JBossExtendedProperties) props;

		String min = t.getMinimumJavaVersionString();
		String max = t.getMaximumJavaVersionString();
		
		String[] fails = allBut(success);
		for( int i = 0; i < fails.length; i++ ) {
			assertFalse(fails[i] + " should not be compatible but server is supporting range " + min + " - " + max,
					isJavaCompatible(fails[i], min, max));
		}

		for( int i = 0; i < success.length; i++ ) {
			assertTrue(success[i] + " should be compatible but server is supporting " + min + " - " + max,
					isJavaCompatible(success[i], min, max));
		}
	}

	private boolean isJavaCompatible(String test, String min, String max) {
		return 	VersionComparisonUtility.isJavaCompatible(test, min, max);
	}

	private String[] allBut(String[] excluded) {
		return Arrays.stream(ALL_JAVA)
			.filter(version -> Arrays.stream(excluded).noneMatch(ex -> ex.equals(version)))
			.toArray(String[]::new);
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
