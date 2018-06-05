package org.jboss.tools.ssp.server.spi.model.polling;

import org.jboss.tools.ssp.server.spi.model.polling.IServerStatePoller.SERVER_STATE;

public interface IPollResultListener {

	/**
	 * Called if the poller did reach the expected state
	 * @param expectedState the expected state
	 * @param currentState  the actual state
	 */
	public void stateAsserted(SERVER_STATE state, SERVER_STATE currentState);

	/**
	 * Called if the poller did NOT reach the expected state
	 * @param expectedState the expected state
	 * @param currentState  the actual state
	 */
	public void stateNotAsserted(SERVER_STATE state, SERVER_STATE currentState);	

}
