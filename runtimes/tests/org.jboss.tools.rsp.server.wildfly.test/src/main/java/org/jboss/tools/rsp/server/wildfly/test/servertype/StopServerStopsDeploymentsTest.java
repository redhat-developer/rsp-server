package org.jboss.tools.rsp.server.wildfly.test.servertype;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.server.model.internal.publishing.ServerPublishStateModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;
import org.junit.Test;

public class StopServerStopsDeploymentsTest {
	@Test
	public void testStopServerStopsDeployments() throws IOException {
		IServer server = mockServer();
		TestWildFlyServerDelegate del = (TestWildFlyServerDelegate) server.getDelegate();
		String f = Files.createTempFile(System.currentTimeMillis() + "", ".war").toString();
		String f1 = Files.createTempFile(System.currentTimeMillis() + "", ".war").toString();
		String f2 = Files.createTempFile(System.currentTimeMillis() + "", ".war").toString();
		DeployableReference r1 = new DeployableReference(f, f);
		DeployableReference r2 = new DeployableReference(f1, f1);
		DeployableReference r3 = new DeployableReference(f2, f2);
		
		del.getServerPublishModel().addDeployable(r1);
		del.getServerPublishModel().addDeployable(r2);
		del.getServerPublishModel().addDeployable(r3);
		
		del.setServerState(ServerManagementAPIConstants.STATE_STARTED);
		del.getServerPublishModel().setDeployableState(r1, ServerManagementAPIConstants.STATE_STARTED);
		del.getServerPublishModel().setDeployableState(r2, ServerManagementAPIConstants.STATE_STARTED);
		del.getServerPublishModel().setDeployableState(r3, ServerManagementAPIConstants.STATE_STARTED);
		
		assertEquals(del.getServerRunState(), ServerManagementAPIConstants.STATE_STARTED);
		assertEquals(del.getServerState().getState(), ServerManagementAPIConstants.STATE_STARTED);

		assertEquals(del.getServerPublishModel().getDeployableState(r1).getState(), ServerManagementAPIConstants.STATE_STARTED);
		assertEquals(del.getServerPublishModel().getDeployableState(r2).getState(), ServerManagementAPIConstants.STATE_STARTED);
		assertEquals(del.getServerPublishModel().getDeployableState(r3).getState(), ServerManagementAPIConstants.STATE_STARTED);
		
		
		del.setServerState(ServerManagementAPIConstants.STATE_STOPPED);
		assertEquals(del.getServerRunState(), ServerManagementAPIConstants.STATE_STOPPED);
		assertEquals(del.getServerState().getState(), ServerManagementAPIConstants.STATE_STOPPED);

		assertEquals(del.getServerPublishModel().getDeployableState(r1).getState(), ServerManagementAPIConstants.STATE_STOPPED);
		assertEquals(del.getServerPublishModel().getDeployableState(r2).getState(), ServerManagementAPIConstants.STATE_STOPPED);
		assertEquals(del.getServerPublishModel().getDeployableState(r3).getState(), ServerManagementAPIConstants.STATE_STOPPED);
		
		
		
		// Now test the method with fire events in it
		del.setServerState(ServerManagementAPIConstants.STATE_STARTED);
		del.getServerPublishModel().setDeployableState(r1, ServerManagementAPIConstants.STATE_STARTED);
		del.getServerPublishModel().setDeployableState(r2, ServerManagementAPIConstants.STATE_STARTED);
		del.getServerPublishModel().setDeployableState(r3, ServerManagementAPIConstants.STATE_STARTED);

		
		assertEquals(del.getServerRunState(), ServerManagementAPIConstants.STATE_STARTED);
		assertEquals(del.getServerState().getState(), ServerManagementAPIConstants.STATE_STARTED);

		assertEquals(del.getServerPublishModel().getDeployableState(r1).getState(), ServerManagementAPIConstants.STATE_STARTED);
		assertEquals(del.getServerPublishModel().getDeployableState(r2).getState(), ServerManagementAPIConstants.STATE_STARTED);
		assertEquals(del.getServerPublishModel().getDeployableState(r3).getState(), ServerManagementAPIConstants.STATE_STARTED);
		
		
		del.setServerState(ServerManagementAPIConstants.STATE_STOPPED, true);
		assertEquals(del.getServerRunState(), ServerManagementAPIConstants.STATE_STOPPED);
		assertEquals(del.getServerState().getState(), ServerManagementAPIConstants.STATE_STOPPED);

		assertEquals(del.getServerPublishModel().getDeployableState(r1).getState(), ServerManagementAPIConstants.STATE_STOPPED);
		assertEquals(del.getServerPublishModel().getDeployableState(r2).getState(), ServerManagementAPIConstants.STATE_STOPPED);
		assertEquals(del.getServerPublishModel().getDeployableState(r3).getState(), ServerManagementAPIConstants.STATE_STOPPED);
	}
	
	private static class TestWildFlyServerDelegate extends WildFlyServerDelegate {
		public TestWildFlyServerDelegate(IServer server) {
			super(server);
		}

		@Override
		public void setServerState(int state) {
			super.setServerState(state);
		}

		@Override
		public void setServerState(int state, boolean fire) {
			super.setServerState(state, fire);
		}

		@Override
		protected IServerPublishModel createServerPublishModel() {
			return new ServerPublishStateModel(this, null);
		}
		@Override
		public ServerHandle getServerHandle() {
			return new ServerHandle("test", new ServerType("test5", "test5.name", "test5.desc"));
		}
		@Override
		protected void fireStateChanged(ServerState state) {
		}
	}
	
	private IServer mockServer() {
		IServer server = mock(IServer.class);
		TestWildFlyServerDelegate del = new TestWildFlyServerDelegate(server);
		doReturn(del).when(server).getDelegate();
		return server;
	}	

}
