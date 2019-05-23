package org.jboss.tools.rsp.server.model.internal.publishing;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

/**
 * This thread will be spawned in response to either deployments
 * being added, removed, or modified. 
 * The thread will await inactivity for some duration, and then 
 * initiate a publish request. 
 * 
 * As other parts of ServrePublishStateModel receive filesystem events,
 * they will update the inactivity timer for this thread, to ensure
 * the thread waits longer before initiating a publish request. 
 * 
 */
public class AutoPublishThread extends Thread {
	private int maxInactive = 1;
	private IServer server;
	private boolean publishBegan;
	private boolean done;
	private long lastUpdated;
	public AutoPublishThread(IServer server, int ms) {
		this.server = server;
		this.maxInactive= ms;
		this.publishBegan = false;
		this.done = false;
		this.lastUpdated = System.currentTimeMillis();
		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY + 1);
	}
	
	public void run() {
		boolean shouldPublish = awaitInactivity();
		if( shouldPublish) {
			publishImpl();
			setDone();
		}
	}
	
	protected void publishImpl() {
		try {
			server.getServerModel().publish(server, ServerManagementAPIConstants.PUBLISH_INCREMENTAL);
		} catch (CoreException e) {
			ServerPublishStateModel.LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Await an inactive state for a certain duration. 
	 * @return true if should publish, false if should abort thread
	 */
	protected boolean awaitInactivity() {
		// Don't even wait, if state is garbage just abort now
		if( shouldAbort()) {
			setDone();
			return false;
		}
		
		while( !getPublishBegan()) {
			long preSleepLastUpdated = getLastUpdated();
			sleepExpectedDuration();
			if( shouldAbort()) {
				setDone();
				return false;
			}
			synchronized ( this ) {
				if( getLastUpdated() != preSleepLastUpdated ) {
					// While we slept, someone updated another file, 
					// which means we need to wait longer
					continue;
				}
				setPublishBegan();
			}
		}
		return true;
	}
	
	protected boolean shouldAbort() {
		ServerState state = getServerState(); 
		int runState = state.getState(); 
		if(  runState != ServerManagementAPIConstants.STATE_STARTED) {
			return true;
		}				
		
		int publishState = state.getPublishState();
		if( publishState == ServerManagementAPIConstants.PUBLISH_STATE_NONE) {
			return true;
		}
		return false;
	}
	
	protected ServerState getServerState() {
		return server.getDelegate().getServerState();
	}
	
	/**
	 * Sleep the duration expected to reach our cutoff for filesystem silence.
	 * Return the timestamp of when the last fs change was received.
	 * @return
	 */
	protected void sleepExpectedDuration() {
		try {
			long curTime = System.currentTimeMillis();
			long nextSleep = getAwakenTime() - curTime;
			if( nextSleep > 0) {
				sleep(nextSleep);
			}
		} catch(InterruptedException ie) {
			Thread.interrupted();
		}
	}
	
	public synchronized void updateInactivityCounter() {
		this.lastUpdated = System.currentTimeMillis();
	}

	protected synchronized long getLastUpdated() {
		return this.lastUpdated;
	}

	protected long getAwakenTime() {
		long awakenTime = getLastUpdated() + maxInactive;
		return awakenTime;
	}
	
	protected synchronized void setPublishBegan() {
		this.publishBegan = true;
	}
	protected synchronized boolean getPublishBegan() {
		return this.publishBegan;
	}
	protected synchronized void setDone() {
		this.done = true;
	}
	protected synchronized boolean isDone() {
		return this.done;
	}
}