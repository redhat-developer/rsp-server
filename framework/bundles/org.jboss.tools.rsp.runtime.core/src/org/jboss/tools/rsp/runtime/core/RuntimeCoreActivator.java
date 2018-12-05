/*************************************************************************************
 * Copyright (c) 2010-2011 Red Hat, Inc. and others.
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

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author snjeza
 */
public class RuntimeCoreActivator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.rsp.runtime.core"; //$NON-NLS-1$

	// The shared instance
	private static RuntimeCoreActivator plugin;

	private BundleContext context;
	
	/**
	 * The constructor
	 */
	public RuntimeCoreActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RuntimeCoreActivator getDefault() {
		return plugin;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	public static IDownloadRuntimesModel createDownloadRuntimesModel() {
		return new DownloadRuntimesModel();
	}
}
