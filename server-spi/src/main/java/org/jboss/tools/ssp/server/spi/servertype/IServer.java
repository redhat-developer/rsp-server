package org.jboss.tools.ssp.server.spi.servertype;

public interface IServer extends IServerAttributes {
	
	public String getId();
	
	public String getTypeId();
	
	public IServerDelegate getDelegate();
}
