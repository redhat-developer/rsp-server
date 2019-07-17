/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.StringPrompt;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientImpl;

/**
 *
 * @author jrichter
 */
public class DummyClient extends ServerManagementClientImpl {

    private String stateString;
    private ServerState state;

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
        this.state = state;
    }

    public String getStateString() {
        return stateString;
    }
    
    public ServerState getStateObject() {
    	return state;
    }
    
    private List<String> nextPromptStrings = new ArrayList<String>();
    public synchronized void addPromptStringReply(String s) {
    	nextPromptStrings.add(s);
    }
    
    private synchronized String getNextPromptString() {
    	if( nextPromptStrings.size() > 0 ) {
    		return nextPromptStrings.remove(0);
    	}
    	return null;
    }
    
    @Override
    public CompletableFuture<String> promptString(StringPrompt prompt) {
    	return CompletableFuture.completedFuture(getNextPromptString());
    }
}
