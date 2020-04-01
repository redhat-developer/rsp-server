/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.test.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.server.core.internal.Base;
import org.jboss.tools.rsp.server.generic.servertype.variables.IDynamicVariable;
import org.jboss.tools.rsp.server.generic.servertype.variables.IValueVariable;
import org.jboss.tools.rsp.server.generic.servertype.variables.ServerStringVariableManager;
import org.jboss.tools.rsp.server.generic.servertype.variables.ServerStringVariableManager.IExternalVariableResolver;
import org.jboss.tools.rsp.server.model.internal.Server;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.junit.Test;

public class ServerStringVariableManagerTest {
	@Test
	public void testMissingResolutionFromServer() {
		IServer server = mock(IServer.class);
		ServerStringVariableManager mgr = new ServerStringVariableManager(server, null);

		IDynamicVariable dynamic = mgr.getDynamicVariable("key1");
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable("key1");
		assertNull(var);
	}

	@Test
	public void testStringResolutionFromServer() {
		IServer server = mock(IServer.class);
		doReturn("stringret").when(server).getAttribute("key1", (String) null);
		ServerStringVariableManager mgr = new ServerStringVariableManager(server, null);

		IDynamicVariable dynamic = mgr.getDynamicVariable("key1");
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable("key1");
		assertNotNull(var);
		assertNotNull(var.getValue());
		assertEquals("stringret", var.getValue());
	}

	@Test
	public void testBoolResolutionFromServer() {
		Server s = new Server(null, mock(IServerManagementModel.class));
		String key = "boolkey";
		((Base) s).setAttribute(key, true);

		ServerStringVariableManager mgr = new ServerStringVariableManager(s, null);

		IDynamicVariable dynamic = mgr.getDynamicVariable(key);
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable(key);
		assertNotNull(var);
		assertNotNull(var.getValue());
		assertEquals("true", var.getValue());
	}

	@Test
	public void testIntResolutionFromServer() {
		Server s = new Server(null, mock(IServerManagementModel.class));
		String key = "intkey";
		((Base) s).setAttribute(key, 5);

		ServerStringVariableManager mgr = new ServerStringVariableManager(s, null);

		IDynamicVariable dynamic = mgr.getDynamicVariable(key);
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable(key);
		assertNotNull(var);
		assertNotNull(var.getValue());
		assertEquals("5", var.getValue());
	}

	@Test
	public void testListResolutionFromServer() {
		Server s = new Server(null, mock(IServerManagementModel.class));
		String key = "listkey";
		((Base) s).setAttribute(key, Arrays.asList(new String[] { "one", "two" }));

		ServerStringVariableManager mgr = new ServerStringVariableManager(s, null);

		IDynamicVariable dynamic = mgr.getDynamicVariable(key);
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable(key);
		assertNull(var);
	}

	@Test
	public void testMapResolutionFromServer() {
		Server s = new Server(null, mock(IServerManagementModel.class));
		Map<String, String> map = new HashMap<String, String>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		String key = "mapKey";
		((Base) s).setAttribute(key, map);

		ServerStringVariableManager mgr = new ServerStringVariableManager(s, null);

		IDynamicVariable dynamic = mgr.getDynamicVariable(key);
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable(key);
		assertNull(var);
	}



	// With an external provider
	
	@Test
	public void testResolutionEmptyServerEmptyExternal() {
		IServer server = mock(IServer.class);
		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				return null;
			}
		};
		ServerStringVariableManager mgr = new ServerStringVariableManager(server, external);

		IDynamicVariable dynamic = mgr.getDynamicVariable("key1");
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable("key1");
		assertNull(var);
	}
	
	@Test
	public void testResolutionEmptyServerPresentExternal() {
		IServer server = mock(IServer.class);
		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				if("key1".equals(key))
					return "key1result";
				return null;
			}
		};
		ServerStringVariableManager mgr = new ServerStringVariableManager(server, external);

		IDynamicVariable dynamic = mgr.getDynamicVariable("key1");
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable("key1");
		assertNotNull(var);
		assertEquals("key1result", var.getValue());
	}

	// With both 
	
	@Test
	public void testStringResolutionPresentServerPresentExternal() {
		IServer server = mock(IServer.class);
		doReturn("key1FromServer").when(server).getAttribute("key1", (String)null);
		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				if("key1".equals(key))
					return "key1FromExternal";
				return null;
			}
		};
		ServerStringVariableManager mgr = new ServerStringVariableManager(server, external);

		IDynamicVariable dynamic = mgr.getDynamicVariable("key1");
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable("key1");
		assertNotNull(var);
		assertNotNull(var.getValue());
		assertEquals("key1FromServer", var.getValue());
	}

	@Test
	public void testBoolResolutionPresentServerPresentExternal() {
		Server s = new Server(null, mock(IServerManagementModel.class));
		String key = "boolkey";
		((Base) s).setAttribute(key, true);

		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				if("boolkey".equals(key))
					return "false";
				return null;
			}
		};
		ServerStringVariableManager mgr = new ServerStringVariableManager(s, external);

		IDynamicVariable dynamic = mgr.getDynamicVariable(key);
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable(key);
		assertNotNull(var);
		assertNotNull(var.getValue());
		assertEquals("true", var.getValue());
	}

	@Test
	public void testIntResolutionServerPresentExternalPresent() {
		Server s = new Server(null, mock(IServerManagementModel.class));
		String key = "intkey";
		((Base) s).setAttribute(key, 5);

		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				if("intkey".equals(key))
					return "0";
				return null;
			}
		};
		ServerStringVariableManager mgr = new ServerStringVariableManager(s, external);

		IDynamicVariable dynamic = mgr.getDynamicVariable(key);
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable(key);
		assertNotNull(var);
		assertNotNull(var.getValue());
		assertEquals("5", var.getValue());
	}

	@Test
	public void testListResolutionFromServerWithExternal() {
		Server s = new Server(null, mock(IServerManagementModel.class));
		String key = "listkey";
		((Base) s).setAttribute(key, Arrays.asList(new String[] { "one", "two" }));

		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				if("listkey".equals(key))
					return "present";
				return null;
			}
		};
		ServerStringVariableManager mgr = new ServerStringVariableManager(s, external);

		IDynamicVariable dynamic = mgr.getDynamicVariable(key);
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable(key);
		assertNotNull(var);
		assertNotNull(var.getValue());
		assertEquals("present", var.getValue());
	}

	@Test
	public void testMapResolutionFromServerWithExternal() {
		Server s = new Server(null, mock(IServerManagementModel.class));
		Map<String, String> map = new HashMap<String, String>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		String key = "mapKey";
		((Base) s).setAttribute(key, map);

		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				if("mapKey".equals(key))
					return "present";
				return null;
			}
		};
		ServerStringVariableManager mgr = new ServerStringVariableManager(s, external);

		IDynamicVariable dynamic = mgr.getDynamicVariable(key);
		assertNull(dynamic);

		IValueVariable var = mgr.getValueVariable(key);
		assertNotNull(var);
		assertNotNull(var.getValue());
		assertEquals("present", var.getValue());
	}


}
