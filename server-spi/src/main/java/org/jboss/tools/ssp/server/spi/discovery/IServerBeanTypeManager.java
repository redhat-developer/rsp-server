package org.jboss.tools.ssp.server.spi.discovery;

public interface IServerBeanTypeManager {
	public void addTypeProvider(IServerBeanTypeProvider provider);
	public ServerBeanType[] getAllRegisteredTypes();
}
