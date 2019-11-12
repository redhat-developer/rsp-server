/*************************************************************************************
 * Copyright (c) 2010-2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesModel;
import org.jboss.tools.rsp.runtime.core.model.internal.DownloadRuntimesModel;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeCoreActivator implements BundleActivator {
	private static final Logger LOG = LoggerFactory.getLogger(RuntimeCoreActivator.class);
	public static final String PLUGIN_ID = "org.jboss.tools.rsp.runtime.core"; //$NON-NLS-1$
	
	public RuntimeCoreActivator() {
	}

	public void start(BundleContext context) throws Exception {
		LOG.debug(NLS.bind("{0} bundle started.", PLUGIN_ID));
	}

	public void stop(BundleContext context) throws Exception {
		LOG.debug(NLS.bind("{0} bundle stopped.", PLUGIN_ID));
	}

	public static IDownloadRuntimesModel createDownloadRuntimesModel() {
		return new DownloadRuntimesModel();
	}
}
