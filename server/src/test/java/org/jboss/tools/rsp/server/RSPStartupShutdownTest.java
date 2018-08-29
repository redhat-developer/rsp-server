/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.SocketLauncher;
import org.jboss.tools.rsp.server.ShutdownExecutor.IShutdownHandler;
import org.jboss.tools.rsp.server.util.ClientLauncher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RSPStartupShutdownTest {
	private ServerManagementServerLauncher rspInstance;
	private ClientLauncher clientInstance;
	private TestShutdownHandler shutdownHandler;
	private CountDownLatch startSignal;
	private CountDownLatch doneSignal;
	
	@Before
	public void setup() {
		shutdownHandler = new TestShutdownHandler();
		startSignal = new CountDownLatch(1);
		doneSignal = new CountDownLatch(1);
	}
	
	@After
	public void teardown() {
		cleanup(rspInstance, clientInstance);
	}
	
	private class TestShutdownHandler implements IShutdownHandler {
		private List<Long> stamps = new ArrayList<Long>();
		@Override
		public void shutdown() {
			if( startSignal != null ) {
				boolean i = Thread.interrupted();
				try {
					startSignal.await();
				} catch(InterruptedException ie) {
				}
				if( i ) {
					Thread.currentThread().interrupt();
				}
			}
			stamps.add(System.currentTimeMillis());
			
			if( doneSignal != null ) {
				doneSignal.countDown();
			}
		}
		
		public Long getLastShutdown() {
			if( stamps.size() > 0 )
				return stamps.get(stamps.size()-1);
			return null;
		}
	}
	
	@Test
	public void testStart() {
		System.out.println("Testing testStart");
		initNew();
		cleanup(rspInstance, clientInstance);
	}
	
	@Test
	public void testShutdown() {
		System.out.println("Testing testShutdown");
		initNew(true);
		startSignal.countDown();
		try {
			doneSignal.await();
		} catch(InterruptedException ie) {}

		assertNotNull(clientInstance.getServerProxy());
		List<RSPClient> clients = rspInstance.getClients();
		assertNotNull(clients);
		assertEquals(1, clients.size());
		
		startSignal = new CountDownLatch(1);
		doneSignal = new CountDownLatch(1);
		
		clientInstance.getServerProxy().shutdown();
		startSignal.countDown();
		try {
			doneSignal.await();
		} catch(InterruptedException ie) {}
		assertNotNull(shutdownHandler.getLastShutdown());
		cleanup(rspInstance, clientInstance);
	}
	

	@Test
	public void testClientClosed() {
		System.out.println("Testing testClientClosed");
		initNew(true);
		startSignal.countDown();
		try {
			doneSignal.await();
		} catch(InterruptedException ie) {}

		assertNotNull(clientInstance.getServerProxy());
		assertNotNull(rspInstance.getClients());
		assertEquals(1, rspInstance.getClients().size());
		
		startSignal = new CountDownLatch(1);
		doneSignal = new CountDownLatch(1);
		
		clientInstance.closeConnection();
		startSignal.countDown();
		try {
			doneSignal.await();
		} catch(InterruptedException ie) {
			
		}
		assertEquals(0, rspInstance.getClients().size());
		cleanup(rspInstance, clientInstance);
	}
	
	protected ServerManagementServerLauncher defaultLauncher() {
		return new ServerManagementServerLauncher();
	}
	
	protected ServerManagementServerLauncher countdownLauncher() {
		return new ServerManagementServerLauncher() {
			protected ServerManagementServerImpl createImpl() {
				return new ServerManagementServerImpl(this) {
					@Override
					protected void removeClient(SocketLauncher<RSPClient> launcher) {
						try {
							startSignal.await();
						} catch(InterruptedException ie) {
						}
						super.removeClient(launcher);
						doneSignal.countDown();
					}
					@Override
					public Runnable addClient(SocketLauncher<RSPClient> launcher) {
						try {
							startSignal.await();
						} catch(InterruptedException ie) {
						}
						Runnable ret = super.addClient(launcher);
						doneSignal.countDown();
						return ret;
					}
				};
			}
		};
	}
	
	private void initNew() {
		initNew(false);
	}
	private void initNew(boolean countdown) {
		ShutdownExecutor.getExecutor().setHandler(shutdownHandler);
		rspInstance = countdown ? countdownLauncher() : defaultLauncher();
		LauncherSingleton.getDefault().setLauncher(rspInstance);
		try {
			rspInstance.launch("27511");
		} catch(Exception e) {
			e.printStackTrace();
			cleanup(rspInstance, null);
			fail();
		}
		
		clientInstance = null;
		try {
			clientInstance = new ClientLauncher("localhost", 27511);
			clientInstance.launch();
			
			assertTrue(clientInstance.isConnectionActive());
		} catch(Exception e) {
			e.printStackTrace();
			cleanup(rspInstance, clientInstance);
			fail();
		}
	}

	private void cleanup(ServerManagementServerLauncher rspInstance, ClientLauncher clientInstance) {
		startSignal = null;
		doneSignal = null;
		if( rspInstance != null ) {
			rspInstance.shutdown();
			rspInstance = null;
		}
		if( clientInstance != null ) {
			clientInstance.closeConnection();
			clientInstance = null;
		}
	}
}
