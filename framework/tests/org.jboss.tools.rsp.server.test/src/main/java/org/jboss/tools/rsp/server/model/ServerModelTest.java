/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.ServerModelListenerAdapter;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.util.DataLocationSysProp;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServerModelTest {
	private static final DataLocationSysProp dataLocation = new DataLocationSysProp();

	@BeforeClass
	public static void beforeClass() {
		dataLocation.backup().set("ServerModelTest");
	}

	@AfterClass
	public static void afterClass() {
		dataLocation.restore();
	}

	private ServerModel sm;

	@Before
	public void before() {
		this.sm = new ServerModel(mock(IServerManagementModel.class));
	}
	
	@Test
	public void testGibberishFile() {
		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			String contents = "this is not xml at all";
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 0);
		} catch(IOException e) {
			removeFile(s1);
			fail();
		}
	}

	@Test
	public void testMissingServerTypeInFile() {
		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			String contents = getServerStringNoType("abc123"); 
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 0);
		} catch(IOException e) {
			removeFile(s1);
			fail();
		}
	}

	@Test
	public void testMissingServerTypeInModel() {
		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			String contents = getServerString("abc123", "wonka5"); 
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 0);
		} catch(IOException e) {
			removeFile(s1);
			fail();
		}
	}

	

	@Test
	public void testLoadServer() {
		sm.addServerType(mockServerType("wonka5"));
		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			String contents = getServerString("abc123", "wonka5"); 
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 1);
		} catch(IOException e) {
			removeFile(s1);
			fail();
		}
	}
	

	@Test
	public void testLoadServerHandles() {
		sm.addServerType(mockServerType("wonka5"));
		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			String contents = getServerString("abc123", "wonka5"); 
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 1);
			
			assertNotNull(sm.getServerHandles());
			assertEquals(sm.getServerHandles().length, 1);
			ServerHandle sh = sm.getServerHandles()[0];
			assertServerHandle("abc123", "wonka5", sh);
		} catch(IOException e) {
			removeFile(s1);
			fail();
		}
	}
	
	@Test
	public void testLoadMultipleServerHandles() {
		sm.addServerType(mockServerType("wonka1"));
		Path dir = null;
		Path s1 = null, s2 = null, s3 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = createServerFile("s1", getServerString("abc123", "wonka1"), dir);
			s2 = createServerFile("s2", getServerString("abc456", "wonka1"), dir);
			s3 = createServerFile("s3", getServerString("abc789", "wonka1"), dir);
			sm.loadServers(dir.toFile());
			assertEquals(3, sm.getServers().size());
			
			assertNotNull(sm.getServerHandles());
			assertEquals(3, sm.getServerHandles().length);
			ServerHandle sh = sm.getServerHandles()[1];
			assertServerHandle("abc456", "wonka1", sh);
			IServer server = sm.getServer(sh.getId());
			assertEquals("abc456", server.getId());
		} catch(IOException e) {
			removeFile(s1);
			removeFile(s2);
			removeFile(s3);
			if (dir != null && dir.toFile().exists()) {
				dir.toFile().delete();
			}
			fail();
		}
	}

	private void assertServerHandle(String id, String serverTypeId, ServerHandle sh) {
		assertNotNull(sh);
		assertEquals(id, sh.getId());
		assertNotNull(sh.getType());
		ServerType st = sh.getType();
		assertEquals(serverTypeId, st.getId());
	}

	private void removeFile(Path file) {
		if( file != null && file.toFile().exists()) {
			file.toFile().delete();
		}
	}

	private Path createServerFile(String serverFilename, String contents, Path serversDir) throws IOException {
		Path serverFile = serversDir.resolve(serverFilename);
		Files.write(serverFile, contents.getBytes());
		return serverFile;
	}

	@Test
	public void testAddAndRemoveServerType() {
		assertNotNull(sm.getServerTypes());
		assertEquals(0, sm.getServerTypes().length);
		assertNull(sm.getIServerType("wonka5"));
		
		IServerType mock = mockServerType("wonka5");
		sm.addServerType(mock);
		assertNotNull(sm.getServerTypes());
		assertEquals(1, sm.getServerTypes().length);
		assertNotNull(sm.getIServerType("wonka5"));

		sm.removeServerType(mock);
		assertNotNull(sm.getServerTypes());
		assertNull(sm.getIServerType("wonka5"));
		assertEquals(0, sm.getServerTypes().length);
	}
	
	@Test
	public void testLoadAndRemoveServerWithListener() {
		final Boolean[] added = new Boolean[] {new Boolean(false)};
		final Boolean[] removed = new Boolean[] {new Boolean(false)};
		ServerModelListenerAdapter smla = new ServerModelListenerAdapter() {
			@Override
			public void serverAdded(ServerHandle server) {
				added[0] = Boolean.TRUE;
			}
			@Override
			public void serverRemoved(ServerHandle server) {
				removed[0] = Boolean.TRUE;
			}
		}; 
		sm.addServerModelListener(smla);
		sm.addServerType(mockServerType("wonka5"));
		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			String contents = getServerString("abc123", "wonka5"); 
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 1);
			assertNotNull(sm.getServer("abc123"));
			assertTrue(added[0].booleanValue());
			IServer server = sm.getServer("abc123");

			sm.removeServer(server);
			assertEquals(sm.getServers().size(), 0);
			assertNull(sm.getServer("abc123"));
			assertTrue(removed[0].booleanValue());
			assertFalse(s1.toFile().exists());
			
			sm.removeServerModelListener(smla);
			added[0] = false;
			removed[0] = false;
			
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			contents = getServerString("abc123", "wonka5"); 
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 1);
			assertNotNull(sm.getServer("abc123"));
			assertFalse(added[0].booleanValue());
			
			server = sm.getServer("abc123");
			sm.removeServer(server);
			assertEquals(sm.getServers().size(), 0);
			assertNull(sm.getServer("abc123"));
			assertFalse(removed[0].booleanValue());
			assertFalse(s1.toFile().exists());
		} catch(IOException e) {
			removeFile(s1);
			fail();
		}
	}
	
	private String getServerString(String name, String type) {
		String contents = "{id:\"" + name + "\", id-set:\"true\", " + 
				"org.jboss.tools.rsp.server.typeId=\"" + type + "\"}\n";
		return contents;
	}

	private String getServerStringNoType(String name) {
		String contents = "{id:\"" + name + "\", id-set:\"true\"}\n";
		return contents;
	}
	
	private IServerType mockServerType(String typeId) {
		IServerType ist = mock(IServerType.class);
		doReturn(typeId).when(ist).getId();
		IServerDelegate isd = mock(IServerDelegate.class);
		when(ist.createServerDelegate(any(IServer.class))).thenReturn(isd);
		return ist;
	}

}
