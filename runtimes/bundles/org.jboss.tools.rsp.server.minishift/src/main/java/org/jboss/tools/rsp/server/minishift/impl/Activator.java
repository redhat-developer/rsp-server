/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.impl;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	public static final String BUNDLE_ID = "org.jboss.tools.rsp.server.minishift";
	
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Minishift Server bundle started");
		ExtensionHandler.addExtensionsToModel(LauncherSingleton.getDefault().getLauncher().getModel());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
}
