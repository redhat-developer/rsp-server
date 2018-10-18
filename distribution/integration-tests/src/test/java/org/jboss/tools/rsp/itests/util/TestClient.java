/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.util;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientImpl;

/**
 *
 * @author jrichter
 */
public class TestClient extends ServerManagementClientImpl {

    private String stateString;

    @Override
    public void serverStateChanged(ServerState state) {
        switch (state.getState()) {
            case ServerManagementAPIConstants.STATE_STARTED:
                stateString = "started";
                break;
            case ServerManagementAPIConstants.STATE_STARTING:
                stateString = "starting";
                break;
            case ServerManagementAPIConstants.STATE_STOPPED:
                stateString = "stopped";
                break;
            case ServerManagementAPIConstants.STATE_STOPPING:
                stateString = "stopping";
                break;
        }
    }

    public String getState() {
        return stateString;
    }
}
