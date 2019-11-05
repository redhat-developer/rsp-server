package org.jboss.tools.rsp.server.wildfly.test.servertype;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.eclipse.jdt.launching.StandardVMType;
import org.jboss.tools.rsp.server.model.internal.publishing.ServerPublishStateModel;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.publishing.IPublishController;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.ServerTypeStringConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;
import org.junit.Test;

public class WildFlyServerAttributesTest {
	@Test
	public void testChangedHost() throws IOException {
		IServer server = mockServer();
		TestWildFlyServerDelegate del = (TestWildFlyServerDelegate)server.getDelegate();
		
		File f = Files.createTempDirectory(System.currentTimeMillis() + "_wfly").toFile();
		f.mkdirs();
		new File(f, "bin").mkdirs();
		
		doReturn(f.getAbsolutePath()).when(server).getAttribute(eq(IJBossServerAttributes.SERVER_HOME), anyString());
		doReturn(f.getAbsolutePath()).when(server).getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);

		doReturn("0.0.0.0").when(server).getAttribute(eq(IJBossServerAttributes.JBOSS_SERVER_HOST), anyString());
		doReturn("0.0.0.0").when(server).getAttribute(IJBossServerAttributes.JBOSS_SERVER_HOST, (String)null);
		
		CommandLineDetails det = del.getStartLaunchCommand("run", null);
		String[] cmdLine = det.getCmdLine();
		
		assertTrue(isFound(cmdLine, "-b"));
		assertTrue(isFound(cmdLine, "0.0.0.0"));
	}
	
	@Test
	public void testChangedPort() throws IOException {
		IServer server = mockServer();
		TestWildFlyServerDelegate del = (TestWildFlyServerDelegate)server.getDelegate();
		
		File f = Files.createTempDirectory(System.currentTimeMillis() + "_wfly").toFile();
		f.mkdirs();
		new File(f, "bin").mkdirs();
		
		doReturn(f.getAbsolutePath()).when(server).getAttribute(eq(IJBossServerAttributes.SERVER_HOME), anyString());
		doReturn(f.getAbsolutePath()).when(server).getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);

		doReturn(8500).when(server).getAttribute(eq(IJBossServerAttributes.JBOSS_SERVER_PORT), anyInt());
		
		CommandLineDetails det = del.getStartLaunchCommand("run", null);
		String[] cmdLine = det.getCmdLine();
		
		assertTrue(isFound(cmdLine, "-Djboss.http.port=8500"));
	}

	@Test
	public void testChangedConfigFile() throws IOException {
		IServer server = mockServer();
		TestWildFlyServerDelegate del = (TestWildFlyServerDelegate)server.getDelegate();
		
		File f = Files.createTempDirectory(System.currentTimeMillis() + "_wfly").toFile();
		f.mkdirs();
		new File(f, "bin").mkdirs();
		
		doReturn(f.getAbsolutePath()).when(server).getAttribute(eq(IJBossServerAttributes.SERVER_HOME), anyString());
		doReturn(f.getAbsolutePath()).when(server).getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);

		doReturn("standalone-ha.xml").when(server).getAttribute(eq(IJBossServerAttributes.WILDFLY_CONFIG_FILE), anyString());
		
		CommandLineDetails det = del.getStartLaunchCommand("run", null);
		String[] cmdLine = det.getCmdLine();
		
		assertTrue(isFound(cmdLine, "--server-config=standalone-ha.xml"));
	}

	
	private boolean isFound(String[] haystack, String needle) {
		for( int i = 0; i < haystack.length; i++ ) {
			if( haystack[i].equals(needle))
				return true;
		}
		return false;
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
		
		@Override
		public 	IPublishController getOrCreatePublishController() {
			return super.getOrCreatePublishController();
		}

	}

	private static final String JAVA_HOME = "java.home";
	private static final String RUNNING_VM_ID = "running";
	private IServer mockServer() {
		IServer server = mock(IServer.class);
		IServerWorkingCopy wc = mock(IServerWorkingCopy.class);
		doReturn(wc).when(server).createWorkingCopy();
		
		TestWildFlyServerDelegate del = new TestWildFlyServerDelegate(server);
		doReturn(del).when(server).getDelegate();
		IServerType st = mock(IServerType.class);
		doReturn(st).when(server).getServerType();
		doReturn(ServerTypeStringConstants.WF17_ID).when(st).getId();
		doReturn("Test1").when(server).getName();
		doReturn("Test1").when(server).getId();
		
		IServerManagementModel mgmtModel = mock(IServerManagementModel.class);
		doReturn(mgmtModel).when(server).getServerManagementModel();
		IVMInstallRegistry reg = mock(IVMInstallRegistry.class);
		doReturn(reg).when(mgmtModel).getVMInstallModel();
		
		
		String home = System.getProperty(JAVA_HOME);
		File f = new File(home);
		if (f.exists()) {
			IVMInstall vmi = StandardVMType.getDefault().createVMInstall(RUNNING_VM_ID);
			vmi.setInstallLocation(f);
			doReturn(vmi).when(reg).getDefaultVMInstall();
		}
		return server;
	}	

}
