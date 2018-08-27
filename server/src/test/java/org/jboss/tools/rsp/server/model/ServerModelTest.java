package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.junit.Test;

public class ServerModelTest {
	
	@Test
	public void testGibberishFile() {
		ServerModel sm = new ServerModel();
		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			String contents = "this is not xml at all";
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 0);
		} catch(IOException | CoreException e) {
			if( s1 != null && s1.toFile().exists()) {
				s1.toFile().delete();
				s1.toFile().getParentFile().delete();
			}
			fail();
		}
	}

	@Test
	public void testMissingServerTypeInFile() {
		ServerModel sm = new ServerModel();
		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			String contents = getServerStringNoType("abc123"); 
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 0);
		} catch(IOException | CoreException e) {
			if( s1 != null && s1.toFile().exists()) {
				s1.toFile().delete();
				s1.toFile().getParentFile().delete();
			}
			fail();
		}
	}

	@Test
	public void testMissingServerTypeInModel() {
		ServerModel sm = new ServerModel();
		Path dir = null;
		Path s1 = null;
		try {
			dir = Files.createTempDirectory("servermodeltest");
			s1 = dir.resolve("s1");
			String contents = getServerString("abc123", "wonka5"); 
			Files.write(s1, contents.getBytes());
			sm.loadServers(dir.toFile());
			assertEquals(sm.getServers().size(), 0);
		} catch(IOException | CoreException e) {
			if( s1 != null && s1.toFile().exists()) {
				s1.toFile().delete();
				s1.toFile().getParentFile().delete();
			}
			fail();
		}
	}

	

	@Test
	public void testLoadServer() {
		ServerModel sm = new ServerModel();
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
		} catch(IOException | CoreException e) {
			if( s1 != null && s1.toFile().exists()) {
				s1.toFile().delete();
				s1.toFile().getParentFile().delete();
			}
			fail();
		}
	}

	
	private String getServerString(String name, String type) {
		String contents = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
				"<server id=\"" + name + "\" id-set=\"true\" " + 
				"org.jboss.tools.rsp.server.typeId=\"" + type + "\"/>\n";
		return contents;
	}

	private String getServerStringNoType(String name) {
		String contents = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
				"<server id=\"" + name + "\" id-set=\"true\"/>\n";
		return contents;
	}
	
	private IServerType mockServerType(String typeId) {
		IServerType ist = mock(IServerType.class);
		doReturn(typeId).when(ist).getId();
		return ist;
	}
	
}
