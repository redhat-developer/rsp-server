/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerCoreActivator implements BundleActivator {

	public static final String BUNDLE_ID = "org.jboss.tools.rsp.server";
	private static final Logger LOG = LoggerFactory.getLogger(ServerCoreActivator.class);

	@Override
	public void start(final BundleContext context) throws Exception {
		setShutdownHandler(context);
		startServer();
		LOG.debug(NLS.bind("{0} bundle started.", BUNDLE_ID));
	}

	public ServerManagementServerLauncher getLauncher() {
		return LauncherSingleton.getDefault().getLauncher();
	}

	private int getPort() {
		return RSPFlags.getServerPort();
	}

	private void startServer() {
		int port = getPort();
		ServerManagementServerLauncher launcher = new ServerManagementServerLauncher(""+port);
		LauncherSingleton.getDefault().setLauncher(launcher);
		
		new Thread(() -> {
				try {
					launcher.launch(port);
				} catch (Exception e) {
					LOG.error("Unable to launch RSP server", e);
				}
			}, 
			"Launch RSP Server")
		.start();
	}

	private void setShutdownHandler(final BundleContext context) {
		ShutdownExecutor.getExecutor().setHandler(() -> {
			try {
				context.getBundle(0).stop();
			} catch (BundleException e) {
				LOG.error(NLS.bind("Stopping bundle {0} failed.", BUNDLE_ID), e);
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.debug(NLS.bind("{0} bundle stopped.", BUNDLE_ID));
	}
}