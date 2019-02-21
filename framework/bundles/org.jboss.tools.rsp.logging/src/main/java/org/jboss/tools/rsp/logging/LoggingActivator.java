/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.logging;


import org.jboss.tools.rsp.logging.internal.OSGILogReaderInitializer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingActivator implements BundleActivator, LoggingConstants {


	private static final Logger LOG = LoggerFactory.getLogger(LoggingActivator.class);

	private OSGILogReaderInitializer initializer;

	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("Activating bundle");
		LogLevelInitializer.initLogLevel();
		initializer = new OSGILogReaderInitializer();
		initializer.init(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		initializer.dispose(context);
		LOG.debug("Bundle stopped");
	}

}
