package org.jboss.tools.ssp.server.wildfly.impl;

import org.jboss.tools.ssp.server.ServerCoreActivator;
import org.jboss.tools.ssp.server.ServerManagementServerImpl;
import org.jboss.tools.ssp.server.wildfly.servertype.impl.JBossServerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	public static final String BUNDLE_ID = "org.jboss.tools.ssp.server.wildfly";
	
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Wildfly Server bundle started");
		ExtensionHandler.addExtensionsToModel(ServerCoreActivator.getDefault().getLauncher().getModel());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
	
	protected void addExtensionsToModel(ServerManagementServerImpl server) {
		server.getModel().getServerBeanTypeManager().addTypeProvider(new JBossServerBeanTypeProvider());
		server.getModel().getServerModel().addServerType(new JBossServerFactory());
	}
	
}
