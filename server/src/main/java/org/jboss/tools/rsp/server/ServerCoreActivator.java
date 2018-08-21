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
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
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
		sendLogsToSysout();
		setShutdownHandler();
		startServer();
		log(LogService.LOG_INFO, NLS.bind("{0} bundle activated.", BUNDLE_ID));
	}
	
	public ServerManagementServerLauncher getLauncher() {
		return LauncherSingleton.getDefault().getLauncher();
	}
	
	private String getPort() {
		// TODO from sysprops?
		int port = 27511;
		return Integer.toString(port);
	}
	
	private void startServer() {
		// TODO from sysprops?
		String port = getPort();
		ServerManagementServerLauncher launcher = new ServerManagementServerLauncher();
		LauncherSingleton.getDefault().setLauncher(launcher);
		
		new Thread("Launch RSP Server") {
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
	
	private void sendLogsToSysout() {
		LogReaderService logReader = getService(LogReaderService.class);
		if (logReader == null) {
			return;
		}
		logReader.addLogListener(entry -> {
				if( entry.getLevel() <= LogService.LOG_WARNING) { 
					String message = new StringBuilder()
						.append("[").append(entry.getLevel()).append("] ")
						.append(entry.getTime()).append(": ").append(entry.getMessage())
						.toString();
					System.out.println(message);
				}
		});
	}

	private void log(int level, String message) {
		LogService log = getService(LogService.class);
		if (log == null) {
			return;
		}
		log.log(level, message);
	}

	private <T> T getService(Class<T> clazz) {
		if (context == null )
			return null;
		ServiceReference<?> ref = context.getServiceReference(clazz.getName());
		if( ref != null )
			return (T)context.getService(ref);
		return null;
	}
	
}
