package org.jboss.tools.rsp.server.model.publishing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.server.model.internal.publishing.AutoPublishThread;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.junit.Test;

public class AutoPublishThreadTest {
	@Test
	public void testAutoPublishNoActivity() {
		AutoPublishTestThread thread = new AutoPublishTestThread(null, 500, startedAndIncremental());
		thread.start();
		wait(600);
		assertTrue(thread.getPublishCalled());
	}

	@Test
	public void testAutoPublishContinuousActivity() {
		AutoPublishTestThread thread = new AutoPublishTestThread(null, 500, startedAndIncremental());
		thread.start();
		
		// Thread should publish after 500 ms, so lets change something
		// every 100 ms for 10 loops, to verify publish is never called.
		long lastUpdated1 = thread.getLastUpdated();
		long awakenTime1 = thread.getAwakenTime();
		for( int i = 0; i < 10; i++ ) {
			wait(100);
			assertFalse(thread.getPublishCalled());
			thread.updateInactivityCounter();
			long lastUpdated2 = thread.getLastUpdated();
			long awakenTime2 = thread.getAwakenTime();
			assertTrue(lastUpdated2 > lastUpdated1);
			assertTrue(awakenTime2 > awakenTime1);
			lastUpdated1 = lastUpdated2;
			awakenTime1 = awakenTime2;
		}		
		
		wait(700);
		assertTrue(thread.getPublishCalled());
		long lastUpdated2 = thread.getLastUpdated();
		long awakenTime2 = thread.getAwakenTime();
		assertEquals(lastUpdated1, lastUpdated2);
		assertEquals(awakenTime1, awakenTime2);
		
	}

	@Test
	public void testImmediateStoppedState() {
		AutoPublishTestThread thread = new AutoPublishTestThread(null, 500, 
				stoppedAndIncremental());
		thread.start();
		wait(50);
		assertTrue(thread.isDone());
	}

	@Test
	public void testImmediateNoneState() {
		AutoPublishTestThread thread = new AutoPublishTestThread(null, 500, 
				startedAndNone());
		thread.start();
		wait(50);
		assertTrue(thread.isDone());
	}

	@Test
	public void testImmediateStoppedAndNoneState() {
		AutoPublishTestThread thread = new AutoPublishTestThread(null, 500, 
				stoppedAndNone());
		thread.start();
		wait(50);
		assertTrue(thread.isDone());
	}


	@Test
	public void testServerSwitchesToStopped() {
		AutoPublishTestThread thread = new AutoPublishTestThread(null, 500, 
				startedAndIncremental(), stoppedAndIncremental());
		thread.start();
		
		wait(300);
		assertFalse(thread.isDone());
		assertFalse(thread.getPublishCalled());
		thread.switchState2();
		wait(300);
		assertTrue(thread.isDone());
		assertFalse(thread.getPublishCalled());
	}

	@Test
	public void testServerSwitchesToSynchronized() {
		AutoPublishTestThread thread = new AutoPublishTestThread(null, 500, 
				startedAndIncremental(), startedAndNone());
		thread.start();
		
		wait(300);
		assertFalse(thread.isDone());
		assertFalse(thread.getPublishCalled());
		thread.switchState2();
		wait(300);
		assertTrue(thread.isDone());
		assertFalse(thread.getPublishCalled());
	}

	
	
	private void wait(int duration) {
		try {
			Thread.sleep(duration);
		} catch(InterruptedException ie) {
			Thread.interrupted();
		}
	}
	
	private static class AutoPublishTestThread extends AutoPublishThread {
		
		private boolean publishCalled = false;
		private ServerState s1;
		private ServerState s2;
		private ServerState state;
		public AutoPublishTestThread(IServer server, int ms, 
				ServerState s1, ServerState s2) {
			super(server, ms);
			this.s1 = s1;
			this.s2 = s2;
			this.state = this.s1;
		}

		public AutoPublishTestThread(IServer server, int ms, 
				ServerState s1) {
			super(server, ms);
			this.s1 = s1;
			this.state = s1;
		}

		@Override
		protected synchronized void publishImpl() {
			publishCalled = true;
		}
		public synchronized boolean getPublishCalled() {
			return publishCalled;
		}
		@Override
		public synchronized long getLastUpdated() {
			return super.getLastUpdated();
		}
		@Override
		public long getAwakenTime() {
			return super.getAwakenTime();
		}
		@Override
		protected ServerState getServerState() {
			return state;
		}
		@Override
		public synchronized void setDone() {
			super.setDone();
		}
		@Override
		public synchronized boolean isDone() {
			return super.isDone();
		}
		public void switchState2() {
			state = s2;
		}
	}
	
	private ServerState startedAndIncremental() {
		return createServerState(ServerManagementAPIConstants.STATE_STARTED,
				ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL);
	}

	private ServerState stoppedAndIncremental() {
		return createServerState(ServerManagementAPIConstants.STATE_STOPPED,
				ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL);
	}

	private ServerState startedAndNone() {
		return createServerState(ServerManagementAPIConstants.STATE_STARTED,
				ServerManagementAPIConstants.PUBLISH_STATE_NONE);
	}

	private ServerState stoppedAndNone() {
		return createServerState(ServerManagementAPIConstants.STATE_STOPPED,
				ServerManagementAPIConstants.PUBLISH_STATE_NONE);
	}

	private ServerState createServerState(int runState, int pubState) {
		ServerState ss = new ServerState();
		ss.setState(runState);
		ss.setPublishState(pubState);
		return ss;
	}
}
