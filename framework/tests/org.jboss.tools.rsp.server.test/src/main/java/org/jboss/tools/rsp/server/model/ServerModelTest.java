/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.assertj.core.data.MapEntry;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.IServerModel;
import org.jboss.tools.rsp.server.spi.model.ServerModelListenerAdapter;
import org.jboss.tools.rsp.server.spi.servertype.AbstractServerType;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
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

	private static final String BONNET_COLOR_ATTRIBUTE_ID = "BonnetColor";
	private static final String HAS_BEARD_ATTRIBUTE_ID = "HasBeard";
	protected static final String TROUSERS_COLOR_ATTRIBUTE_ID = "throusersColor";
	protected static final String EAT_APPLE_PIES_ATTRIBUTE_ID = "eatApplePies";
	private static final Map<String, Attribute> ATTRIBUTES = new HashMap<String, Attribute>() {{
		put(BONNET_COLOR_ATTRIBUTE_ID, new Attribute(ServerManagementAPIConstants.ATTR_TYPE_STRING, null, null));
		put(HAS_BEARD_ATTRIBUTE_ID, new Attribute(ServerManagementAPIConstants.ATTR_TYPE_BOOL, null, null));
	}};
	
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

	@Test
	public void shouldNotCreateNewServerWithExistingId() {
		// given
		String serverId = "papa-smurf";
		IServer server = mockServer(serverId);
		IServerModel serverModel = new TestableServerModel(mock(IServerManagementModel.class), 
				Collections.emptyMap(), 
				new HashMap<String, IServer>() {{
					put(serverId, server);
				}}, 
				Collections.emptyMap());
		String serverTypeId = "smurfs";
		// when
		CreateServerResponse response = serverModel.createServer(serverTypeId, serverId, Collections.emptyMap());
		// then
		assertThat(response.getStatus().isOK()).isFalse();
	}

	@Test
	public void shouldNotCreateNewServerForUnknownServerType() {
		// given
		TestableServerModel serverModelSpy = new TestableServerModel(mock(IServerManagementModel.class), 
				Collections.emptyMap(),
				Collections.emptyMap(), 
				Collections.emptyMap());
		// when
		CreateServerResponse response = serverModelSpy.createServer("smurfs", "papa-smurf", Collections.emptyMap());
		// then
		assertThat(response.getStatus().isOK()).isFalse();
	}

	@Test
	public void shouldNotCreateNewServerWithEmptyAttribute() {
		// given
		String serverTypeId = "smurfs";
		IServerModel serverModel = createServerModel(serverTypeId);
		// when
		CreateServerResponse response = serverModel.createServer(serverTypeId, "papa-smurf", 
				new HashMap<String, Object>() {{
					put(BONNET_COLOR_ATTRIBUTE_ID, "");
					put(HAS_BEARD_ATTRIBUTE_ID, Boolean.TRUE);
				}});
		// then
		assertThat(response.getStatus().isOK()).isFalse();
		assertThat(response.getInvalidKeys()).containsExactly(BONNET_COLOR_ATTRIBUTE_ID);
	}

	@Test
	public void shouldReportInvalidAttributesInOrderDefinedInServerType() {
		// given
		String serverTypeId = "smurfs";
		Map<String, Attribute> attributes = new LinkedHashMap<String, Attribute>() {{
			put(HAS_BEARD_ATTRIBUTE_ID, new Attribute(ServerManagementAPIConstants.ATTR_TYPE_BOOL, null, null));
			put(BONNET_COLOR_ATTRIBUTE_ID, new Attribute(ServerManagementAPIConstants.ATTR_TYPE_STRING, null, null));
			put(TROUSERS_COLOR_ATTRIBUTE_ID, new Attribute(ServerManagementAPIConstants.ATTR_TYPE_STRING, null, null));
			put(EAT_APPLE_PIES_ATTRIBUTE_ID, new Attribute(ServerManagementAPIConstants.ATTR_TYPE_INT, null, null));
		}};
		IServerType serverType = mockServerType(serverTypeId, attributes);
		IServerModel serverModel = createServerModel(serverType);
		// when
		CreateServerResponse response = serverModel.createServer(serverTypeId, "papa-smurf", 
				new HashMap<String, Object>() {{
					put(BONNET_COLOR_ATTRIBUTE_ID, "Red");
				}});
		// then
		assertThat(response.getStatus().isOK()).isFalse();
		assertThat(response.getInvalidKeys()).containsExactly(
				HAS_BEARD_ATTRIBUTE_ID, 
				TROUSERS_COLOR_ATTRIBUTE_ID, 
				EAT_APPLE_PIES_ATTRIBUTE_ID);
	}

	@Test
	public void shouldNotCreateNewServerWithAttributeInWrongType() {
		// given
		String serverTypeId = "smurfs";
		IServerModel serverModel = createServerModel(serverTypeId);
		// when
		CreateServerResponse response = serverModel.createServer(serverTypeId, "papa-smurf", 
				new HashMap<String, Object>() {{
					put(BONNET_COLOR_ATTRIBUTE_ID, "Red");
					put(HAS_BEARD_ATTRIBUTE_ID, Integer.valueOf("100"));
				}});
		// then
		assertThat(response.getStatus().isOK()).isFalse();
		assertThat(response.getInvalidKeys()).containsExactly(HAS_BEARD_ATTRIBUTE_ID);
	}

	@Test
	public void shouldNotCreateNewServerWithNullAttribute() {
		// given
		String serverTypeId = "smurfs";
		IServerModel serverModel = createServerModel(serverTypeId);
		// when
		CreateServerResponse response = serverModel.createServer(serverTypeId, "papa-smurf", 
				new HashMap<String, Object>() {{
					put(BONNET_COLOR_ATTRIBUTE_ID, "");
					put(HAS_BEARD_ATTRIBUTE_ID, Boolean.TRUE);
				}});
		// then
		assertThat(response.getStatus().isOK()).isFalse();
		assertThat(response.getInvalidKeys()).containsExactly(BONNET_COLOR_ATTRIBUTE_ID);
	}

	@Test
	public void shouldNotCreateNewServerIfDelegateDoesntValidate() {
		// given
		CreateServerValidation validation = new CreateServerValidation(Status.CANCEL_STATUS, null);
		IServerDelegate delegate = mockServerDelegate(validation);
		IServerType serverType = mockServerType("smurfs", delegate, ATTRIBUTES);
		IServerModel serverModel = createServerModel(serverType);
		// when
		CreateServerResponse response = serverModel.createServer(serverType.getId(), "papa-smurf", 
				new HashMap<String, Object>() {{
					put(BONNET_COLOR_ATTRIBUTE_ID, "Red");
					put(HAS_BEARD_ATTRIBUTE_ID, Boolean.TRUE);
				}});
		// then
		assertThat(response.getStatus().isOK()).isFalse();
		assertThat(response.getInvalidKeys()).isEmpty();
	}

	@Test
	public void shouldCreateNewServerIfAllValidationIsOK() {
		// given
		CreateServerValidation validation = new CreateServerValidation(Status.OK_STATUS, null);
		IServerDelegate delegate = mockServerDelegate(validation);
		IServerType serverType = mockServerType("smurfs", delegate, ATTRIBUTES);
		IServerModel serverModel = createServerModel(serverType);
		// when
		CreateServerResponse response = serverModel.createServer("smurfs", "papa-smurf", 
				new HashMap<String, Object>() {{
					put(BONNET_COLOR_ATTRIBUTE_ID, "Red");
					put(HAS_BEARD_ATTRIBUTE_ID, Boolean.TRUE);
				}});
		// then
		assertThat(response.getStatus().isOK()).isTrue();
		assertThat(response.getInvalidKeys()).isEmpty();
	}

	@SuppressWarnings("serial")
	@Test
	public void shouldPreserveOrderingOfRequiredServerAttributes() {
		// given
		final Attribute stringAttribute = new Attribute(ServerManagementAPIConstants.ATTR_TYPE_STRING, null, null);
		final Attribute intAttribute = new Attribute(ServerManagementAPIConstants.ATTR_TYPE_INT, null, null);
		final Attribute booleanAttribute = new Attribute(ServerManagementAPIConstants.ATTR_TYPE_BOOL, null, null);
		final IServerType testType = new AbstractServerType("id", "name", "description") {

			@Override
			public Attributes getRequiredAttributes() {
				final LinkedHashMap<String, Attribute> attributesMap = new LinkedHashMap<String, Attribute>() {{
					put("chimpansee", stringAttribute);
					put("gorilla", booleanAttribute);
					put("orangutan", intAttribute);
				}};
				return new Attributes(attributesMap);
			}

			@Override
			public IServerDelegate createServerDelegate(IServer server) {
				return null;
			}
		};
		sm.addServerType(testType);
		Attributes required = sm.getRequiredAttributes(testType);
		assertThat(required.getAttributes()).containsExactly(
				MapEntry.entry("chimpansee", stringAttribute),
				MapEntry.entry("gorilla", booleanAttribute),
				MapEntry.entry("orangutan", intAttribute));
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

	private IServerModel createServerModel(String serverTypeId) {
		return createServerModel(mockServerType(serverTypeId, ATTRIBUTES));
	}

	private IServerModel createServerModel(IServerType serverType) {
		IServerModel serverModel = new TestableServerModel(mock(IServerManagementModel.class), 
				new HashMap<String, IServerType>() {{
					put(serverType.getId(), serverType);
				}}, 
				new HashMap<>(), 
				new HashMap<>());
		return serverModel;		
	}

	private IServerType mockServerType(String typeId) {
		return mockServerType(typeId, Collections.emptyMap());
	}

	private IServerType mockServerType(String typeId, Map<String, Attribute> attributes) {
		return mockServerType(typeId, mock(IServerDelegate.class), attributes);
	}

	private IServerType mockServerType(String typeId, IServerDelegate delegate, Map<String, Attribute> attributes) {
		IServerType type = mock(IServerType.class);
		doReturn(typeId).when(type).getId();
		doReturn(delegate).when(type).createServerDelegate(any(IServer.class));
		Attributes attrs = mock(Attributes.class);
		doReturn(attributes).when(attrs).getAttributes();
		doReturn(attrs).when(type).getRequiredAttributes();
		return type;
	}

	private IServer mockServer(String serverId) {
		IServer server = mock(IServer.class);
		doReturn(serverId).when(server).getId();
		return server;
	}

	private IServerDelegate mockServerDelegate(CreateServerValidation validation) {
		IServerDelegate delegate = mock(IServerDelegate.class);
		doReturn(validation).when(delegate).validate();
		return delegate;
	}

	public class TestableServerModel extends ServerModel {

		public TestableServerModel(IServerManagementModel managementModel) {
			super(managementModel);
		}

		public TestableServerModel(IServerManagementModel managementModel, 
				Map<String, IServerType> serverTypes, Map<String, IServer> servers, Map<String, IServerDelegate> delegates) {
			super(managementModel, serverTypes, servers, delegates);
		}

		@Override
		public void addServer(IServer server, IServerDelegate delegate) {
			super.addServer(server, delegate);
		}
	}
	
}
