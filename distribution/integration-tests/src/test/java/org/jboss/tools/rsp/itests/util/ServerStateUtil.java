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

/**
 * Utility class for server state manipulation.
 * @author adietish, odockal
 *
 */
public class ServerStateUtil {

	public static final String OS = System.getProperty("os.name");
	private static final long WAIT_FOR_SERVERSTATE = OS.indexOf("win") >= 0 ? 5000 : 3000;
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
        
        ServerState s = null;
        while(tries > 0) {
            tries--;
            s = client.getStateObject();
			if (s != null) {
				if (expected == NO_STATE
						|| expected == s.getState()) {
					return s;
				}
			}
            Thread.sleep(WAIT_FOR_SERVERSTATE);
        }
        throw new AssertionError("Waiting for server state to change to " + expected + " timed out. Last state was " + s.getState());
    }
    
    public static ServerState waitForDeployables(int attempts, DummyClient client) throws Exception {
        return waitForDeployablePublishState(NO_STATE, attempts, client);
    }
    
    public static ServerState waitForDeployablePublishState(int expected, int attempts, DummyClient client) throws Exception {
        int tries = attempts;
        
        while(tries > 0) {
            tries--;
            ServerState s = client.getStateObject();
			if (s != null) {
				if (s.getDeployableStates().size() > 0 && 
						(expected == s.getDeployableStates().get(0).getPublishState() || expected == NO_STATE)) {
					return s;
				}
			}
            Thread.sleep(WAIT_FOR_SERVERSTATE);
        }
        throw new AssertionError("Waiting for server publish state to change to " + expected + " timed out");
    }
    
}
