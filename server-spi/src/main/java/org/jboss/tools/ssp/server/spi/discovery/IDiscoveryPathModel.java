package org.jboss.tools.ssp.server.spi.discovery;

import java.util.List;

import org.jboss.tools.ssp.api.dao.DiscoveryPath;

public interface IDiscoveryPathModel {

	public void addListener(IDiscoveryPathListener l);

	public void removeListener(IDiscoveryPathListener l);

	public List<DiscoveryPath> getPaths();
	
	public void addPath(DiscoveryPath path);
	
	public void removePath(DiscoveryPath path);
}
