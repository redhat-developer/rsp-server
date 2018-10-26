/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.itests.util.RSPServerHandler;
import org.jboss.tools.rsp.itests.util.TestClientLauncher;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author jrichter
 */
public class RSPTestCase {
    protected static final String WILDFLY13_ROOT = Paths.get("target/wildfly-13.0.0.Final").toAbsolutePath().toString();    
    protected static final ServerType wfly13Type = new ServerType("org.jboss.ide.eclipse.as.wildfly.130",
            "WildFly 13.x", "A server adapter capable of discovering and controlling a WildFly 13.x runtime instance.");
    protected static final String INVALID_PARAM =
            "Parameter is invalid. It may be null, missing required fields, or unacceptable values.";
    
    protected static TestClientLauncher launcher;
    protected static RSPServer serverProxy;
    
    @BeforeClass
    public static void prepare() throws Exception {
        RSPServerHandler.clearServerData(true);
        RSPServerHandler.prepareServer();
        RSPServerHandler.startServer();
        launcher = new TestClientLauncher("localhost", 27511);
        launcher.launch();
        serverProxy = launcher.getServerProxy();
    }
    
    @AfterClass
    public static void dispose() throws Exception {
        launcher.closeConnection();
        RSPServerHandler.stopServer();
        RSPServerHandler.restoreBackupData(true);
    }
    
    protected Status createServer(String location, String id) throws Exception {
        ServerBean bean = serverProxy.findServerBeans(new DiscoveryPath(location)).get().get(0);
        Map<String, Object> attr = new HashMap<>();
        attr.put("server.home.dir", bean.getLocation());
        ServerAttributes serverAttr = new ServerAttributes(bean.getServerAdapterTypeId(), id, attr);
        return serverProxy.createServer(serverAttr).get().getStatus();
    }
}
