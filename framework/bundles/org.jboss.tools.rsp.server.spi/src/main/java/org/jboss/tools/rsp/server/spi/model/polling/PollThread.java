/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.CANCELATION_CAUSE;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.SERVER_STATE;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.TIMEOUT_BEHAVIOR;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

/**
 * 
 * @author rob.stryker@jboss.com
 */
public class PollThread extends Thread {

	private boolean abort, stateStartedOrStopped;
	private SERVER_STATE expectedState;
	private IServerStatePoller poller;
	private IPollResultListener listener;
	private IServer server;
	private int timeout;

	public PollThread(SERVER_STATE expectedState, IServerStatePoller poller, IPollResultListener listener, IServer server, int timeout) {
		super(getThreadName(server));
		this.expectedState = expectedState;
		this.poller = poller;
		this.server = server;
		this.listener = listener;
		this.abort = false;
		this.timeout = timeout;
	}

	private static String getThreadName(IServer server) {
		return NLS.bind("{0} - Server Poller", server.getName());
	}

	public void cancel() {
		cancel(null);
	}

	public void cancel(String message) {
		abort = true;
		//abortMessage = message;
		poller.cancel(IServerStatePoller.CANCELATION_CAUSE.CANCEL);
	}

	private SERVER_STATE oppositeState(SERVER_STATE state) {
		if( state == SERVER_STATE.UNKNOWN) {
			// There's no opposite to unknown... 
			return SERVER_STATE.UNKNOWN;
		}
		if( state == SERVER_STATE.UP)
			return SERVER_STATE.DOWN;
		return SERVER_STATE.UP;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void run() {
		// Poller not found. Abort
		if (poller == null) {
			alertListener(oppositeState(expectedState));
			return;
		}

		int maxWait = getTimeout();

		long startTime = System.currentTimeMillis();
		boolean done = false;
		try {
			poller.beginPolling(getServer(), expectedState);
	
			// begin the loop; ask the poller every so often
			while (!stateStartedOrStopped 
					&& !abort 
					&& !done
					&& !timeoutReached(startTime, maxWait)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					// I have no idea what I'm supposed to do here to make this 'not empty'
				}
	
				try {
					done = poller.isComplete();
				} catch (PollingException e) {
					// abort and put the message in event log
					poller.cancel(CANCELATION_CAUSE.CANCEL);
					poller.cleanup();
					alertListener(oppositeState(expectedState));
					return;
				} catch (RequiresInfoException rie) {
					// This way each request for new info is checked only once.
					if (!rie.getChecked()) {
						rie.setChecked();
						fireRequestCredentials(expectedState, poller);
					}
				}
				stateStartedOrStopped = checkServerState();
			}
			if (stateStartedOrStopped) {
				// we stopped. Did we abort?
				handleUncertainTermination();
			} else if (abort) {
				// Definite abort
				poller.cleanup();
			} else if (done) {
				// the poller has an answer
				handlePollerHasAnswer();
			} else {
				// we timed out. get response from preferences
				handleTimeoutTermination();
			}
		} catch(Exception e) {
			e.printStackTrace();
			handleExceptionTermination();
		}

	}

	private void handlePollerHasAnswer() {
		try {
			SERVER_STATE currentState = poller.getState();
			poller.cleanup();
			alertListener(currentState);
		} catch (PollingException pe) {
			// Poller's answer was exception:  abort and put the message in event log
			poller.cancel(CANCELATION_CAUSE.CANCEL);
			poller.cleanup();
			alertListener(oppositeState(expectedState));
			return;
		} catch (RequiresInfoException rie) {
			// You don't have an answer... liar!
		}
	}
	private void handleExceptionTermination() {
		cancel();
		poller.cleanup();
		handleTimeoutBehavior();
	}
	private void handleTimeoutTermination() {
		poller.cancel(CANCELATION_CAUSE.TIMEOUT_REACHED);
		poller.cleanup();
		handleTimeoutBehavior();
	}
	private void handleTimeoutBehavior() {
		TIMEOUT_BEHAVIOR behavior = poller.getTimeoutBehavior();
		// xnor;
		// if behavior is to succeed and we're expected to go up, we're up
		// if behavior is to fail and we're expecting to be down, we're up (failed to shutdown)
		// all other cases, we're down.
		boolean expectedAsBool = (expectedState == SERVER_STATE.UP);
		boolean currentState = (expectedAsBool == (behavior == TIMEOUT_BEHAVIOR.SUCCEED));
		SERVER_STATE ret = (currentState ? SERVER_STATE.UP : SERVER_STATE.DOWN);
		alertListener(ret);
	}
	
	private void handleUncertainTermination() {
		int state = server.getDelegate().getServerRunState();
		boolean success = false;
		if (expectedState == SERVER_STATE.UP)
			success = state == IServerDelegate.STATE_STARTED;
		else
			success = state == IServerDelegate.STATE_STOPPED;

		poller.cancel(success ? CANCELATION_CAUSE.SUCCESS
				: CANCELATION_CAUSE.FAILED);
		poller.cleanup();
	}
	
	private boolean timeoutReached(long startTime, int maxWait) {
		return System.currentTimeMillis() >= (startTime + maxWait);
	}

	protected boolean checkServerState() {
		int state = server.getDelegate().getServerRunState();
		if (state == IServerDelegate.STATE_STARTED)
			return true;
		if (state == IServerDelegate.STATE_STOPPED)
			return true;
		return false;
	}

	protected void alertListener(SERVER_STATE currentState) {
		if (currentState != expectedState) {
			listener.stateNotAsserted(expectedState, currentState);
		} else {
			listener.stateAsserted(expectedState, currentState);
		}
	}
	
	protected IServer getServer() {
		return server;
	}

	public static void fireRequestCredentials(SERVER_STATE expectedState, IServerStatePoller poller) {
		// TODO We need to find a way to accomplish this
		
		//PollThreadUtils.requestCredentialsAsynch(poller, poller.getRequiredProperties());
		
	}
}