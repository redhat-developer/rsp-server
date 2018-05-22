package org.jboss.tools.ssp.server.wildfly.impl;

import org.jboss.tools.ssp.server.spi.model.IServerManagementModel;
import org.jboss.tools.ssp.server.wildfly.servertype.impl.JBossServerFactory;

public class ExtensionHandler {
	
	public static void addExtensionsToModel(IServerManagementModel model) {
		model.getServerBeanTypeManager().addTypeProvider(new JBossServerBeanTypeProvider());
		model.getServerModel().addServerType(new JBossServerFactory());
	}
	
}
