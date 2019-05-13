/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import static org.jboss.tools.rsp.itests.util.ServerStateUtil.waitForNonNullServerState;
import static org.jboss.tools.rsp.itests.util.ServerStateUtil.waitForServerState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerDeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.itests.RSPCase;
import org.jboss.tools.rsp.itests.util.DeploymentGeneration;
import org.jboss.tools.rsp.itests.util.DummyClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jrichter
 */
public class WildFlyPublishingTest extends RSPCase {

	private static final String SERVER_ID = "wildfly6a";
	private static final String WAR_FILENAME = "test9.war";
    private final DummyClient client = launcher.getClient();

    @Before
    public void before() throws Exception {
        createServer(WILDFLY_ROOT, SERVER_ID);
    }

    @After
    public void after() throws Exception {
    	deleteServer(SERVER_ID);
    }

    @Test
    public void testStartServer() throws Exception, ExecutionException {
        Map<String, Object> attr = new HashMap<>();
        attr.put("server.home.dir", WILDFLY_ROOT);
        LaunchParameters params = new LaunchParameters(
                new ServerAttributes(wildflyType.getId(), SERVER_ID, attr), "run");
        
        StartServerResponse response = serverProxy.startServerAsync(params).get();
        
        assertTrue("server wasnt successfully started: " + response.getStatus().getMessage(), response.getStatus().isOK());
        assertEquals("ok", response.getStatus().getMessage());
        waitForServerState(ServerManagementAPIConstants.STATE_STARTED, 20, client);
        
        ServerHandle handle = new ServerHandle(SERVER_ID, wildflyType);
        File war = createWar();
        DeployableReference deployable = new DeployableReference(war.getAbsolutePath(), war.getAbsolutePath());
        ServerDeployableReference req = new ServerDeployableReference(handle, deployable);
        serverProxy.addDeployable(req);
        
        ServerState state = waitForNonNullServerState(20, client);
        assertNotNull(state);
        DeployableState ds1 = getDeployableState(deployable, state.getDeployableStates());
        assertNotNull("state for deployable " + deployable.getPath() + " wasnt found.", ds1);
        assertNotNull(ds1);
        assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_ADD, ds1.getPublishState());
        assertEquals(ServerManagementAPIConstants.STATE_UNKNOWN, ds1.getState());
        
        // TODO publish
        PublishServerRequest pubReq = new PublishServerRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);
        serverProxy.publish(pubReq);
        state = waitForNonNullServerState(20, client);
        assertNotNull(state);
        ds1 = getDeployableState(deployable, state.getDeployableStates());
        assertNotNull("state for deployable " + deployable.getPath() + " wasnt found.", ds1);
        assertNotNull(ds1);
        assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_NONE, ds1.getPublishState());
        assertEquals(ServerManagementAPIConstants.STATE_STARTED, ds1.getState());
        
        // TODO touch the file
        war.setLastModified(System.currentTimeMillis());
        state = waitForNonNullServerState(20, client);
        assertNotNull(state);
        ds1 = getDeployableState(deployable, state.getDeployableStates());
        assertNotNull("state for deployable " + deployable.getPath() + " wasnt found.", ds1);
        assertNotNull(ds1);
        assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, ds1.getPublishState());
        assertEquals(ServerManagementAPIConstants.STATE_STARTED, ds1.getState());

        serverProxy.removeDeployable(new ServerDeployableReference(handle,  deployable));
        state = waitForNonNullServerState(20, client);
        assertNotNull(state);
        ds1 = getDeployableState(deployable, state.getDeployableStates());
        assertNotNull("state for deployable " + deployable.getPath() + " wasnt found.", ds1);
        assertNotNull(ds1);
        assertEquals(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE, ds1.getPublishState());
        assertEquals(ServerManagementAPIConstants.STATE_STARTED, ds1.getState());

        // TODO publish
        pubReq = new PublishServerRequest(handle, ServerManagementAPIConstants.PUBLISH_FULL);
        serverProxy.publish(pubReq);
        state = waitForNonNullServerState(20, client);
        assertNotNull(state);
        ds1 = getDeployableState(deployable, state.getDeployableStates());
        assertNull("state for deployable " + deployable.getPath() + " was still found but shouldn't.", ds1);
                
        serverProxy.stopServerAsync(new StopServerAttributes(SERVER_ID, true)).get();       
        waitForServerState(ServerManagementAPIConstants.STATE_STOPPED, 10, client);
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

	public DeployableState getDeployableState(DeployableReference reference, List<DeployableState> states) {
		if (states == null || states.isEmpty()) {
			return null;
		}
		
		return states.stream()
				.filter(state -> reference.equals(state.getReference()))
				.findFirst()
				.orElse(null);
	}    
}
