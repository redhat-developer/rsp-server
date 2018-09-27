/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.logging;


import org.jboss.tools.rsp.logging.internal.OSGILogReaderInitializer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingActivator implements BundleActivator {
	public static final String SYSPROP_LOG_LEVEL_FLAG = "rsp.log.level";

	public static final int LOG_ERROR = 1;
	public static final int LOG_WARNING = 2;
	public static final int LOG_INFO = 3;
	public static final int LOG_DEBUG = 4;

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
