/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching.java;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class JavaLaunchingActivator implements BundleActivator {

	public static final String BUNDLE_ID = "org.jboss.tools.rsp.launching.java.java";

	private BundleContext bc = null;

	@Override
	public void start(BundleContext context) throws Exception {
		this.bc = context;
		log(LogService.LOG_INFO, NLS.bind("{0} bundle activated.", BUNDLE_ID));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		this.bc = null;
	}

	private void log(int level, String message) {
		LogService log = getService(LogService.class);
		if (log == null) {
			return;
		}
		log.log(level, message);
	}

	private <T> T getService(Class<T> clazz) {
		if (bc == null )
			return null;
		ServiceReference<?> ref = bc.getServiceReference(clazz.getName());
		if( ref != null )
			return (T)bc.getService(ref);
		return null;
	}
	
}
