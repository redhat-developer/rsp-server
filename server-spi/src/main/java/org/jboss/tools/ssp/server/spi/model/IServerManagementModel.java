package org.jboss.tools.ssp.server.spi.model;

import org.jboss.tools.ssp.server.spi.discovery.IDiscoveryPathModel;

public interface IServerManagementModel {
	public IServerBeanTypeManager getServerBeanTypeManager();
	public IServerModel getServerModel();
	public IDiscoveryPathModel getDiscoveryPathModel();
}
