package org.jboss.tools.ssp.launching;

import org.jboss.tools.ssp.api.dao.Status;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.google.gson.Gson;

public class LaunchingActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		Status stat = new Status(Status.OK, "blah", "launching");
		Class gs = Gson.class;
		System.out.println("Launching Bundle Started");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
