package org.jboss.tools.ssp.server;

import org.jboss.tools.ssp.server.ShutdownExecutor.IShutdownHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class ServerCoreActivator implements BundleActivator {
	private static BundleContext context;
	private static ServerCoreActivator activator;
	
	public static BundleContext getDefaultContext() {
		return context;
	}
	
	public static ServerCoreActivator getDefault() {
		return activator;
	}
	
	private ServerManagementServerLauncher launcher;
	
	@Override
	public void start(final BundleContext context2) throws Exception {
		activator = this;
		context = context2;
		setShutdownHandler();
		startServer();
		System.out.println("Server bundle started");
	}
	
	public ServerManagementServerLauncher getLauncher() {
		return launcher;
	}
	
	
	
	private String getPort() {
		// TODO from sysprops?
		int port = 27511;
		return Integer.toString(port);
	}
	
	private void startServer() {
		// TODO from sysprops?
		String port = getPort();
		launcher = new ServerManagementServerLauncher();
		new Thread("Launch SSP Server") {
			public void run() {
				try {
					launcher.launch(port);
				} catch( Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private void setShutdownHandler() {
		ShutdownExecutor.getExecutor().setHandler(new IShutdownHandler() {
			@Override
			public void shutdown() {
				try {
					context.getBundle(0).stop();
				} catch(BundleException be) {
					be.printStackTrace();
				}
			}});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
