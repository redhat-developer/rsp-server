/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests;

import static org.jboss.tools.rsp.itests.util.ServerStateUtil.waitForServerState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.itests.util.DummyClient;
import org.jboss.tools.rsp.itests.util.DummyClientLauncher;
import org.jboss.tools.rsp.itests.util.RSPServerHandler;
import org.jboss.tools.rsp.itests.util.RSPServerUtility;
import org.jboss.tools.rsp.itests.util.ServerStateUtil;
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
	protected static final String MISSING_SERVER_ID = "Invalid Request: Request must include server id.";
	protected static final String SERVER_TYPE_NOT_FOUND = "Invalid Request: Server type not found.";
	protected static final String NULL_SERVER_ACTION_REQUEST = "Invalid Request: Request cannot be null.";
	protected static final String MISSING_SERVER_TYPE = "Invalid Request: Request must include server type.";
	protected static final String MISSING_SERVER_TYPE_ID = "Invalid Request: Request must include server type id.";
	protected static final String MISSING_SERVER_HANDLE = "Invalid Request: Request must include server handle.";
	
	protected static final String MODE_DEBUG = "debug";
	protected static final String MODE_RUN = "run";
	protected static final String STATUS_MESSAGE_OK = "ok";
	protected static final int SERVER_OPERATION_TIMEOUT = 10000;
	protected static final int REQUEST_TIMEOUT = 4000;

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
	
	public static <V> V timeConsumption(Callable<V> callable) throws Exception {
		return timeConsumption(callable, null, 4);
	}
	
	public static <V> V timeConsumption(Callable<V> callable, int level) throws Exception {
		return timeConsumption(callable, new File(getProperty("user.dir") + "/" + "output.log"), level);
	}
	
	/**
	 * This method logs out or saves to the file output of time consumption
	 * for anonymous callable implementation of overrided call method. 
	 * Serves mainly for debug purposes now. 
	 * 
	 * @param <V> generic type that is used in callable type definition
	 * @param callable instance of callable class on which the call function is executed in method body and time is measured on
	 * @param file file to write output to
	 * @return object of generic type V that is type definition of callable class
	 * @throws Exception
	 */
	public static <V> V timeConsumption(Callable<V> callable, File file, int levelOfCaller) throws Exception {
		long startTime = System.currentTimeMillis();
		StackTraceElement [] trace = Thread.currentThread().getStackTrace();
		StackTraceElement impl = trace[levelOfCaller-1];
		StackTraceElement caller = trace[levelOfCaller];
		
		V result = callable.call();
		
		String output = "Calling " + caller.getClassName() + "." + caller.getMethodName() + " of " 
						+ impl.getClassName() + "." + impl.getMethodName() 
						+ " took: " + (System.currentTimeMillis() - startTime) / 1000.0 + " s\r\n";
		if (file != null) {
			try(FileWriter fw = new FileWriter(file, true)) {
				fw.write(output);
				fw.flush();
			}
		} else {
			System.out.println(output);
		}
		return result;
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

	protected void startServer(DummyClient client, String id) throws Exception {
		Map<String, Object> attr = new HashMap<>();
		attr.put("server.home.dir", WILDFLY_ROOT);
		LaunchParameters params = new LaunchParameters(new ServerAttributes(wildflyType.getId(), id, attr), MODE_RUN);
		
		StartServerResponse response = timeConsumption(new Callable<StartServerResponse>() {
			public StartServerResponse call() throws InterruptedException, ExecutionException, TimeoutException {
				return serverProxy.startServerAsync(params).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
			}
		});
		

		assertEquals(0, response.getStatus().getSeverity());
		assertEquals(STATUS_MESSAGE_OK, response.getStatus().getMessage());
		ServerStateUtil.waitForServerState(ServerManagementAPIConstants.STATE_STARTED, 10, client);
	}

	protected void stopServer(DummyClient client, String id) throws Exception {
		if (client.getStateObject() != null
				&& client.getStateObject().getState() != ServerManagementAPIConstants.STATE_STOPPED) {
			
			Status status = timeConsumption(new Callable<Status>() {
				public Status call() throws InterruptedException, ExecutionException, TimeoutException {
					return serverProxy.stopServerAsync(new StopServerAttributes(id, true)).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
				}
			});
			assertEquals(Status.OK, status.getSeverity());
			assertEquals(STATUS_MESSAGE_OK, status.getMessage());
			waitForServerState(ServerManagementAPIConstants.STATE_STOPPED, 10, client);
		} else {
			System.out.println("Server is already stopped");
		}
	}

	protected Status createServer(String location, String id) throws Exception {
		System.out.println("Creating server " + id + " at location " + location);
		System.out.println("Location " + location + "exists? " + new File(location).exists());
		
		ServerBean bean = serverProxy.findServerBeans(new DiscoveryPath(location)).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).get(0);
		Map<String, Object> attr = new HashMap<>();
		attr.put("server.home.dir", bean.getLocation());
		ServerAttributes serverAttr = new ServerAttributes(bean.getServerAdapterTypeId(), id, attr);
		return timeConsumption(new Callable<Status>() {
			public Status call() throws InterruptedException, ExecutionException, TimeoutException {
				return serverProxy.createServer(serverAttr).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).getStatus();
			}
		});
	}

	protected void deleteServer(String id) throws Exception {
		List<ServerHandle> handles = serverProxy.getServerHandles().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		ServerHandle handle = handles.stream().filter(server -> id.equals(server.getId())).findFirst()
				.orElse(null);
		if (handle == null) {
			return;
		}
		Status status = timeConsumption(new Callable<Status>() {
			public Status call() throws InterruptedException, ExecutionException, TimeoutException {
				return serverProxy.deleteServer(handle).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
			}
		});
		assertEquals(Status.OK, status.getSeverity());
	}

	protected void sendPublishRequest(ServerHandle handle, int publishType)
			throws Exception {
		PublishServerRequest pubReq = new PublishServerRequest(handle, publishType);
		Status status = timeConsumption(new Callable<Status>() {
			public Status call() throws InterruptedException, ExecutionException, TimeoutException {
				return serverProxy.publish(pubReq).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
			}
		});
		assertEquals("Expected request status is 'ok' but was " + status, Status.OK, status.getSeverity());
	}

	public DeployableState getDeployableState(DeployableReference reference, List<DeployableState> states) {
		if (states == null || states.isEmpty()) {
			return null;
		}

		return states.stream().filter(state -> reference.equals(state.getReference())).findFirst().orElse(null);
	}

}
