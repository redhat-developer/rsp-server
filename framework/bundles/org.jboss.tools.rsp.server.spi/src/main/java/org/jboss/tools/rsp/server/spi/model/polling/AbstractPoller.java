/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.tools.rsp.server.spi.servertype.IServer;

public abstract class AbstractPoller implements IServerStatePoller {

	private IServer server;
	private boolean canceled, done;
	private SERVER_STATE state;
	private SERVER_STATE expectedState;

	public void beginPolling(IServer server, SERVER_STATE expectedState) {
		this.server = server;
		this.canceled = done = false;
		this.expectedState = expectedState;
		this.state = SERVER_STATE.UNKNOWN;
		launchThread();
	}

	protected void launchThread() {
		Thread t = new Thread(new Runnable(){
			public void run() {
				pollerRun();
			}
		}, getThreadName()); //$NON-NLS-1$
		t.start();
	}
	
	protected abstract String getThreadName();

	private synchronized void setStateInternal(boolean done, SERVER_STATE state) {
		this.done = done;
		this.state = state;
	}
	
	private void pollerRun() {
		setStateInternal(false, state);
		while(!canceled && !done) {
			SERVER_STATE stat = onePing(server);
			if( stat == expectedState ) {
				setStateInternal(true, expectedState);
			}
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) {} // ignore
		}
	}

	protected abstract SERVER_STATE onePing(IServer server);
	
	public IServer getServer() {
		return server;
	}

	public synchronized boolean isComplete() throws PollingException, RequiresInfoException {
		return done;
	}

	public synchronized SERVER_STATE getState() throws PollingException, RequiresInfoException {
		return state;
	}

	private SERVER_STATE fromBool(boolean b) {
		return b ? SERVER_STATE.UP : SERVER_STATE.DOWN;
	}
	
	public void cleanup() {
	}

	public List<String> getRequiredProperties() {
		return new ArrayList<String>();
	}

	public void provideCredentials(Properties properties) {
	}

	public SERVER_STATE getCurrentStateSynchronous(IServer server) {
		return onePing(server);
	}

	@Override
	public synchronized void cancel(CANCELATION_CAUSE cause) {
		canceled = true;
	}

	@Override
	public TIMEOUT_BEHAVIOR getTimeoutBehavior() {
		return IServerStatePoller.TIMEOUT_BEHAVIOR.FAIL;
	}

}
