/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.foundation.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoundationCoreActivator implements BundleActivator {
	public static final String PLUGIN_ID = "org.jboss.tools.rsp.foundation.core";

	private static final Logger LOG = LoggerFactory.getLogger(FoundationCoreActivator.class);


	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("Activating bundle " + PLUGIN_ID);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.debug("Stopping bundle " + PLUGIN_ID);
	}

}
