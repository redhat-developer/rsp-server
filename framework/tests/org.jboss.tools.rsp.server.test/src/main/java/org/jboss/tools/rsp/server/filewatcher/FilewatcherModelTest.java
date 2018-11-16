/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.filewatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherEventListener;
import org.junit.Test;

public class FilewatcherModelTest {

	public static class TestableFileWatcherService extends FileWatcherService {
		public TestableFileWatcherService() {
			super();
		}
		/*
		 * Getters For testing
		 */
		@Override
		public WatchService getWatchService() {
			return super.getWatchService();
		}
		@Override
		public HashMap<Path, List<RegistrationRequest>> getRequests() {
			return super.getRequests();
		}
		@Override
		public ExecutorService getExecutor() {
			return super.getExecutor();
		}
		@Override
		public Map<Path, WatchKey> getSubscriptions() {
			return super.getSubscriptions();
		}
	}
	
	@Test
	public void testBasicStartStop() {
		TestableFileWatcherService service = new TestableFileWatcherService();
		assertNotNull(service.getRequests());
		assertNotNull(service.getSubscriptions());
		assertNull(service.getWatchService());
		assertNull(service.getExecutor());
		
		service.start();
		assertFalse(service.getExecutor().isShutdown());
		assertNotNull(service.getWatchService());
		
		service.stop();
		assertNull(service.getExecutor());
		assertNull(service.getWatchService());
	}

	
	@Test
	public void testBasicFolderSubscription() {
		TestableFileWatcherService service = new TestableFileWatcherService();
		assertNotNull(service.getRequests());
		assertNotNull(service.getSubscriptions());
		assertNull(service.getWatchService());
		assertNull(service.getExecutor());
		
		service.start();
		assertFalse(service.getExecutor().isShutdown());
		assertNotNull(service.getWatchService());

		
		assertNotNull(service.getRequests());
		assertNotNull(service.getSubscriptions());
		assertEquals(0, service.getRequests().size());
		assertEquals(0, service.getSubscriptions().size());
		try {
			IFileWatcherEventListener listener = (events) -> System.out.println(events);
			Path root = Files.createTempDirectory(getClass().getName() + "_1");

			service.addFileWatcherListener(root, listener, false);
			verifyModel(service, root, 1, 1, root.getNameCount()+1);
			
			service.removeFileWatcherListener(root, listener);
			verifyModel(service, root, 0, 0, 0);
		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}
	
	@Test
	public void testBasicFileSubscription() {
		TestableFileWatcherService service = new TestableFileWatcherService();
		service.start();
		
		try {
			IFileWatcherEventListener listener = (events) -> System.out.println(events);
			Path root = Files.createTempDirectory(getClass().getName() + "_2");
			Path childFile = root.resolve("out.txt");
			Files.write(childFile, "test".getBytes());
			
			verifyModel(service, null, 0, 0, 0);
			service.addFileWatcherListener(childFile, listener, false);
			verifyModel(service, childFile, 1, 1, root.getNameCount() + 1);
			
			service.removeFileWatcherListener(childFile, listener);
			verifyModel(service, childFile, 0,0,0);

		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}

	@Test
	public void testBasicSiblingSubscriptions() {
		TestableFileWatcherService service = new TestableFileWatcherService();
		service.start();
		try {
			IFileWatcherEventListener listener = (events) -> System.out.println(events);
			
			Path root = Files.createTempDirectory(getClass().getName() + "_3");
			Path nested1 = root.resolve("nested1");
			Path nested2 = root.resolve("nested2");
			Path nested3 = root.resolve("nested3");
			nested1.toFile().mkdirs();
			nested2.toFile().mkdirs();
			nested3.toFile().mkdirs();
			
			int tmpDirNameCount = root.getNameCount() + 1;

			service.addFileWatcherListener(nested1, listener, false);
			verifyModel(service, nested1, 1, 1, tmpDirNameCount+1);

			service.addFileWatcherListener(nested2, listener, false);
			verifyModel(service, nested2, 2, 1, tmpDirNameCount+2);

			service.addFileWatcherListener(nested3, listener, false);
			verifyModel(service, nested3, 3, 1, tmpDirNameCount+3);

			// Remove them
			service.removeFileWatcherListener(nested2, listener);
			verifyModel(service, nested2, 2, 0, tmpDirNameCount+2);

			service.removeFileWatcherListener(nested1, listener);
			verifyModel(service, nested1, 1, 0, tmpDirNameCount+1);

			service.removeFileWatcherListener(nested3, listener);
			verifyModel(service, nested3, 0,0,0);
		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}

	@Test
	public void testDuplicateRegistration() {
		TestableFileWatcherService service = new TestableFileWatcherService();
		service.start();
		try {
			IFileWatcherEventListener listener = (events) -> System.out.println(events);
			
			Path root = Files.createTempDirectory(getClass().getName() + "_4");
			int tmpDirNameCount = root.getNameCount() + 1;

			service.addFileWatcherListener(root, listener, false);
			assertEquals(1, service.getRequests().size());
			assertEquals(1, service.getRequests().get(root).size());
			assertEquals(tmpDirNameCount, service.getSubscriptions().size());

			service.addFileWatcherListener(root, listener, false);
			assertEquals(1, service.getRequests().size());
			assertEquals(1, service.getRequests().get(root).size());
			assertEquals(tmpDirNameCount, service.getSubscriptions().size());

			service.removeFileWatcherListener(root, listener);
			assertEquals(0, service.getRequests().size());
			assertNull(service.getRequests().get(root));
			assertEquals(0, service.getSubscriptions().size());

		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}

	@Test
	public void testDuplicateRegistrationUniqueListener() {
		TestableFileWatcherService service = new TestableFileWatcherService();
		service.start();
		try {
			IFileWatcherEventListener listener1 = (events) -> System.out.println(events);
			IFileWatcherEventListener listener2 = (events) -> System.out.println("Hey " + events);
			
			Path root = Files.createTempDirectory(getClass().getName() + "_5");
			int tmpDirNameCount = root.getNameCount() + 1;

			service.addFileWatcherListener(root, listener1, false);
			verifyModel(service, root, 1, 1, tmpDirNameCount);

			service.addFileWatcherListener(root, listener2, false);
			// requests only has 1 key (the path) 
			// but it's value is a list w 2 request objects
			verifyModel(service, root, 1, 2, tmpDirNameCount);

			// Remove 
			service.removeFileWatcherListener(root, listener2);
			verifyModel(service, root, 1, 1, tmpDirNameCount);

			service.removeFileWatcherListener(root, listener1);
			verifyModel(service, root, 0,0,0);
		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}

	

	@Test
	public void testBasicRecursiveSubscriptions() {
		TestableFileWatcherService service = new TestableFileWatcherService();
		service.start();
		try {
			IFileWatcherEventListener listener = (events) -> System.out.println(events);
			
			Path root = Files.createTempDirectory(getClass().getName() + "_6");
			Path nested1 = root.resolve("nested1");
			Path nested2 = root.resolve("nested2");
			Path nested3 = root.resolve("nested3");
			nested1.toFile().mkdirs();
			nested2.toFile().mkdirs();
			nested3.toFile().mkdirs();
			
			int tmpDirNameCount = root.getNameCount() + 1;

			service.addFileWatcherListener(root, listener, true);
			verifyModel(service, root, 1, 1, tmpDirNameCount+3);

			// Remove them
			service.removeFileWatcherListener(root, listener);
			assertEquals(0, service.getRequests().size());
			assertEquals(0, service.getSubscriptions().size());
			verifyModel(service, root, 0,0,0);
		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}
	

	@Test
	public void testIdenticalRecursiveSubscriptions() {
		TestableFileWatcherService service = new TestableFileWatcherService();
		service.start();
		try {
			IFileWatcherEventListener listener1 = (events) -> System.out.println(events);
			IFileWatcherEventListener listener2 = (events) -> System.out.println("Yeah! " + events);
			
			Path root = Files.createTempDirectory(getClass().getName() + "_6");
			Path nested1 = root.resolve("nested1");
			Path nested2 = root.resolve("nested2");
			Path nested3 = root.resolve("nested3");
			nested1.toFile().mkdirs();
			nested2.toFile().mkdirs();
			nested3.toFile().mkdirs();
			
			int tmpDirNameCount = root.getNameCount() + 1;

			service.addFileWatcherListener(root, listener1, true);
			verifyModel(service, root, 1, 1, tmpDirNameCount+3);

			service.addFileWatcherListener(root, listener2, true);
			verifyModel(service, root, 1, 2, tmpDirNameCount+3);

			// Remove them
			service.removeFileWatcherListener(root, listener1);
			verifyModel(service, root, 1, 1, tmpDirNameCount+3);

			service.removeFileWatcherListener(root, listener2);
			verifyModel(service, root, 0,0,0);
		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}

	
	
	@Test
	public void testOverlappingRecursiveSubscriptions() {
		TestableFileWatcherService service = new TestableFileWatcherService();
		service.start();
		try {
			IFileWatcherEventListener listener1 = (events) -> System.out.println(events);
			
			Path root = Files.createTempDirectory(getClass().getName() + "_7");
			Path nested1 = root.resolve("nested1");
			Path nested2 = root.resolve("nested2");
			Path nested3 = root.resolve("nested3");
			Path nested3a = nested3.resolve("3a");
			Path nested3b = nested3.resolve("3b");
			Path nested3c = nested3.resolve("3c");
			
			nested1.toFile().mkdirs();
			nested2.toFile().mkdirs();
			nested3.toFile().mkdirs();
			nested3a.toFile().mkdirs();
			nested3b.toFile().mkdirs();
			nested3c.toFile().mkdirs();
			
			int tmpDirNameCount = root.getNameCount() + 1;
			int totalSubs = 6 + tmpDirNameCount;
			int subsNested3 = 4 + tmpDirNameCount;
			int subsNested2 = 1 + tmpDirNameCount;

			service.addFileWatcherListener(nested3, listener1, true);
			verifyModel(service, nested3, 1, 1, subsNested3);
			
			service.addFileWatcherListener(root, listener1, true);
			verifyModel(service, root, 2, 1, totalSubs);
			
			service.removeFileWatcherListener(nested3, listener1);
			verifyModel(service, nested3, 1, 0, totalSubs);
			verifyModel(service, root, 1, 1, totalSubs);

			
			service.addFileWatcherListener(nested2, listener1, true);
			verifyModel(service, nested2, 2, 1, totalSubs);

			service.removeFileWatcherListener(root, listener1);
			verifyModel(service, nested3, 1, 0, subsNested2);
			verifyModel(service, root, 1,0,subsNested2);
			verifyModel(service, nested2, 1,1,subsNested2);
			
			service.removeFileWatcherListener(nested2, listener1);
			verifyModel(service, nested2, 0,0,0);

		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}

	private void verifyModel(TestableFileWatcherService service, Path path, 
			int uniquePaths, int requestForPath, int watchedFolders) {
		assertNotNull(service.getRequests());
		assertNotNull(service.getSubscriptions());
		assertEquals(uniquePaths, service.getRequests().size());
		assertEquals(watchedFolders, service.getSubscriptions().size());
		if( path != null ) {
			if( requestForPath == 0 )
				assertNull(service.getRequests().get(path));
			else
				assertEquals(requestForPath, service.getRequests().get(path).size());
		}
	}
	
}
