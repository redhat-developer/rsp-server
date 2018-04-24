package org.jboss.tools.ssp.server.wildfly.impl;

import org.jboss.tools.ssp.server.ServerManagementServerImpl;
import org.jboss.tools.ssp.server.ServerManagementServerLauncher;
import org.jboss.tools.ssp.server.wildfly.servertype.impl.JBossServerTypeFactory;

/**
 * This class is for testing purposes until a definitive structure
 * can be decided upon. This just allows me to run the server with 
 * the wildfly enhancements added. 
 */
public class WildflyServerMain extends ServerManagementServerLauncher {
	public static void main(String[] args) throws Exception {
		instance = new WildflyServerMain();
		instance.launch(args[0]);
	}
	
	public void launch(int port) throws Exception {
		// create the chat server
		ServerManagementServerImpl server = new ServerManagementServerImpl();
		addExtensionsToModel(server);
		startListening(port, server);
	}
	
	protected void addExtensionsToModel(ServerManagementServerImpl server) {
		server.getModel().getServerBeanTypeManager().addTypeProvider(new JBossServerBeanTypeProvider());
		server.getModel().getServerModel().addServerFactory(new JBossServerTypeFactory());
		
	}
	

	
}
