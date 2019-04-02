package org.jboss.tools.rsp.server;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ServerTestActivator implements BundleActivator {
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
