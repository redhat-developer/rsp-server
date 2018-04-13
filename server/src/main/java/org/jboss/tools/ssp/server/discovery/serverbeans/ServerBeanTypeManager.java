package org.jboss.tools.ssp.server.discovery.serverbeans;

import java.util.ArrayList;
import java.util.Arrays;

import org.jboss.tools.ssp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.ssp.server.spi.discovery.ServerBeanType;

public class ServerBeanTypeManager {
	private ArrayList<IServerBeanTypeProvider> typeProviders;
	
	public ServerBeanTypeManager() {
		typeProviders = new ArrayList<IServerBeanTypeProvider>();
	}
	
	public void addTypeProvider(IServerBeanTypeProvider provider) {
		typeProviders.add(provider);
	}
	
	public ServerBeanType[] getAllRegisteredTypes() {
		ArrayList<ServerBeanType> ret = new ArrayList<ServerBeanType>();
		for( IServerBeanTypeProvider prov : typeProviders) {
			ret.addAll(Arrays.asList(prov.getServerBeanTypes()));
		}
		return (ServerBeanType[]) ret.toArray(new ServerBeanType[ret.size()]);
	}
}
