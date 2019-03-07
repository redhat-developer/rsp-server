/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ModifyDeployableRequest;
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.itests.RSPCase;
import org.jboss.tools.rsp.itests.util.DeploymentGeneration;
import org.jboss.tools.rsp.itests.util.DummyClient;
import org.junit.Test;

/**
 *
 * @author jrichter
 */
public class WildFlyPublishingTest extends RSPCase {
    
    private static final String WAR_FILENAME = "test9.war";
    private final DummyClient client = launcher.getClient();
    
    @Test
    public void testStartServer() throws Exception {
        createServer(WILDFLY_ROOT, "wildfly6a");
        Map<String, Object> attr = new HashMap<>();
        attr.put("server.home.dir", WILDFLY_ROOT);
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wildflyType.getId(), "wildfly6a", attr), "run");
        
        StartServerResponse response = serverProxy.startServerAsync(params).get();
        
        assertEquals(0, response.getStatus().getSeverity());
        assertEquals("ok", response.getStatus().getMessage());
        waitForServerState("started", 10);
        
        ServerHandle handle = new ServerHandle("wildfly6a", wildflyType);
        File war = createWar();
        DeployableReference deployable = new DeployableReference(war.getAbsolutePath(), war.getAbsolutePath());
        ModifyDeployableRequest req = new ModifyDeployableRequest(handle, deployable);
        serverProxy.addDeployable(req);
        
        ServerState state = waitForNonNullServerState(5);
        assertNotNull(state);
        List<DeployableState> list = state.getDeployableStates();
        assertNotNull(list);
        assertEquals(1, list.size());
        DeployableState ds1 = list.get(0);
        assertNotNull(ds1);
        assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_ADD, ds1.getPublishState());
        assertEquals(ServerManagementAPIConstants.STATE_UNKNOWN, ds1.getState());
        
        // TODO publish
        PublishServerRequest pubReq = new PublishServerRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);
        serverProxy.publish(pubReq);
        state = waitForNonNullServerState(5);
        assertNotNull(state);
        list = state.getDeployableStates();
        assertNotNull(list);
        assertEquals(1, list.size());
        ds1 = list.get(0);
        assertNotNull(ds1);
        assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_NONE, ds1.getPublishState());
        assertEquals(ServerManagementAPIConstants.STATE_STARTED, ds1.getState());
        
        // TODO touch the file
        war.setLastModified(System.currentTimeMillis());
        state = waitForNonNullServerState(5);
        assertNotNull(state);
        list = state.getDeployableStates();
        assertNotNull(list);
        assertEquals(1, list.size());
        ds1 = list.get(0);
        assertNotNull(ds1);
        assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, ds1.getPublishState());
        assertEquals(ServerManagementAPIConstants.STATE_STARTED, ds1.getState());

        
        ModifyDeployableRequest req2 = new ModifyDeployableRequest(handle, deployable);
        serverProxy.removeDeployable(req2);
        state = waitForNonNullServerState(5);
        assertNotNull(state);
        list = state.getDeployableStates();
        assertNotNull(list);
        assertEquals(1, list.size());
        ds1 = list.get(0);
        assertNotNull(ds1);
        assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE, ds1.getPublishState());
        assertEquals(ServerManagementAPIConstants.STATE_STARTED, ds1.getState());

        // TODO publish
        pubReq = new PublishServerRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);
        serverProxy.publish(pubReq);
        state = waitForNonNullServerState(5);
        assertNotNull(state);
        list = state.getDeployableStates();
        assertNotNull(list);
        assertEquals(0, list.size());
                
        serverProxy.stopServerAsync(new StopServerAttributes("wildfly6a", true)).get();
        waitForServerState("stopped", 10);
    }

	protected File createWar() {
		Path deployments = null;
		File war = null;
		try {
			deployments = Files.createTempDirectory(getClass().getName() + "5");
			war = deployments.resolve(WAR_FILENAME).toFile();
			if (!(new DeploymentGeneration().createWar(war))) {
				fail();
			}
		} catch (IOException e) {}
		return war != null && war.exists() && war.isFile() ? war : null;
	}
	
    private void waitForServerState(String serverState, int timeout) throws Exception {
        int tries = timeout;
        
        while(tries > 0) {
            tries--;
            String s = client.getStateString();
            System.out.println("State String: " + s);
            if (serverState.equals(s)) {
            	client.clearState();
                return;
            }
            Thread.sleep(1000);
        }
        throw new AssertionError("Waiting for server state to change to " + serverState + " timed out");
    }

    private ServerState waitForNonNullServerState(int timeout) throws Exception {
        int tries = timeout;
        
        while(tries > 0) {
            tries--;
        	ServerState ret = client.getStateObject();
            if (ret != null) {
            	client.clearState();
                return ret;
            }
            Thread.sleep(1000);
        }
        throw new AssertionError("Waiting for server state to change to a non-null value");
    }
}
