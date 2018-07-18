package org.jboss.tools.ssp.server.spi.discovery;

import java.util.List;

import org.jboss.tools.ssp.api.dao.DiscoveryPath;

public interface IDiscoveryPathModel {

	public void addListener(IDiscoveryPathListener l);

	public void removeListener(IDiscoveryPathListener l);

	public List<DiscoveryPath> getPaths();
	
	/**
	 * Return whether the path was added.
	 * @param path
	 * @return
	 */
	public boolean addPath(DiscoveryPath path);
	
	/**
	 * Return whether the path was removed
	 * @param path
	 * @return
	 */
	public boolean removePath(DiscoveryPath path);
}
