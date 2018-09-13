/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import java.util.List;
import java.util.Properties;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public interface IServerStatePoller {
	public static final int POLLING_CODE = 1 << 24;
	public static final int POLLER_MASK = 0xFF << 16;
	
	public enum SERVER_STATE {
		UP, DOWN, UNKNOWN
	}

	public enum CANCELATION_CAUSE {
		CANCEL, TIMEOUT_REACHED, SUCCESS, FAILED
	}
	
	public enum TIMEOUT_BEHAVIOR {
		SUCCEED, //If we timeout, set new state to expected state 
		FAIL //If we timeout, set new state to old state
	}

	
	/**
	 * Begin polling the provided server for its state, while the server transitions into expectedState.
	 * 
	 * @param server
	 * @param expectedState one of IServerStatePoller.SERVER_UP or IServerStatePoller.SERVER_DOWN
	 * @throws PollingException
	 */
	public void beginPolling(IServer server, SERVER_STATE expectedState) throws PollingException;
	
	/**
	 * Check whether the polling has completed. 
	 * What this means is has the expected transition in state been recognized?
	 * 
	 * @return
	 * @throws PollingException
	 * @throws RequiresInfoException
	 */
	public boolean isComplete() throws PollingException, RequiresInfoException;
	
	/**
	 * Called only after poller is "done".  
	 * Should return cached final state rather than poll again. 
	 *  
	 * @return
	 * @throws PollingException
	 * @throws RequiresInfoException
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
