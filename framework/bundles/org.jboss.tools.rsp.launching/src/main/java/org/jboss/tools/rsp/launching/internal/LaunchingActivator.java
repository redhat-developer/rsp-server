/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching.internal;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaunchingActivator implements BundleActivator {

	public static final String BUNDLE_ID = "org.jboss.tools.rsp.launching";
	private static final Logger LOG = LoggerFactory.getLogger(LaunchingActivator.class);

	private BundleContext bc = null;

	@Override
	public void start(BundleContext context) throws Exception {
		this.bc = context;
		LOG.info(NLS.bind("{0} bundle activated.", BUNDLE_ID));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		this.bc = null;
	}
	
	public Bundle getBundle() {
		return this.bc == null ? null : this.bc.getBundle();
	}
}
