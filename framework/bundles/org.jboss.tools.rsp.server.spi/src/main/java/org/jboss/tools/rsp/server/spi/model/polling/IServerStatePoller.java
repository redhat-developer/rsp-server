/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import java.util.List;
import java.util.Properties;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.slf4j.Logger;

public interface IServerStatePoller {

	public static final int POLLING_CODE = 1 << 24;
	public static final int POLLER_MASK = 0xFF << 16;
	
	public enum SERVER_STATE {
		UP, DOWN, UNKNOWN
	}

	public enum CANCELATION_CAUSE {
		CANCEL {
			@Override
			String getExplanation() {
				return "Aborted polling";
			}

			@Override
			void log(String message, IServer server, Logger log) {
				log.info(message);
			}			
		}, 
		
		TIMEOUT_REACHED {
			@Override
			String getExplanation() {
				return "Timeout reached, aborted polling";
			}

			@Override
			void log(String message, IServer server, Logger log) {
				log.info(message);
			}
		}, 
		
		SUCCESS {
			@Override
			String getExplanation() {
				return "Polling succeeded, aborting poller";
			}

			@Override
			void log(String message, IServer server, Logger log) {
				log.info(message);
			}
			
		}, 
		
		FAILED {
			@Override
			String getExplanation() {
				return "Failed polling, aborting poller";
			}

			@Override
			void log(String message, IServer server, Logger log) {
				log.error(message);
			}

		};
		
		public String getMessage(String message, IServer server) {
			if (message != null) {
				return NLS.bind(getExplanation() + " for server {0}: {1}", server.getName(), message);
			} else {
				return NLS.bind(getExplanation() + " for server {0}", server.getName());
			}
		}

		abstract String getExplanation();
		abstract void log(String message, IServer server, Logger log);
	}
	
	public enum TIMEOUT_BEHAVIOR {
		SUCCEED { // if we timeout, return expected state

			@Override
			public SERVER_STATE getServerState(SERVER_STATE expectedState) {
				switch(expectedState) {
				case UP:
					return SERVER_STATE.UP;
				case DOWN:
					return SERVER_STATE.DOWN;
				case UNKNOWN:
				default:
					return SERVER_STATE.UNKNOWN;
				}
			}
			
		},  
		FAIL { // if we timeout, return the inversion of the expected state

			@Override
			public SERVER_STATE getServerState(SERVER_STATE expectedState) {
				switch(expectedState) {
				case UP:
					return SERVER_STATE.DOWN;
				case DOWN:
					return SERVER_STATE.UP;
				case UNKNOWN:
				default:
					return SERVER_STATE.UNKNOWN;
				}
			}
		};		
		
		public abstract SERVER_STATE getServerState(SERVER_STATE expectedState);
	}

	
	/**
	 * Begins polling the provided server for its state, while the server transitions into expectedState.
	 * 
	 * @param server
	 * @param expectedState one of IServerStatePoller#SERVER_UP or IServerStatePoller#SERVER_DOWN
	 * @throws PollingException
	 */
	public void beginPolling(IServer server, SERVER_STATE expectedState) throws PollingException;
	
	/**
	 * Returns {@code true} if the polling has completed. Returns {@code false} otherwise.
	 * 
	 * @return
	 * @throws PollingException, RequiresInfoException
	 */
	public boolean isComplete() throws PollingException, RequiresInfoException;
	
	/**
	 * Returns the state that resulted from polling. Returns {@code null} before the
	 * polling was executed, {@link SERVER_STATE} afterwards. Called only after
	 * poller is "done". Should return cached final state rather than poll again.
	 * 
	 * @return
	 * @throws PollingException, RequiresInfoException
	 */
	public SERVER_STATE getState() throws PollingException, RequiresInfoException; 
	/*
	 * clean up any resources / processes. Will ALWAYS be called
	 */
	public void cleanup();

	/**
	 * Cancel the polling. 
	 * @param type CANCEL or TIMEOUT_REACHED
	 */
	public void cancel(CANCELATION_CAUSE cause);    
	
	/**
	 * Returns a TIMEOUT_BEHAVIOR_XXX constant
	 * @return
	 */
	public TIMEOUT_BEHAVIOR getTimeoutBehavior();
	
	

	/**
	 * Get a list of required properties for these credentials
	 * Ex:  username, password, security realm, etc
	 * @return
	 */
	public List<String> getRequiredProperties();
	
	/**
	 * Provides the required credentials to the INeedCredentials object
	 * @param credentials  A property map, mapping each String property to a String value
	 */
	public void provideCredentials(Properties credentials);
	
	public IServer getServer();

	/**
	 * Get the current state of the server via a forced 
	 * poll request. 
	 * 
	 * This API is required because the structure of the poller API
	 * allows some pollers to launch their own threads, and respond to 
	 * getState() as the answer comes in. 
	 * 
	 * This method, in contrast, initiates an immediate and synchronous 
	 * poll attempt to determine the current state. 
	 * 
	 * @return IStatus.OK if a server is completely started,
	 * 			IStatus.INFO if a server is in various states of startup / shutdown or unknown,
	 * 			or IStatus.ERROR if a server is definitely not up. 
	 */
	public SERVER_STATE getCurrentStateSynchronous(IServer server);
	
}
