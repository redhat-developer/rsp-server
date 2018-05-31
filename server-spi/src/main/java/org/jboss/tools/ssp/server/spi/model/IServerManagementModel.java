package org.jboss.tools.ssp.server.spi.model;

import org.jboss.tools.ssp.server.spi.discovery.IDiscoveryPathModel;
import org.jboss.tools.ssp.server.spi.discovery.IServerBeanTypeManager;

public interface IServerManagementModel {
	public IServerBeanTypeManager getServerBeanTypeManager();
	public IServerModel getServerModel();
	public IDiscoveryPathModel getDiscoveryPathModel();
}
