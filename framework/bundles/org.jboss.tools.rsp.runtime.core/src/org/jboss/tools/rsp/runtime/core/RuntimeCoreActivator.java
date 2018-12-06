/*************************************************************************************
 * Copyright (c) 2010-2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core;

import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesModel;
import org.jboss.tools.rsp.runtime.core.model.internal.DownloadRuntimesModel;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class RuntimeCoreActivator implements BundleActivator {

	public static final String PLUGIN_ID = "org.jboss.tools.rsp.runtime.core"; //$NON-NLS-1$
	
	public RuntimeCoreActivator() {
	}

	public void start(BundleContext context) throws Exception {
	}

	public void stop(BundleContext context) throws Exception {
	}

	public static IDownloadRuntimesModel createDownloadRuntimesModel() {
		return new DownloadRuntimesModel();
	}
}
