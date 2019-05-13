/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests;

import static org.junit.Assert.fail;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.itests.util.DummyClientLauncher;
import org.jboss.tools.rsp.itests.util.RSPServerHandler;
import org.jboss.tools.rsp.itests.util.RSPServerUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author jrichter, odockal
 */
public abstract class RSPCase {

	protected static String WILDFLY_SERVER_ID;
	protected static String WILDFLY_PATH;
	protected static String WILDFLY_VERSION;
	protected static String WILDFLY_ROOT;
	protected static ServerType wildflyType;
	protected static final String INVALID_PARAM = "Parameter is invalid. It may be null, missing required fields, or unacceptable values.";

	protected static DummyClientLauncher launcher;
	protected static RSPServer serverProxy;

	public static void initializeProperties() {
		WILDFLY_SERVER_ID = getProperty("server.type.id");
		WILDFLY_PATH = getProperty("server.path");
		WILDFLY_VERSION = getProperty("server.version");
		WILDFLY_ROOT = Paths.get(WILDFLY_PATH).toAbsolutePath().toString();
		wildflyType = RSPServerUtility.getServerType(WILDFLY_SERVER_ID);
	}

	public static String getProperty(String property) {
		String val = System.getProperty(property);
		if (val == null) {
			fail("System property " + property + " value is not defined");
		}
		return val;
	}

	@BeforeClass
	public static void prepare() throws Exception {
		initializeProperties();
		RSPServerHandler.clearServerData(true);
		RSPServerHandler.prepareServer();
		RSPServerHandler.startServer();
		launcher = new DummyClientLauncher("localhost", 27511);
		launcher.launch();
		serverProxy = launcher.getServerProxy();
	}

	@AfterClass
	public static void dispose() throws Exception {
		if (launcher != null) {
			launcher.closeConnection();
		}
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

	protected void deleteServer(String id) throws Exception {
		CompletableFuture<List<ServerHandle>> handles = serverProxy.getServerHandles();
		ServerHandle handle = handles.get().stream()
			.filter(server -> id.equals(server.getId()))
			.findFirst().orElse(null);
		if (handle == null) {
			return;
		}
		serverProxy.deleteServer(handle);
	}

}
