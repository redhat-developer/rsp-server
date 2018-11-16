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
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.tools.rsp.server.filewatcher.FilewatcherModelTest.TestableFileWatcherService;
import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherEventListener;
import org.junit.Test;

public class FilewatcherModificationsTest {

	private static boolean DEBUG = false;
	private static void debug(String msg) {
		if( DEBUG ) {
			System.out.println(msg);
		}
	}
	
	@Test
	public void testSimpleEvent() {
		System.out.println("\n\n*****testSimpleEvent");
		FileWatcherServiceWithLatches service = new FileWatcherServiceWithLatches();
		service.start();
		service.reset();
		try {
			IFileWatcherEventListener listener1 = (events2) -> System.out.println(events2);
			
			Path root = Files.createTempDirectory(getClass().getName() + "_1");
			Path childFile = root.resolve("out.txt");
			service.addFileWatcherListener(root, listener1, true);
			
			Files.write(childFile, "test".getBytes());
			service.waitOnLatches();

			// Now verify the events
			assertTrue(matchingEventFound(childFile, 
					StandardWatchEventKinds.ENTRY_CREATE, new ArrayList<>(service.events)));
			service.reset();

			Files.write(childFile, "test2".getBytes());
			service.waitOnLatches();

			// Now verify the events
			assertTrue(matchingEventFound(childFile, 
					StandardWatchEventKinds.ENTRY_MODIFY, new ArrayList<>(service.events)));
			
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

	/*
	 * This test verifies that if someone slowly creates a folder, 
	 * registers to listen to it, deletes the folder, recreates the folder, 
	 * and adds a file to that folder, we receive all the proper events. 
	 * 
	 * The problem with this test is that if you create the folder
	 * and write the inner file to it immediately, we won't be registered 
	 * to listen to that that folder when the inner file is written, and 
	 * so we will get no event for it. 
	 * 
	 * This means when a folder is created, we might need to traverse 
	 * that folder and propagate "fake" events to the IFileWatcherEventListener
	 * for all the files we find in that folder at the time we receive its 
	 * creation event. 
	 * 
	 * This test shows that if you do each step, and wait for it to complete, 
	 * and wait for a new registration, we receive all expected events
	 * from the model (ie that we're listening to all the right folders at 
	 * the right time).  
	 * 
	 * However it still cannot receive events for a 
	 * mkdir + writeFile made in quick succession. So our service
	 * will need to make these events up somehow. 
	 */
	@Test
	public void testSubscribeDeleteRecreateEvent() {
		System.out.println("\n\n*****testSubscribeDeleteRecreateEvent");

		FileWatcherServiceWithLatches service = new FileWatcherServiceWithLatches();
		service.start();
		service.reset();
		try {
			Path root = Files.createTempDirectory(getClass().getName() + "_2");
			Path childFile = root.resolve("out.txt");

			// Subscribe to folder/out.txt, which registers 
			// listeners to all folders above in tree
			IFileWatcherEventListener listener1 = (events2) -> System.out.println(events2);
			service.addFileWatcherListener(root, listener1, true);
			
			assertTrue(root.toFile().delete());
			service.waitOnLatches();
			
			// Now verify we receive an event for the parent folder delete
			assertTrue(matchingEventFound(root, 
					StandardWatchEventKinds.ENTRY_DELETE, new ArrayList<>(service.events)));
			service.reset();

			// Now verify we receive an event for the parent folder mkdir
			root.toFile().mkdirs();
			service.waitOnLatches();
			assertTrue(matchingEventFound(root, 
					StandardWatchEventKinds.ENTRY_CREATE, new ArrayList<>(service.events)));
			service.reset();

			
			Files.write(childFile, "test".getBytes());
			service.waitOnLatches();

			// Now verify the events for the child
			assertTrue(matchingEventFound(childFile, 
					StandardWatchEventKinds.ENTRY_CREATE, new ArrayList<>(service.events)));
			service.reset();

			Files.write(childFile, "test2".getBytes());
			service.waitOnLatches();

			// Now verify the events
			assertTrue(matchingEventFound(childFile, 
					StandardWatchEventKinds.ENTRY_MODIFY, new ArrayList<>(service.events)));
			
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
	
	

	/*
	 * This test verifies that if someone creates a folder, 
	 * registers to listen to it, deletes the folder, recreates the folder, 
	 * and adds a file to that folder, we receive all the proper events. 
	 * 
	 * It's the same as above but without so many delays.
	 */
	@Test
	public void testFileWatcherEvent_subscribeDeleteRecreate() {
		System.out.println("\n\n*****testFileWatcherEvent_subscribeDeleteRecreate");

		FileWatcherServiceWithLatches service = new FileWatcherServiceWithLatches();
		service.start();
		service.reset();
		
		ArrayList<FileWatcherEvent> listenerEvents = new ArrayList<>();
		
		try {
			Path root = Files.createTempDirectory(getClass().getName() + "_3");
			Path childFile = root.resolve("out.txt");

			// Subscribe to folder/out.txt, which registers 
			// listeners to all folders above in tree
			IFileWatcherEventListener listener1 = (events2) -> listenerEvents.add(events2);
			service.addFileWatcherListener(root, listener1, true);
			
			assertTrue(root.toFile().delete());
			root.toFile().mkdirs();
			Files.write(childFile, "test".getBytes());
			service.waitOnLatches();

			debug(""+listenerEvents.size());
			for( FileWatcherEvent event : listenerEvents ) {
				debug(event.getKind() + " " + event.getPath().toString());
			}

			assertTrue(matchingFWEventFound(root, 
					StandardWatchEventKinds.ENTRY_DELETE, listenerEvents));
			assertTrue(matchingFWEventFound(root, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertTrue(matchingFWEventFound(childFile, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			
			
		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}
	

	/*
	 * This test verifies that if someone creates a folder, 
	 * registers to listen to it, deletes the folder, recreates the folder, 
	 * and adds a file several folders deeper, we receive all the proper events
	 * when the request is recursive.
	 */
	@Test
	public void testFileWatcherEvent_subscribeRootDeleteRootRecreateDeep() {

		FileWatcherServiceWithLatches service = new FileWatcherServiceWithLatches();
		service.start();
		service.reset();
		
		ArrayList<FileWatcherEvent> listenerEvents = new ArrayList<>();
		
		try {
			Path root = Files.createTempDirectory(getClass().getName() + "_4");
			Path nested1 = root.resolve("nested1");
			Path nested2 = nested1.resolve("nested2");
			Path nested3 = nested2.resolve("nested3");
			Path nested4 = nested3.resolve("nested4");
			Path childFile = nested4.resolve("out.txt");
			
			
			// Subscribe to root, which registers 
			// listeners to all folders above in tree
			IFileWatcherEventListener listener1 = (events2) -> listenerEvents.add(events2);
			service.addFileWatcherListener(root, listener1, true);
			
			assertTrue(root.toFile().delete());
			root.toFile().mkdirs();
			nested1.toFile().mkdirs();
			nested2.toFile().mkdirs();
			nested3.toFile().mkdirs();
			nested4.toFile().mkdirs();
			
			Files.write(childFile, "test".getBytes());
			service.waitOnLatches();

			assertTrue(matchingFWEventFound(root, 
					StandardWatchEventKinds.ENTRY_DELETE, listenerEvents));
			assertTrue(matchingFWEventFound(root, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertTrue(matchingFWEventFound(nested1, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertTrue(matchingFWEventFound(nested2, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertTrue(matchingFWEventFound(nested3, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertTrue(matchingFWEventFound(nested4, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertTrue(matchingFWEventFound(childFile, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			
			
		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}
	
	/*
	 * This test verifies that if someone creates a folder, 
	 * registers to listen to it, deletes the folder, recreates the folder, 
	 * and adds a file several folders deeper, we receive all the proper events
	 * when the request is recursive.
	 */
	@Test
	public void testFileWatcherEvent_subscribeDeepDeleteRootRecreateDeep() {
		_testFileWatcherEvent_subscribeDeepDeleteRootRecreateDeep(false);
	}

	@Test
	public void testFileWatcherEvent_subscribeDeepDeleteRootRecreateDeepRecursive() {
		_testFileWatcherEvent_subscribeDeepDeleteRootRecreateDeep(true);
	}

	private void _testFileWatcherEvent_subscribeDeepDeleteRootRecreateDeep(boolean recursive) {

		FileWatcherServiceWithLatches service = new FileWatcherServiceWithLatches();
		service.start();
		service.reset();
		
		ArrayList<FileWatcherEvent> listenerEvents = new ArrayList<>();
		
		try {
			Path root = Files.createTempDirectory(getClass().getName() + "_4");
			Path nested1 = root.resolve("nested1");
			Path nested2 = nested1.resolve("nested2");
			Path nested3 = nested2.resolve("nested3");
			Path nested4 = nested3.resolve("nested4");
			Path childFile = nested4.resolve("out.txt");
			
			
			// Subscribe to the deep file, which registers 
			// listeners to all folders above in tree
			IFileWatcherEventListener listener1 = (events2) -> listenerEvents.add(events2);
			service.addFileWatcherListener(childFile, listener1, recursive);
			
			assertTrue(root.toFile().delete());
			root.toFile().mkdirs();
			nested1.toFile().mkdirs();
			nested2.toFile().mkdirs();
			nested3.toFile().mkdirs();
			nested4.toFile().mkdirs();
			
			Files.write(childFile, "test".getBytes());
			service.waitOnLatches();

			assertFalse(matchingFWEventFound(root, 
					StandardWatchEventKinds.ENTRY_DELETE, listenerEvents));
			assertFalse(matchingFWEventFound(root, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertFalse(matchingFWEventFound(nested1, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertFalse(matchingFWEventFound(nested2, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertFalse(matchingFWEventFound(nested3, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertFalse(matchingFWEventFound(nested4, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			assertTrue(matchingFWEventFound(childFile, 
					StandardWatchEventKinds.ENTRY_CREATE, listenerEvents));
			
			
		} catch(IOException ioe) {
			fail();
		} finally {
			service.stop();
			assertNull(service.getExecutor());
			assertNull(service.getWatchService());
		}
	}
	private static class WatchKeyEvent {
		private WatchKey key;
		private WatchEvent<?> event;

		public WatchKeyEvent(WatchKey key, WatchEvent<?> event) {
			this.key = key;
			this.event = event;
		}
		public WatchKey getKey() {
			return key;
		}
		public WatchEvent<?> getEvent() {
			return event;
		}
	}
	
	
	private static class FileWatcherServiceWithLatches extends TestableFileWatcherService {
		
		// Testing the events but not through the listeners. 
		// This tests the raw events, and so may include events
		// from ANY watched path
		
		final CountDownLatch[] startLatch = new CountDownLatch[1];
		final CountDownLatch[] endLatch = new CountDownLatch[1];
		final ArrayList<WatchKeyEvent> events = new ArrayList<>();
		private boolean waiting = false;
		
		public FileWatcherServiceWithLatches() {
			super();
		}
		@Override
		public void start() throws IllegalStateException {
			super.start();
			reset();
		}
		
		private synchronized boolean isWaiting() {
			return waiting;
		}
		
		private synchronized void setWaiting(boolean b) {
			this.waiting = b;
		}
		
		@Override
		protected void fireEvents(WatchKey key, WatchEvent<?> event) {
			debug("fireEvents[1]");

			setWaiting(true);
			try {
				startLatch[0].await();
				debug("fireEvents[2]");
			} catch(InterruptedException ie) {
			}
			
			events.add(new WatchKeyEvent(key, event));
			debug("fireEvents[3]");
			super.fireEvents(key, event);
			endLatch[0].countDown();
			setWaiting(false);
			debug("fireEvents[4]");
		}
		
		public void reset() {
			resetLatches();
			events.clear();
		}
		
		private void resetLatches() {
			debug("resetLatches[1]");

			if( startLatch[0] != null ) {
				// If something's already waiting at the latch, 
				// better let them proceed or they'll get stuck there forever 
				startLatch[0].countDown();
				debug("resetLatches[2]");
			}
			startLatch[0] = new CountDownLatch(1);
			endLatch[0] = new CountDownLatch(1);
			debug("resetLatches[3]");
		}
		
		/*
		 * Wait for all events to be completely handled.
		 * Sometimes a framework may throw several events consecutively, 
		 * and so we need to be prepared to wait a bit here
		 * to verify that there's nothing left in the queue before
		 * beginning to analyze the results.
		 * 
		 * This feels a bit like a hack, but it's the best I got at the moment
		 */
		public void waitOnLatches() {
			debug("waitOnLatches[1]");
			boolean done = false;
			do {
				startLatch[0].countDown();
				debug("waitOnLatches[2]");
				try {
					if( !endLatch[0].await(200, TimeUnit.MILLISECONDS)) {
						done = true;
					}
					debug("waitOnLatches[3]");
				} catch (InterruptedException e) {
					debug("waitOnLatches[4]");
					return;
				}
				resetLatches();
				debug("waitOnLatches[5]");
			} while(!done);
			debug("waitOnLatches[6]");
		}
	}
	
	private boolean matchingFWEventFound(Path path, Kind<?> kind, List<FileWatcherEvent> events) {
		for( FileWatcherEvent keyvent : events ) {
			Path absolute = keyvent.getPath();
			if( absolute.equals(path) && kind.equals(keyvent.getKind()))
				return true;
		}
		return false;
	}
	private boolean matchingEventFound(Path path, Kind<?> kind, List<WatchKeyEvent> events) {
		for( WatchKeyEvent keyvent : events ) {
			WatchEvent<?> event = keyvent.getEvent();
			Path matched = ((Path)event.context());
			Path absolute = ((Path)keyvent.key.watchable()).resolve(matched);
			if( absolute.equals(path) && kind.equals(event.kind()))
				return true;
		}
		return false;
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
