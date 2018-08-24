package org.jboss.tools.rsp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.server.ShutdownExecutor.IShutdownHandler;
import org.jboss.tools.rsp.server.util.ClientLauncher;
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
	
	private class TestShutdownHandler implements IShutdownHandler {
		private List<Long> stamps = new ArrayList<Long>();
		@Override
		public void shutdown() {
			if( startSignal != null ) {
				Thread.interrupted();
				try {
					startSignal.await();
				} catch(InterruptedException ie) {
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
		initNew();
		cleanup(rspInstance, clientInstance);
	}
	
	@Test
	public void testShutdown() {
		initNew();
		assertNotNull(clientInstance.getServerProxy());
		List<RSPClient> clients = rspInstance.getClients();
		assertNotNull(clients);
		assertEquals(1, clients.size());
		clientInstance.getServerProxy().shutdown();
		startSignal.countDown();
		try {
			doneSignal.await();
		} catch(InterruptedException ie) {
			
		}
		assertNotNull(shutdownHandler.getLastShutdown());
		cleanup(rspInstance, clientInstance);
	}
	
	
	private void initNew() {
		ShutdownExecutor.getExecutor().setHandler(shutdownHandler);
		
		
		rspInstance = new ServerManagementServerLauncher();
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
