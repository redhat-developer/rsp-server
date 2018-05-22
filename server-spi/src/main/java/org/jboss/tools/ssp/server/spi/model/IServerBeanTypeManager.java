package org.jboss.tools.ssp.server.spi.model;

import org.jboss.tools.ssp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.ssp.server.spi.discovery.ServerBeanType;

public interface IServerBeanTypeManager {
	public void addTypeProvider(IServerBeanTypeProvider provider);
	public ServerBeanType[] getAllRegisteredTypes();
}
