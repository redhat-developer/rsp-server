package org.jboss.tools.rsp.server.model.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.tools.rsp.server.persistence.DataLocationCore;
import org.junit.Test;

public class DataLocationStoreTest {
	
	@Test
	public void testLockUnlock() {
		try {
			Path p = Files.createTempDirectory("DataLocationStoreTestRoot");
			p.toFile().mkdirs();
			
			Path workspace1 = p.resolve("workspace1");
			workspace1.toFile().mkdirs();
			
			DataLocationCore dlc = new DataLocationCore(workspace1.toFile());
			assertFalse(dlc.isInUse());
			dlc.lock();
			assertTrue(dlc.isInUse());
			dlc.unlock();
			assertFalse(dlc.isInUse());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}


	@Test
	public void testAlreadyLocked() {
		DataLocationCore dlc = null;
		try {
			Path p = Files.createTempDirectory("DataLocationStoreTestRoot2");
			p.toFile().mkdirs();
			
			Path workspace1 = p.resolve("workspace1");
			workspace1.toFile().mkdirs();
			File lock = new File(workspace1.toFile(), ".lock");
			lock.createNewFile();
			dlc = new DataLocationCore(workspace1.toFile());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		assertTrue(dlc.isInUse());
		
		try {
			dlc.lock();
			fail();
		} catch(IOException ioe) {
			// success
		}
		
		try {
			dlc.unlock();
			fail();
		} catch(IOException ioe) {
			// success
		}
		assertTrue(dlc.isInUse());

	}
}
