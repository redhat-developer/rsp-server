package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.model.internal.Server;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.junit.Test;

public class ServerPersistenceListMapTest {
	private class MyServer extends Server {

		public MyServer(File file, IServerManagementModel managementModel) {
			super(file, managementModel);
		}
		
	}

	private IServerManagementModel createMockMgmtModel() {
		return mock(IServerManagementModel.class);
	}
	
	@Test
	public void testLoadDeploymentOptionDefectIssue449() throws IOException {
		Path tmp = Files.createTempFile(getClass().getName(), System.currentTimeMillis() + "");
		MyServer myserver = new MyServer(tmp.toFile(), createMockMgmtModel());
		
		List<String> list1 = Arrays.asList("list1.member1", "list1.member2");
		List<String> list2 = Arrays.asList("list2.member1", "list2.member2");
		Map<String,String> map1 = new HashMap<>();
		Map<String,String> map2 = new HashMap<>();
		map1.put("map1.key1",  "map1.val1");
		map1.put("map1.key2",  "map1.val2");
		map2.put("map2.key1",  "map2.val1");
		map2.put("map2.key2",  "map2.val2");
		
		
		
		myserver.setAttribute("list1", list1);
		myserver.setAttribute("list2", list2);
		myserver.setAttribute("map1", map1);
		myserver.setAttribute("map2", map2);
		
		try {
			String asBytes = myserver.asJson(new NullProgressMonitor());
			JSONMemento memento = JSONMemento.createReadRoot(new ByteArrayInputStream(asBytes.getBytes()));
			
			IMemento lists = memento.getChild("listProperties");
			assertNotNull(lists);
			IMemento[] listChildren = lists.getChildren();
			assertNotNull(listChildren);
			assertEquals(listChildren.length, 2);
			boolean oneNamedList1 = listChildren[0].getNodeName().equals("list1") || 
					listChildren[1].getNodeName().equals("list1");
			boolean oneNamedList2 = listChildren[0].getNodeName().equals("list2") || 
					listChildren[1].getNodeName().equals("list2");
			
			assertTrue(oneNamedList1);
			assertTrue(oneNamedList2);

			IMemento l1Memento = lists.getChild("list1");
			assertNotNull(l1Memento);
			assertEquals(l1Memento.getString("value0"), "list1.member1");
			assertEquals(l1Memento.getString("value1"), "list1.member2");
			
			IMemento l2Memento = lists.getChild("list2");
			assertNotNull(l2Memento);
			assertEquals(l2Memento.getString("value0"), "list2.member1");
			assertEquals(l2Memento.getString("value1"), "list2.member2");
			
			
			IMemento maps = memento.getChild("mapProperties");
			assertNotNull(maps);
			IMemento[] mapChildren = maps.getChildren();
			assertNotNull(mapChildren);
			assertEquals(mapChildren.length, 2);
			boolean oneNamedMap1 = mapChildren[0].getNodeName().equals("map1") || 
					mapChildren[1].getNodeName().equals("map1");
			boolean oneNamedMap2 = mapChildren[0].getNodeName().equals("map2") || 
					mapChildren[1].getNodeName().equals("map2");
			
			assertTrue(oneNamedMap1);
			assertTrue(oneNamedMap2);

			IMemento m1Memento = maps.getChild("map1");
			assertNotNull(m1Memento);
			assertEquals(m1Memento.getString("map1.key1"), "map1.val1");
			assertEquals(m1Memento.getString("map1.key2"), "map1.val2");
			
			IMemento m2Memento = maps.getChild("map2");
			assertNotNull(m2Memento);
			assertEquals(m2Memento.getString("map2.key1"), "map2.val1");
			assertEquals(m2Memento.getString("map2.key2"), "map2.val2");
		} catch (CoreException e) {
			fail();
		}
	}
	
}
