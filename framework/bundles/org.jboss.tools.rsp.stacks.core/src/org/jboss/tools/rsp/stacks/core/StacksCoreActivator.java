/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.stacks.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class StacksCoreActivator implements BundleActivator {

	public static final String PLUGIN_ID = "org.jboss.tools.rsp.stacks.core";
	private static BundleContext context;
	private static StacksCoreActivator DEFAULT;
	
	public static StacksCoreActivator getDefault() {
		return DEFAULT;
	}
	
	public static BundleContext getBundleContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		StacksCoreActivator.context = bundleContext;
		DEFAULT = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		StacksCoreActivator.context = null;
	}
	
}
