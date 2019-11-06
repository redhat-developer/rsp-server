package org.jboss.tools.rsp.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import org.jboss.tools.rsp.server.persistence.DataLocationCore;
import org.junit.Test;

public class DataLocationCoreTest {
	private Path createSourceFolder() {
		Path src = null;
		try {
			src = Files.createTempDirectory("DataLocationCoreTest_" + System.currentTimeMillis() + "_src");
			Files.write(src.resolve("f1.txt"), "test".getBytes());
			Files.write(src.resolve("f2.txt"), "test".getBytes());
			Files.write(src.resolve("f3.txt"), "test".getBytes());
			Path srcInner = src.resolve("inner");
			srcInner.toFile().mkdirs();
			Files.write(srcInner.resolve("f1.txt"), "test".getBytes());
			Files.write(srcInner.resolve("f2.txt"), "test".getBytes());
			Files.write(srcInner.resolve("f3.txt"), "test".getBytes());
		} catch(IOException ioe) {
			fail();
		}
		return src;
	}
	
	@Test
	public void testCopyFolder() {
		Path src = createSourceFolder();
		Path dest = null;
		try {
			dest = Files.createTempDirectory("DataLocationCoreTest_" + System.currentTimeMillis() + "_dest");
			copyAndVerify(src, dest, 8, false);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testCopyFolderWithUnreadableFile() {
		Path src = createSourceFolder();
		Path dest = null;
		try {
			dest = Files.createTempDirectory("DataLocationCoreTest_" + System.currentTimeMillis() + "_dest");
			Path f2Outter = src.resolve("f2.txt");
			boolean notReadable = f2Outter.toFile().setReadable(false);
			int copiedFiles = (notReadable ? 7 : 8);
			copyAndVerify(src, dest, copiedFiles, true);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void copyAndVerify(Path src, Path dest, int copied, boolean mayThrowException) {
		copyAndVerify(new DataLocationCore(), src, dest, copied, mayThrowException);
	}
	private void copyAndVerify(DataLocationCore dlc, Path src, Path dest, int copied, boolean mayThrowException) {
		if( dlc != null ) {
			try {
				dlc.copyFolder(src, dest);
			} catch (IOException ioe) {
				if (!mayThrowException)
					fail();
			}
		}
		System.out.println("DataLocationCoreTest call:");
		try (Stream<Path> st = Files.walk(dest)) {
			Iterator<Path> stIt = st.iterator();
			int length = 0;
			for (; stIt.hasNext(); ++length) {
				System.out.println("    DataLocationCoreTest stuff: " + stIt.next().toAbsolutePath());
			}
			assertEquals(length, copied);
		} catch (IOException ioe) {
			fail(ioe.getMessage());
		}
	}
	
	@Test
	public void testMigrate() {
		final Path src = createSourceFolder();
		Path dest = null;
		try {
			dest = Files.createTempDirectory("DataLocationCoreTest_" + System.currentTimeMillis() + "_dest");
			final Path dest1 = dest;
			Path f2Outter = src.resolve("f2.txt");
			boolean notReadable = f2Outter.toFile().setReadable(false);
			int copiedFiles = (notReadable ? 7 : 8);
			// Instantiation should migrate it
			DataLocationCore dlc = new DataLocationCore() {
				protected File getLegacy1DefaultDataLocation() {
					return src.toFile();
				}

				protected File getCurrentDefaultDataLocation() {
					return dest1.toFile();
				}
			};
			copyAndVerify(null, src, dest, copiedFiles, true);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	

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

	public static void main(String[] args) {
		for( int i = 1; i <= 100; i++ ) {
			if( i % 3 != 0 && i % 5 != 0 ) {
				System.out.println(i);
			} else {
				if( i % 3 == 0 )
					System.out.print("fizz");
				if( i % 5 == 0 )
					System.out.print("buzz");
				System.out.println();
			}
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
