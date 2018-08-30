/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.ShutdownExecutor.IShutdownHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.log.LogService;

public class ServerCoreActivator implements BundleActivator {
	public static final String BUNDLE_ID = "org.jboss.tools.rsp.server";

	private static BundleContext context;
	private static ServerCoreActivator activator;
	
	public static BundleContext getDefaultContext() {
		return context;
	}
	
	public static ServerCoreActivator getDefault() {
		return activator;
	}
	
	@Override
	public void start(final BundleContext context2) throws Exception {
		activator = this;
		context = context2;
		RSPLogger.useService();
		setShutdownHandler();
		startServer();
		RSPLogger.log(LogService.LOG_INFO, NLS.bind("{0} bundle activated.", BUNDLE_ID));
	}
	
	public ServerManagementServerLauncher getLauncher() {
		return LauncherSingleton.getDefault().getLauncher();
	}
	
	private int getPort() {
		return RSPFlags.getServerPort();
	}
	
	private void startServer() {
		int port = getPort();
		ServerManagementServerLauncher launcher = new ServerManagementServerLauncher();
		LauncherSingleton.getDefault().setLauncher(launcher);
		
		new Thread("Launch RSP Server") {
			public void run() {
				try {
					launcher.launch(port);
				} catch( Exception e) {
					RSPLogger.log(RSPLogger.LOG_ERROR, "Unable to launch RSP server", e);
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
