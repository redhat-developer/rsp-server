package org.jboss.tools.rsp.server.spi.discovery;

public interface IServerBeanTypeManager {
	public void addTypeProvider(IServerBeanTypeProvider provider);
	public ServerBeanType[] getAllRegisteredTypes();
}
