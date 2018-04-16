package org.jboss.tools.ssp.server.wildfly.impl;

import org.jboss.tools.ssp.server.ServerManagementServerImpl;
import org.jboss.tools.ssp.server.ServerManagementServerLauncher;

/**
 * This class is for testing purposes until a definitive structure
 * can be decided upon. This just allows me to run the server with 
 * the wildfly enhancements added. 
 */
public class WildflyServerMain extends ServerManagementServerLauncher {
	public static void main(String[] args) throws Exception {
		WildflyServerMain xi = new WildflyServerMain();
		xi.launch(args[0]);
	}
	
	public void launch(int port) throws Exception {
		// create the chat server
		ServerManagementServerImpl server = new ServerManagementServerImpl();
		addExtensionsToModel(server);
		startListening(port, server);
	}
	
	protected void addExtensionsToModel(ServerManagementServerImpl server) {
		server.getModel().getServerBeanTypeManager().addTypeProvider(new JBossServerBeanTypeProvider());
	}

	
}
