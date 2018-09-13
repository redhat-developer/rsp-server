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
