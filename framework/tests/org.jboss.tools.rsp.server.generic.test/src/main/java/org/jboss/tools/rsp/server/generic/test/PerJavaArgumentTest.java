package org.jboss.tools.rsp.server.generic.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.generic.servertype.launch.GenericJavaLauncher;
import org.junit.Test;

public class PerJavaArgumentTest {
	
	@Test
	public void testGetJavaVersionProperty() {
		String[] options = {"test.version.1.5", "test.version.1.6", "test.version.1.7", 
				"test.version.9", "test.version.10"};
		assertEquals("test.version.1.5", GenericJavaLauncher.getJavaVersionProperty("1.5.1", Arrays.asList(options), "test"));
		assertEquals("test.version.1.5", GenericJavaLauncher.getJavaVersionProperty("1.5.2", Arrays.asList(options), "test"));
		assertEquals("test.version.1.5", GenericJavaLauncher.getJavaVersionProperty("1.5.10", Arrays.asList(options), "test"));
		assertEquals("test.version.1.6", GenericJavaLauncher.getJavaVersionProperty("1.6.0.123", Arrays.asList(options), "test"));
		assertEquals("test.version.1.6", GenericJavaLauncher.getJavaVersionProperty("1.6.9999", Arrays.asList(options), "test"));
		assertEquals("test.version.1.7", GenericJavaLauncher.getJavaVersionProperty("1.7.0.123.1.1.1.1.1", Arrays.asList(options), "test"));
		assertEquals("test.version.1.7", GenericJavaLauncher.getJavaVersionProperty("1.7.9999", Arrays.asList(options), "test"));
		assertEquals("test.version.9", GenericJavaLauncher.getJavaVersionProperty("9.0.123.1.1.1.1.1", Arrays.asList(options), "test"));
		assertEquals("test.version.9", GenericJavaLauncher.getJavaVersionProperty("9.9999", Arrays.asList(options), "test"));
		assertEquals("test.version.10", GenericJavaLauncher.getJavaVersionProperty("10.0.123.1.1.1.1.1", Arrays.asList(options), "test"));
		assertEquals("test.version.10", GenericJavaLauncher.getJavaVersionProperty("10.9999", Arrays.asList(options), "test"));
		assertEquals("test.version.10", GenericJavaLauncher.getJavaVersionProperty("15.9999", Arrays.asList(options), "test"));
		
		
		
		String str = getLaunchJson();
		JSONMemento memento = JSONMemento.createReadRoot(new ByteArrayInputStream(str.getBytes()));
		JSONMemento m2 = memento.getChild("startup");
		GenericJavaLauncherTestImpl g = new GenericJavaLauncherTestImpl(null, m2, "9.0.1");
		assertEquals("xyz", g.getDefaultVMArguments());
		assertEquals("789", g.getDefaultProgramArguments());

		GenericJavaLauncherTestImpl g2 = new GenericJavaLauncherTestImpl(null, m2, "6.0.1");
		assertEquals("abc", g2.getDefaultVMArguments());
		assertEquals("123", g2.getDefaultProgramArguments());

	}
	
	public static class GenericJavaLauncherTestImpl extends GenericJavaLauncher {

		private String javaVersion;
		public GenericJavaLauncherTestImpl(GenericServerBehavior serverDelegate, JSONMemento memento, 
				String javaVersion) {
			super(serverDelegate, memento);
			this.javaVersion = javaVersion;
		}
		@Override
		protected String getJavaVersion() {
			return javaVersion;
		}
		public String getDefaultVMArguments() {
			return super.getDefaultVMArguments();
		}
		public String getDefaultProgramArguments() {
			return super.getDefaultProgramArguments();
		}
	};
	
	private String getLaunchJson() {
		return    "{\n"
				+ "	\"startup\": {\n"
				+ "						\"launchType\": \"java-launch\",\n"
				+ "						\"launchProperties\": {\n"
				+ "							\"workingDirectory\": \"${jetty.base.dir}\",\n"
				+ "							\"mainType\": \"org.eclipse.jetty.start.Main\",\n"
				+ "							\"programArgs.version.1\": \"123\",\n"
				+ "							\"programArgs.version.9\": \"789\",\n"
				+ "							\"vmArgs.version.1\": \"abc\",\n"
				+ "							\"vmArgs.version.9\": \"xyz\",\n"
				+ "							\"classpath\": \"start.jar\"\n"
				+ "						},\n"
				+ "						\"poller\": \"webPoller\",\n"
				+ "						\"pollerProperties\": {\n"
				+ "							\"url\": \"http://${jetty.host}:${jetty.port}\"\n"
				+ "						},\n"
				+ "						\"onProcessTerminated\": \"setServerStateStopped\"\n"
				+ "					}\n"
				+ "}";
	}
}
