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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.server.generic.servertype.variables.ServerStringVariableManager;
import org.jboss.tools.rsp.server.generic.servertype.variables.ServerStringVariableManager.IExternalVariableResolver;
import org.jboss.tools.rsp.server.generic.servertype.variables.StringSubstitutionEngine;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.junit.Test;

public class StringSubstitutionEngineTest {

	@Test
	public void testUndefined() {
		String input = "test ${one}";
		IServer server = mock(IServer.class);
		try {
			String substituted = new StringSubstitutionEngine().performStringSubstitution(input, 
					true, true, new ServerStringVariableManager(server, null));
			fail();
		} catch(CoreException ce) {
		}
	}

	@Test
	public void testSimpleDefined() {
		String input = "test ${one}";
		IServer server = mock(IServer.class);
		doReturn("oneVal").when(server).getAttribute("one", (String) null);
		try {
			String substituted = new StringSubstitutionEngine().performStringSubstitution(input, 
					true, true, new ServerStringVariableManager(server, null));
			assertEquals(substituted, "test oneVal");
		} catch(CoreException ce) {
			fail();
		}
	}

	@Test
	public void testMultipleDefined() {
		String input = "test ${one} ${two}";
		IServer server = mock(IServer.class);
		doReturn("oneVal").when(server).getAttribute("one", (String) null);
		doReturn("twoVal").when(server).getAttribute("two", (String) null);
		try {
			String substituted = new StringSubstitutionEngine().performStringSubstitution(input, 
					true, true, new ServerStringVariableManager(server, null));
			assertEquals(substituted, "test oneVal twoVal");
		} catch(CoreException ce) {
			fail();
		}
	}

	@Test
	public void testMultipleWithUndefined() {
		String input = "test ${one} ${two}";
		IServer server = mock(IServer.class);
		doReturn("oneVal").when(server).getAttribute("one", (String) null);
		try {
			String substituted = new StringSubstitutionEngine().performStringSubstitution(input, 
					true, true, new ServerStringVariableManager(server, null));
			fail();
		} catch(CoreException ce) {
		}
	}
	
	@Test
	public void testSimpleExternalDefined() {
		String input = "test ${one}";
		IServer server = mock(IServer.class);
		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				if( "one".equals(key))
					return "oneVal";
				return null;
			}
		};
		try {
			String substituted = new StringSubstitutionEngine().performStringSubstitution(input, 
					true, true, new ServerStringVariableManager(server, external));
			assertEquals(substituted, "test oneVal");
		} catch(CoreException ce) {
			fail();
		}
	}

	@Test
	public void testSimpleExternalUndefined() {
		String input = "test ${one}";
		IServer server = mock(IServer.class);
		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				return null;
			}
		};
		try {
			String substituted = new StringSubstitutionEngine().performStringSubstitution(input, 
					true, true, new ServerStringVariableManager(server, external));
			fail();
		} catch(CoreException ce) {
		}
	}

	@Test
	public void testDefinedServerDefinedExternal() {
		String input = "test ${one} ${two}";
		IServer server = mock(IServer.class);
		doReturn("oneVal").when(server).getAttribute("one", (String)null);
		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				if( key.equals("two"))
					return "twoVal";
				return null;
			}
		};
		try {
			String substituted = new StringSubstitutionEngine().performStringSubstitution(input, 
					true, true, new ServerStringVariableManager(server, external));
			assertEquals(substituted, "test oneVal twoVal");
		} catch(CoreException ce) {
			fail();
		}
	}

	@Test
	public void testServerDefinesBothExternalDefinesBoth() {
		String input = "test ${one} ${two}";
		IServer server = mock(IServer.class);
		doReturn("oneVal").when(server).getAttribute("one", (String)null);
		doReturn("twoVal").when(server).getAttribute("two", (String)null);
		IExternalVariableResolver external = new IExternalVariableResolver() {
			@Override
			public String getNonServerKeyValue(String key) {
				if( key.equals("one"))
					return "oneVal__";
				if( key.equals("two"))
					return "twoVal__";
				return null;
			}
		};
		try {
			String substituted = new StringSubstitutionEngine().performStringSubstitution(input, 
					true, true, new ServerStringVariableManager(server, external));
			assertEquals(substituted, "test oneVal twoVal");
		} catch(CoreException ce) {
			fail();
		}
	}

}
