/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.filewatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherEventListener;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWatcherService implements IFileWatcherService {
	private static final Logger LOG = LoggerFactory.getLogger(FileWatcherService.class);

	// The watch service from java.nio
	private WatchService watchService;
	/* 
	 *  A map of paths and associated listeners that have been specifically
	 *  requested by some client to be listened to.
	 *  
	 */ 
	private HashMap<Path, List<RegistrationRequest>> requests = new HashMap<>();
	
	/*
	 * A map of path -> watch key, listing each and every folder 
	 * that we are subscribed to receive events from. 
	 * This map will contain much more data than the requests map. 
	 * It will possibly contain recursive subscriptions, as well 
	 * as parent subscriptions up to the root of the filesystem. 
	 */
	private Map<Path, WatchKey> subscriptions = new HashMap<>();
	
	private boolean closing = false;
	
	private ExecutorService executor = null;
	private Future<?> executorFuture = null;

	public FileWatcherService() {
	}
	
	private String getThreadName() {
		return "RSP File Watcher Service";
	}

	private void log(Exception e) {
		LOG.error(e.getMessage(), e);
	}

	private synchronized void setClosing(boolean b) {
		this.closing = b;
	}
	
	private synchronized boolean isClosing() {
		return this.closing;
	}
	
	@Override
	public synchronized void start() throws IllegalStateException {
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException | UnsupportedOperationException e) {
			log(e);
			throw new IllegalStateException("Unable to create a filesystem watch service");
		}
		executor = Executors.newSingleThreadExecutor(
				(Runnable runnable) -> new Thread(runnable, getThreadName()));
		executorFuture = executor.submit(() -> runFileWatcher());
	}
	
	@Override
	public synchronized void stop() {
		setClosing(true);
		if( executorFuture != null ) 
			executorFuture.cancel(true);
		executor.shutdownNow();
		executor = null;
		executorFuture = null;
		clearModel();
		try {
			if( watchService != null ) 
				watchService.close();
		} catch (IOException e) {
			log(e);
		}
		watchService = null;
	}
	
	private synchronized void clearModel() {
		for( WatchKey key : subscriptions.values()) {
			key.cancel();
		}
		subscriptions.clear();
	}
	
	@Override
	public synchronized void addFileWatcherListener(Path path, 
			IFileWatcherEventListener listener, boolean recursive) {
		RegistrationRequest req = new RegistrationRequest(path, listener, recursive);
		List<RegistrationRequest> list = requests.get(path);
		if( list == null ) {
			list = new ArrayList<>();
			requests.put(path, list);
		}
		
		// Ignore a request for an identical listener
		if( listContainsRequestForListener(list, listener))
			return;
		
		list.add(req);
		
		ensurePathAndParentsSubscribed(path);
		if( recursive ) {
			ensureChildrenSubscribed(path);
		}
	}

	private boolean listContainsRequestForListener(List<RegistrationRequest> list, 
			IFileWatcherEventListener listener) {
		for( RegistrationRequest req : list ) {
			if( req.getListener() == listener )
				return true;
		}
		return false;
	}
	
	private void ensureChildrenSubscribed(Path p) {
		if( p.toFile().exists() && p.toFile().isDirectory()) {
			subscribeSinglePath(p);
			File[] kids = p.toFile().listFiles();
			for( int i = 0; i < kids.length; i++ ) {
				ensureChildrenSubscribed(kids[i].toPath());
			}
		}
	}
	
	private void ensurePathAndParentsSubscribed(Path p) {
		Path working = p;
		while(working != null ) {
			if( working.toFile().exists() && working.toFile().isDirectory()) {
				subscribeSinglePath(working);
			}
			working = working.getParent();
		}
	}
	
	private void subscribeSinglePath(Path working) {
		try {
			// The service should return the same watchkey for 
			// the same path, assuming the folder hasn't been deleted
			// and recreated. 
			WatchKey k = working.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, 
			        StandardWatchEventKinds.ENTRY_DELETE,  StandardWatchEventKinds.ENTRY_MODIFY);
			WatchKey existing = subscriptions.get(working);
			if( !k.equals(existing)) {
				subscriptions.put(working, k);
				if( existing != null )
					existing.cancel();
			}
		} catch (IOException e) {
			log(e);
		}
	}
	
	@Override
	public synchronized void removeFileWatcherListener(Path path, IFileWatcherEventListener listener) {
		List<RegistrationRequest> list = requests.get(path);
		if( list != null ) {
			Iterator<RegistrationRequest> rit = list.iterator();
			RegistrationRequest r = null;
			while(rit.hasNext()) {
				r = rit.next();
				if( r.getListener() == listener ) {
					rit.remove();
				}
			}
			if( list.isEmpty() ) {
				requests.remove(path);
			}
			updateSubscriptionsForRemovedRegistration(path);
		}
	}

	private void updateSubscriptionsForRemovedRegistration(Path path) {
		// If I'm not needed, unsubscribe
		if( !pathShouldBeSubscribed(path)) {
			removeSubscription(path);
			
			// If I don't need to be watched anymore, do my parents? 
			Path working = path.getParent();
			while( working != null ) {
				if( !pathShouldBeSubscribed(working)) {
					removeSubscription(working);
				} else {
					break;
				}
				working = working.getParent();
			}
			
		}
		
		/*
		 *  I might still be needed, but maybe we don't need to watch
		 *  every folder below me. For example, maybe someone subscribed
		 *  to /home/user/test  (recursive), and another subscribed 
		 *  to /home/user/test/inner/five.   
		 *  
		 *  We may still need to listen to changes in /home/user/test, 
		 *  but perhaps nobody needs to hear updates from 
		 *  /home/user/test/deployments or /home/user/test/incoming
		 */
		 
		List<Path> nested = findAllSubtreeSubscriptions(path);
		for( Path test : nested ) {
			if( !pathShouldBeSubscribed(test)) {
				removeSubscription(test);
			}
		}
	}
	
	private List<Path> findAllSubtreeSubscriptions(Path path) {
		List<Path> nested = new ArrayList<Path>();
		for( Path test : subscriptions.keySet()) {
			if( test.startsWith(path)) {
				nested.add(test);
			}
		}
		return nested;
	}
	
	private void removeSubscription(Path path) {
		// Stop watching the given folder. 
		WatchKey wk = subscriptions.get(path);
		if( wk != null )
			wk.cancel();
		subscriptions.remove(path);
	}


	private boolean pathShouldBeSubscribed(Path path) {
		// A recursive request to me or my parent exists, so I'm still needed
		if( pathOrParentHasRecursiveRequest(path))
			return true;
		
		// A request still exists for this exact path, so I'm still needed
		if( requests.get(path) != null && !requests.get(path).isEmpty() )
			return true;
		
		// A request for a subfolder (or lower) still exists, so I'm still needed
		Set<Path> requestPaths = requests.keySet();
		for( Path testRequest : requestPaths) {
			if( testRequest.startsWith(path))
				return true;
		}

		// Nobody needs me
		return false;
	}
	
	private boolean pathOrParentHasRecursiveRequest(Path p) {
		Path working = p;
		while(working != null ) {
			if( singlePathHasRecursiveRequest(working)) {
				return true;
			}
			working = working.getParent();
		}
		return false;
	}
	private boolean singlePathHasRecursiveRequest(Path p) {
		List<RegistrationRequest> list = requests.get(p);
		if( list == null ) 
			return false;
		Iterator<RegistrationRequest> rit = list.iterator();
		RegistrationRequest r = null;
		while(rit.hasNext()) {
			r = rit.next();
			if( r.isRecursive())
				return true;
		}
		return false;
	}

	public void runFileWatcher() {
		WatchKey key;
		try {
			while (watchService != null && (key = watchService.take()) != null) {
				// Get the list and reset immediately
				// when the WatchKey instance is returned by either of 
				// the poll or take APIs, it will not capture more events 
				// if it’s reset API is not invoked:
				List<WatchEvent<?>> events = key.pollEvents();
				key.reset();
				
				for (WatchEvent<?> event : events) {
					Path eventContext = (Path)event.context();
					if( eventContext != null ) {
						Path context = ((Path)key.watchable()).resolve((Path)event.context());
						subscribeToChanges(event, context);
						fireEvents(key, event);
					}
				}
			}
		} catch (InterruptedException | ClosedWatchServiceException e) {
			if( !isClosing()) {
				log(e);
			}
		}
		setClosing(false);
	}

	/*
	 * Subscribe to any newly created folders that are below a recursive 
	 * subscription. Cleanup any watch keys that are already listening 
	 * to a folder that may have been deleted.  In short, make 
	 * sure the watch service is listening to the right things after
	 * whatever changes have been detected. 
	 */
	private synchronized void subscribeToChanges(WatchEvent<?> event, Path eventContext) {
		if( event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
			// something we were watching has been deleted. Let's 
			// make sure that we delete all current watch keys for that 
			// and any sub-directories if possible
			removeAllSubscriptionsRecursive(eventContext);
		} else if( event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
			if( eventContext.toFile().isDirectory()) {
				// a new folder has been created. 
				// Let's see if any requests match this path, 
				// or if any recursive requests match our parents
				boolean recursive = recursiveRequestMatches(eventContext);
				if( recursive ) {
					ensureChildrenSubscribed(eventContext);
				} else if( requestMatchesExact(eventContext) ) {
					subscribeSinglePath(eventContext);
				}
			}
		}
		// ignore modify event kind
		
	}

	private boolean requestMatchesExact(Path path) {
		return requests.get(path) != null && !requests.get(path).isEmpty();
	}
	
	private boolean recursiveRequestMatches(Path path) {
		Path working = path;
		while( working != null ) {
			if( containsRecursiveRequest(requests.get(path))) {
				return true;
			}
			working = working.getParent();
		}
		return false;
	}
	
	private boolean containsRecursiveRequest(List<RegistrationRequest> list) {
		if( list == null )
			return false;
		for( RegistrationRequest rr : list ) {
			if( rr.isRecursive())
				return true;
		}
		return false;
	}

	private void removeAllSubscriptionsRecursive(Path eventContext) {
		Path absolute = eventContext.toAbsolutePath();
		Iterator<Path> pit = new ArrayList<Path>(subscriptions.keySet()).iterator();
		Path p1 = null;
		while(pit.hasNext()) {
			p1 = pit.next();
			if( p1.startsWith(absolute)) {
				WatchKey wk = subscriptions.get(p1);
				wk.cancel();
				subscriptions.remove(p1);
			}
		}
	}

	protected void fireEvents(WatchKey key, WatchEvent<?> event) {
		Path context = ((Path)key.watchable()).resolve((Path)event.context());
		FileWatcherEvent toFire = new FileWatcherEvent(context, event.kind());

		// Find non-recursive requests matching this exact path
		Set<IFileWatcherEventListener> nonRecursive = findListenersForExactPath(context, false);
		// and fire their simple events
		for(IFileWatcherEventListener one : nonRecursive  ) {
			one.fileChanged(toFire);
		}
		
		// Find all recursive listeners at level 'context' or above
		// that must be alerted to child changes
		Set<IFileWatcherEventListener> recursiveListeners = 
				getRecursiveListenersForPathOrParent(context);
		
		// Now let's fire this item's event to all recursive listeners
		for(IFileWatcherEventListener one : recursiveListeners  ) {
			one.fileChanged(toFire);
		}
		
		/* 
		 * Now comes the hard part.
		 * 
		 * If a directory is deleted, deletion events for all descendents 
		 * are already thrown by NIO.  
		 * 
		 * A directory cannot really be modified, so handling of modifications
		 * is not really necessary. Files have no children, so also unnecessary.
		 * 
		 * However, if a directory is CREATED, it is almost guaranteed 
		 * that this service class will not be registered to listen to changes
		 * in those children when new files inside those children are created.
		 * 
		 * What this means is, when a directory is created, we may receive the 
		 * event some time after it was created, and files may already be present 
		 * inside of it. Because of this, we must now traverse the tree
		 * and fire events at each step for every recursive listener. 
		 * 
		 */
		if( event.kind() == StandardWatchEventKinds.ENTRY_CREATE
				&& context.toFile().isDirectory() && context.toFile().exists()) {
			List<ListenerEvent> events = createRecursiveSyntheticCreationEvents(
					context, recursiveListeners);
			for( ListenerEvent e : events ) {
				e.getListener().fileChanged(e.getEvent());
			}
		}
		
	}
	
	private List<ListenerEvent> createRecursiveSyntheticCreationEvents(
			Path context, Set<IFileWatcherEventListener> recursiveListeners) {
		File[] children = context.toFile().listFiles();
		List<ListenerEvent> ret = new ArrayList<>();
		if( children != null ) {
			for( int i = 0; i < children.length; i++ ) {
				Path child = children[i].toPath();
				FileWatcherEvent toFire = new FileWatcherEvent(child, StandardWatchEventKinds.ENTRY_CREATE);
				
				// First handle existing recursive listeners
				for( IFileWatcherEventListener listener : recursiveListeners) {
					ret.add(new ListenerEvent(listener, toFire));
				}
				
				// Now handle listeners for this child path specifically
				Set<IFileWatcherEventListener> childPathListeners = 
						findListenersForExactPath(child);
				for( IFileWatcherEventListener listener : childPathListeners) {
					ret.add(new ListenerEvent(listener, toFire));
				}
				
				// Now recurse if this child is a directory
				if( child.toFile().exists() && child.toFile().isDirectory()) {
					Set<IFileWatcherEventListener> childRecurseListeners = new HashSet<>();
					childRecurseListeners.addAll(recursiveListeners);
					childRecurseListeners.addAll(findListenersForExactPath(child, true));
					ret.addAll(createRecursiveSyntheticCreationEvents(child, childRecurseListeners));
				}
			}
		}
		return ret;
	}

	protected Set<IFileWatcherEventListener> findListenersForExactPath(Path p, boolean recursive) {
		List<RegistrationRequest> forPath = requests.get(p);
		if( forPath != null ) {
			return (forPath.stream().filter(x -> recursive == x.isRecursive())
					.map(RegistrationRequest::getListener).collect(Collectors.toSet()));
		}
		return Collections.emptySet();
	}

	protected Set<IFileWatcherEventListener> findListenersForExactPath(Path p) {
		List<RegistrationRequest> forPath = requests.get(p);
		if( forPath != null ) {
			return (forPath.stream().map(RegistrationRequest::getListener)
					.collect(Collectors.toSet()));
		}
		return Collections.emptySet();
	}

	/*
	 * Get all recursive listeners registered for the given path, 
	 * in addition to all recursive listeners registered for any 
	 * parent path
	 */
	protected Set<IFileWatcherEventListener> 
				getRecursiveListenersForPathOrParent(Path target) {
		Path[] all = getPathAndAncestors(target);
		Set<IFileWatcherEventListener> ret = new HashSet<>();
		for( Path p : all ) {
			ret.addAll(findListenersForExactPath(p, true));
		}
		return ret;
	}
	
	/*
	 * Get an array of paths representing 
	 * both the parameter and all of its parent directories
	 */
	private Path[] getPathAndAncestors(Path p) {
		ArrayList<Path> ret = new ArrayList<>();
		Path working = p;
		while( working != null ) {
			ret.add(working);
			working = working.getParent();
		}
		return ret.toArray(new Path[ret.size()]);
	}
	
	protected static class ListenerEvent {
		private IFileWatcherEventListener listener;
		private FileWatcherEvent event;

		public ListenerEvent(IFileWatcherEventListener listener, FileWatcherEvent event) {
			this.listener = listener;
			this.event = event;
		}

		public IFileWatcherEventListener getListener() {
			return listener;
		}

		public FileWatcherEvent getEvent() {
			return event;
		}
	}
	
	protected static class RegistrationRequest {
		private Path path;
		private IFileWatcherEventListener listener;
		private boolean recursive;

		public RegistrationRequest(Path path, IFileWatcherEventListener listener, boolean recursive) {
			this.path = path;
			this.listener = listener;
			this.recursive = recursive;
			
		}

		public Path getPath() {
			return path;
		}

		public IFileWatcherEventListener getListener() {
			return listener;
		}

		public boolean isRecursive() {
			return recursive;
		}
	}

	/*
	 * Getters For testing
	 */
	protected WatchService getWatchService() {
		return watchService;
	}

	protected ExecutorService getExecutor() {
		return executor;
	}

	protected HashMap<Path, List<RegistrationRequest>> getRequests() {
		return requests;
	}

	protected Map<Path, WatchKey> getSubscriptions() {
		return subscriptions;
	}
}
