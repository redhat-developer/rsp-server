/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.util;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerState;

public class ServerStateUtil {

	private static final long WAIT_FOR_SERVERSTATE = 3000;
    private static final int NO_STATE = -1;

	public static String toStateString(ServerState state) {
		if (state == null) {
			return "null state";
		}
		
		return toString(state.getState());
	}

	public static String toString(int state) {
		switch(state) {
		case ServerManagementAPIConstants.STATE_STARTING:
			return "starting";
		case ServerManagementAPIConstants.STATE_STARTED:
			return "started";
		case ServerManagementAPIConstants.STATE_STOPPING:
			return "stopping";
		default:
		case ServerManagementAPIConstants.STATE_UNKNOWN:
			return "unknown";
			
		}
	}

    public static ServerState waitForNonNullServerState(int attempts, DummyClient client) throws Exception {
    	return waitForServerState(NO_STATE, attempts, client);
    }

    public static ServerState waitForServerState(int expected, int attempts, DummyClient client) throws Exception {
        int tries = attempts;
        
        while(tries > 0) {
            tries--;
            ServerState s = client.getStateObject();
			if (s != null) {
				if (expected == NO_STATE
						|| expected == s.getState()) {
					client.clearState();
					return s;
				}
			}
            System.out.println("Server state: " + ServerStateUtil.toStateString(s));
            Thread.sleep(WAIT_FOR_SERVERSTATE);
        }
        throw new AssertionError("Waiting for server state to change to " + expected + " timed out");
    }
}
