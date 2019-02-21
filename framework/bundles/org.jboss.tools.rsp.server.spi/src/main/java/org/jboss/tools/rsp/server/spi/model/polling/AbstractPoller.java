/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.tools.rsp.server.spi.servertype.IServer;

public abstract class AbstractPoller implements IServerStatePoller {

	private static final long POLLING_DELAY = 200;

	private IServer server;
	private boolean canceled; 
	private boolean done;
	private SERVER_STATE state;
	private SERVER_STATE expectedState;
	private ExecutorService executor = Executors.newSingleThreadExecutor(
			(Runnable runnable) -> new Thread(runnable, getThreadName()));
	
	public void beginPolling(IServer server, SERVER_STATE expectedState) {
		this.server = server;
		this.canceled = done = false;
		this.expectedState = expectedState;
		this.state = SERVER_STATE.UNKNOWN;
		launchThread();
	}

	protected void launchThread() {
		executor.execute(() -> pollerRun());
	}

	private void pollerRun() {
		setStateInternal(false, state);
		while(!canceled && !done) {
			SERVER_STATE stat = onePing(server);
			if (expectedState == stat) {
				setStateInternal(true, stat);
			}
			try {
					Thread.sleep(POLLING_DELAY);
			} catch (InterruptedException e) {
				cancel(CANCELATION_CAUSE.CANCEL);
			}
		}
	}

	protected abstract SERVER_STATE onePing(IServer server);
	
	private synchronized void setStateInternal(boolean done, SERVER_STATE state) {
		this.done = done;
		this.state = state;
	}
	
	protected abstract String getThreadName();

	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	public synchronized boolean isComplete() throws PollingException, RequiresInfoException {
		return done;
	}

	@Override
	public synchronized SERVER_STATE getState() throws PollingException, RequiresInfoException {
		return state;
	}
	
	@Override
	public void cleanup() {
		executor.shutdownNow();
	}

	@Override
	public List<String> getRequiredProperties() {
		return Collections.emptyList();
	}

	@Override
	public void provideCredentials(Properties properties) {
	}

	@Override
	public SERVER_STATE getCurrentStateSynchronous(IServer server) {
		return onePing(server);
	}

	@Override
	public synchronized void cancel(CANCELATION_CAUSE cause) {
		this.canceled = true;
		this.state = null;
		cleanup();
	}

	@Override
	public TIMEOUT_BEHAVIOR getTimeoutBehavior() {
		return IServerStatePoller.TIMEOUT_BEHAVIOR.FAIL;
	}

}
