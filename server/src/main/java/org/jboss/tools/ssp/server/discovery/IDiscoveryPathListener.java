package org.jboss.tools.ssp.server.discovery;

import org.jboss.tools.ssp.api.beans.DiscoveryPath;

public interface IDiscoveryPathListener {
	public void discoveryPathAdded(DiscoveryPath path);
	
	public void discoveryPathRemoved(DiscoveryPath path);
	
}
