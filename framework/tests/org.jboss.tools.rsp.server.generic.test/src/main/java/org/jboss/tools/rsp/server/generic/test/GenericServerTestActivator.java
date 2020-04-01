/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.server.generic.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class GenericServerTestActivator implements BundleActivator {
	private static BundleContext myContext;
	public static BundleContext getContext() {
		return myContext;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		myContext = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
