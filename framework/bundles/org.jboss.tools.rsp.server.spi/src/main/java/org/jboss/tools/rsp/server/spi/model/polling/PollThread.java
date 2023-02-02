/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author rob.stryker@jboss.com
 */
public class PollThread extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(PollThread.class);

	private static final int POLL_DELAY = 100;
	
	private boolean aborted;
	private SERVER_STATE expectedState;
	private IServerStatePoller poller;
	private IPollResultListener listener;
	private IServer server;
	private int timeout;
	private boolean listenerAlerted = false;

	public PollThread(SERVER_STATE expectedState, IServerStatePoller poller, IPollResultListener listener, IServer server, int timeout) {
		super(NLS.bind("{0} - Server Poller", server.getName()));
		this.expectedState = expectedState;
		this.poller = poller;
		this.server = server;
		this.listener = listener;
		this.aborted = false;
		this.timeout = timeout;
	}


	@Override
	public void run() {
		// Poller not found. Abort
		if (poller == null) {
			LOG.error("No poller defined, aborting polling.");
			alertListener(getOpposite(expectedState));
			return;
		}

		try {
			poller.beginPolling(getServer(), expectedState);
		} catch(Exception e) {
			LOG.error("Error occurred while polling, aborting.", e);
			cancel(e.getMessage(), CANCELATION_CAUSE.FAILED);
		}
		
		try {
			int maxWait = getTimeout();
			long startTime = System.currentTimeMillis();
			boolean done = false;
			boolean serverStartedOrStopped = false;
			
			// begin the loop; ask the poller every so often
			while (!serverStartedOrStopped
					&& !isAborted()
					&& !done
					&& !timeoutReached(startTime, maxWait)) {
				try {
					Thread.sleep(POLL_DELAY);
					done = poller.isComplete();
				} catch (PollingException | InterruptedException e) {
					// abort and put the message in event log
					if( e instanceof InterruptedException) {
						Thread.currentThread().interrupt();
						cancel(e.getMessage(), CANCELATION_CAUSE.CANCEL);
					} else {
						cancel(e.getMessage(), CANCELATION_CAUSE.FAILED);
					}
					return;
				} catch (RequiresInfoException rie) {
					// This way each request for new info is checked only once.
					if (!rie.getChecked()) {
						rie.setChecked();
						fireRequestCredentials(expectedState, poller);
					}
				}
				serverStartedOrStopped = isStartedOrStopped(server.getDelegate());
			}
			if (serverStartedOrStopped) {
				// we stopped. Did we abort?
				handleUncertainTermination();
			} else if (done) {
				// the poller has an answer
				handlePollerHasAnswer();
			} else {
				// we timed out. get response from preferences
				handleTimeoutTermination();
			}
		} catch( RuntimeException re) {
			LOG.error("Error occurred while polling, aborting.", re);
			cancel(re.getMessage(), CANCELATION_CAUSE.FAILED);
		}
	}

	private SERVER_STATE getOpposite(SERVER_STATE state) {
		switch(state) {
		case UNKNOWN:
			// There's no opposite to unknown... 
			return SERVER_STATE.UNKNOWN;
		case UP:
			return SERVER_STATE.DOWN;
		case DOWN:
		default:
			return SERVER_STATE.UP;
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
			alertListener(getOpposite(expectedState));
		} catch (RequiresInfoException rie) {
			// You don't have an answer... liar!
		}
	}

	private void handleTimeoutTermination() {
		cancelPoller(CANCELATION_CAUSE.TIMEOUT_REACHED);
		TIMEOUT_BEHAVIOR beh = poller.getTimeoutBehavior();
		if( beh != null ) {
			SERVER_STATE state = beh.getServerState(expectedState);
			alertListener(state);
		} else {
			// We have to treat it as a fail if the poller doesn't implement this
			if( expectedState == SERVER_STATE.DOWN) {
				alertListener(SERVER_STATE.UP);
			} else {
				alertListener(SERVER_STATE.DOWN);
			}
		}
	}
	
	private void handleUncertainTermination() {
		int state = server.getDelegate().getServerRunState();
		boolean success = false;
		if (expectedState == SERVER_STATE.UP) {
			success = (state == IServerDelegate.STATE_STARTED);
		} else {
			success = (state == IServerDelegate.STATE_STOPPED);
		}

		poller.cancel(success ? 
				CANCELATION_CAUSE.SUCCESS
				: CANCELATION_CAUSE.FAILED);
	}
	
	private boolean timeoutReached(long startTime, int maxWait) {
		return System.currentTimeMillis() >= (startTime + maxWait);
	}

	private boolean isStartedOrStopped(IServerDelegate delegate) {
		int state = delegate.getServerRunState();
		switch(state) {
		case IServerDelegate.STATE_STARTED:
		case IServerDelegate.STATE_STOPPED:
			return true;
		default:
			return false;
		}
	}

	public void cancel() {
		cancel(null, IServerStatePoller.CANCELATION_CAUSE.CANCEL);
	}

	private synchronized boolean isAborted() {
		return aborted;
	}
	
	private synchronized void setAborted() {
		this.aborted = true;
	}
	
	private synchronized boolean isListenerAlerted() {
		return listenerAlerted;
	}
	
	private synchronized void setListenerAlerted() {
		this.listenerAlerted = true;
	}
	
	protected void cancel(String message, IServerStatePoller.CANCELATION_CAUSE cause) {
		// If we haven't aborted already
		if( !isAborted() && !isListenerAlerted()) {
			setAborted();
			cancelPoller(cause);
			log(message, cause);
			alertListener(getOpposite(expectedState));
		}
	}

	private void cancelPoller(IServerStatePoller.CANCELATION_CAUSE cause) {
		if (poller != null) {
			poller.cancel(cause);
		}
	}

	private void log(String message, IServerStatePoller.CANCELATION_CAUSE cause) {
		if (cause != null) {
			cause.log(message, server, LOG);
		} else {
			LOG.info(NLS.bind("Polling server {0} cancelled", server.getName()));
		}
	}

	protected void alertListener(SERVER_STATE currentState) {
		if (listener == null) {
			return;
		}
		if( !isListenerAlerted()) {
			setListenerAlerted();
			if (currentState != expectedState) {
				listener.stateNotAsserted(expectedState, currentState);
			} else {
				listener.stateAsserted(expectedState, currentState);
			}
		}
	}
	
	protected int getTimeout() {
		return timeout;
	}

	protected IServer getServer() {
		return server;
	}

	public static void fireRequestCredentials(SERVER_STATE expectedState, IServerStatePoller poller) {
		// TODO We need to find a way to accomplish this
		//PollThreadUtils.requestCredentialsAsynch(poller, poller.getRequiredProperties());
	}
}