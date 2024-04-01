/*******************************************************************************
 * Copyright (c) 2018, 2024 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.SERVER_STATE;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

/**
 * @author Rob Stryker
 * @author André Dietisheim
 */
public class PollThreadUtils {

	public static final String PROP_POLL_THREAD_KEY = "org.jboss.tools.rsp.server.spi.model.polling.PollThreadKey";

	private static final int DEFAULT_TIMEOUT = DefaultServerAttributes.DEFAULT_SERVER_TRANSITION_TIMEOUT;

	private PollThreadUtils() {
	}

	/**
	 * Stops the given poll thread.
	 * 
	 * @param pollThread the poll thread to stop
	 */
	public static void stopPolling(PollThread pollThread) {
		cancelPolling(null, pollThread);
	}

	/**
	 * Cancels the given poll thread with the given message (that tells about the reason to cancel polling).
	 * 
	 * @param message the reason to cancel the poll thread
	 * @param pollThread the poll thread to cancel
	 */
	public static void cancelPolling(String message, PollThread pollThread) {
		if (pollThread == null
				|| !pollThread.isAlive()) {
			return;
		}
		if (message != null) {
			pollThread.cancel(message, IServerStatePoller.CANCELATION_CAUSE.CANCEL);
		} else {
			pollThread.cancel();
		}
	}
	
	/*
	 * A solution needs to be found here. 
	 * Should ideally use the poller that the server says is its poller,
	 * but some pollers such as timeout poller cannot actively check
	 */
	public static SERVER_STATE isServerStarted(IServer server,IServerStatePoller poller ) {
		return poller.getCurrentStateSynchronous(server);
	}
	

	public static PollThread pollServer(IServer server, SERVER_STATE expectedState, IServerStatePoller poller, IPollResultListener listener) {
		return pollServer(DEFAULT_TIMEOUT, server, expectedState, poller, listener);
	}

	public static PollThread pollServer(int defaultTimeout, IServer server, SERVER_STATE expectedState, IServerStatePoller poller, IPollResultListener listener) {
		String key = (expectedState == SERVER_STATE.UP ? DefaultServerAttributes.SERVER_TIMEOUT_STARTUP : DefaultServerAttributes.SERVER_TIMEOUT_SHUTDOWN);
		int timeoutVal = server.getAttribute(key, defaultTimeout);
		return pollServer(server, expectedState, poller, listener, timeoutVal); 
	}

	public static PollThread pollServer(IServer server, SERVER_STATE expectedState, IServerStatePoller poller,
			IPollResultListener listener, int actualTimeout) {
		IServerDelegate del = server.getDelegate();
		PollThread pollThread = getPollThread(del);
		pollThread = pollServer(server, expectedState, poller, pollThread, listener, actualTimeout);
		return pollThread;
	}
	
	/**
	 * Stops the given PollThread and creates a new PollThread, that polls the given
	 * IServer for the given SERVER_STATE using the given IServerStatePoller and
	 * notifies the given IPollResultListener of the results.
	 *
	 * @param expectedState the state to wait for
	 * @param poller        the poller to use to wait for the expected state
	 * @param pollThread    the poll thread to stop
	 * @param listener      the listener to inform about the polling result
	 * @return the new poll thread
	 * 
	 * @see PollThread
	 * @see SERVER_STATE
	 * @see IServerStatePoller
	 * @see IServer
	 */
	public static PollThread pollServer(IServer server, SERVER_STATE expectedState, IServerStatePoller poller, PollThread currentPollThread,
			IPollResultListener listener, int timeout) {
		stopPolling(currentPollThread);
		PollThread newPollThread = new PollThread(expectedState, poller, listener, server, timeout);
		savePollThread(newPollThread, server.getDelegate());
		newPollThread.start();
		return newPollThread;
	}

	public static PollThread getPollThread(IServerDelegate delegate) {
		return (PollThread) delegate.getSharedData(PROP_POLL_THREAD_KEY);
	}
	
	public static void savePollThread(PollThread poller, IServerDelegate delegate) {
		delegate.putSharedData(PROP_POLL_THREAD_KEY, poller);
	}

}
