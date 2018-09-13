/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.SERVER_STATE;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

/**
 * @author Rob Stryker
 * @author André Dietisheim
 */
public class PollThreadUtils {
	private static final String PROP_POLL_THREAD_KEY = "org.jboss.tools.rsp.server.spi.model.polling.PollThreadKey";
	
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
		if (pollThread != null) {
			if (message != null) {
				pollThread.cancel(message);
			} else {
				pollThread.cancel();
			}
		}
	}


	/**
	 * Stops the given poll thread and creates a new poll thread for the given
	 * expected state, poller, result listener and server.
	 * 
	 * @param expectedState the state to wait for 
	 * @param poller the poller to use to wait for the expected state
	 * @param pollThread the poll thread to stop
	 * @param listener the listener to inform about the polling result 
	 * @return the new poll thread
	 */
	public static PollThread pollServer(SERVER_STATE expectedState, IServerStatePoller poller, PollThread currentPollThread,
			IPollResultListener listener, IServer server, int timeout) {
		stopPolling(currentPollThread);
		PollThread newPollThread = new PollThread(expectedState, poller, listener, server, timeout);
		newPollThread.start();
		return newPollThread;
	}
	
	/*
	 * A solution needs to be found here. 
	 * Should ideally use the poller that the server says is its poller,
	 * but some pollers such as timeout poller cannot actively check
	 */
	public static SERVER_STATE isServerStarted(IServer server,IServerStatePoller poller ) {
		SERVER_STATE started = ((IServerStatePoller)poller).getCurrentStateSynchronous(server);
		return started;
	}
	

	public static void pollServer(IServer server, SERVER_STATE expectedState, IServerStatePoller poller, IPollResultListener listener) {
		pollServer(server, expectedState, poller, listener, 2*60*1000);
	}

	public static void pollServer(IServer server, SERVER_STATE expectedState, IServerStatePoller poller,
			IPollResultListener listener, int timeout) {
		IServerDelegate del = server.getDelegate();
		PollThread pollThread = (PollThread)del.getSharedData(PROP_POLL_THREAD_KEY);
		pollThread = PollThreadUtils.pollServer(expectedState, poller, pollThread, listener, server, timeout);
		del.putSharedData(PROP_POLL_THREAD_KEY, pollThread);
	}
	

	
	
//
//	/**
//	 * The credential provider is alerted that credentials are needed. 
//	 * The response may come at any time. 
//	 * 
//	 * @param requester
//	 * @param requiredProps
//	 */
//	public static void requestCredentialsAsynch(final INeedCredentials requester, final List<String> requiredProps) {
//		new Thread() {
//			public void run() {
//				requestCredentialsSynchronous(requester, requiredProps);
//			}
//		}.start();
//	}
//	
//	/**
//	 * The credential provider is alerted that credentials are needed. 
//	 * The calling thread will block until this method is finished. 
//	 * The requester will be told of its credentials by the provider.
//	 * 
//	 * @param requester
//	 * @param requiredProps
//	 * @return
//	 */
//
//	public static void requestCredentialsSynchronous(final INeedCredentials requester, List<String> requiredProps) {
//		IProvideCredentials provider = ExtensionManager.getDefault()
//				.getFirstCredentialProvider(requester, requiredProps);
//		provider.handle(requester, requiredProps);
//	}
//
//
//	/**
//	 * The credential provider is alerted that credentials are needed. 
//	 * The calling thread will block until this method is finished. 
//	 * A dummy requester is created, which will receive the properties. 
//	 * It will then return them to the caller directly. 
//	 * 
//	 * @param requester
//	 * @param requiredProps
//	 * @return Properties 
//	 */
//
//	public static Properties requestCredentialsSynchronous(final IServerProvider server, List<String> requiredProps) {
//		NeedCredentials requester = new NeedCredentials(server.getServer(), requiredProps);
//		IProvideCredentials provider = ExtensionManager.getDefault()
//				.getFirstCredentialProvider(requester, requiredProps);
//		provider.handle(requester, requiredProps);
//		return requester.getReturnedCredentials();
//	}
//	
//	public static class NeedCredentials implements INeedCredentials {
//		private IServer server;
//		private List<String> requiredProps;
//		private Properties returnedCredentials;
//		public NeedCredentials(IServer server, List<String> requiredProps) {
//			this.server = server;
//			this.requiredProps = requiredProps;
//		}
//		public IServer getServer() {
//			return server;
//		}
//		public List<String> getRequiredProperties() {
//			return requiredProps;
//		}
//		public void provideCredentials(Properties credentials) {
//			returnedCredentials = credentials;
//		}
//		public Properties getReturnedCredentials() {
//			return returnedCredentials;
//		}
//	}

	
}
