package org.jboss.tools.ssp.server.model;

import java.util.List;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.jboss.tools.ssp.api.ServerManagementClient;
import org.jboss.tools.ssp.api.beans.DiscoveryPath;
import org.jboss.tools.ssp.api.beans.ServerHandle;
import org.jboss.tools.ssp.api.beans.VMDescription;
import org.jboss.tools.ssp.server.ServerManagementServerImpl;
import org.jboss.tools.ssp.server.discovery.IDiscoveryPathListener;

public class RemoteEventManager implements IDiscoveryPathListener, IVMInstallChangedListener, IServerModelListener {
	private ServerManagementServerImpl server;
	public RemoteEventManager(ServerManagementServerImpl serverManagementServerImpl) {
		this.server = serverManagementServerImpl; 
		serverManagementServerImpl.getModel().getDiscoveryPathModel().addListener(this);
		serverManagementServerImpl.getModel().getVMInstallModel().addListener(this);
		serverManagementServerImpl.getModel().getServerModel().addServerModelListener(this);
	}
	@Override
	public void discoveryPathAdded(DiscoveryPath path) {
		List<ServerManagementClient> l = server.getClients();
		for( ServerManagementClient c : l) {
			c.discoveryPathAdded(path);
		}
	}
	@Override
	public void discoveryPathRemoved(DiscoveryPath path) {
		List<ServerManagementClient> l = server.getClients();
		for( ServerManagementClient c : l) {
			c.discoveryPathRemoved(path);
		}
	}

	private VMDescription getDescription(IVMInstall vmi) {
		String vers = vmi instanceof IVMInstall2 ? ((IVMInstall2)vmi).getJavaVersion() : null;
		return new VMDescription(vmi.getId(), vmi.getInstallLocation().getAbsolutePath(), vers);
	}

	
	@Override
	public void vmAdded(IVMInstall vm) {
		List<ServerManagementClient> l = server.getClients();
		for( ServerManagementClient c : l) {
			c.vmAdded(getDescription(vm));
		}
	}
	@Override
	public void vmRemoved(IVMInstall vm) {
		List<ServerManagementClient> l = server.getClients();
		for( ServerManagementClient c : l) {
			c.vmRemoved(getDescription(vm));
		}
	}

	public void serverAdded(ServerHandle server2) {
		List<ServerManagementClient> l = server.getClients();
		for( ServerManagementClient c : l) {
			c.serverAdded(server2);
		}
	}
	
	public void serverRemoved(ServerHandle server2) {
		List<ServerManagementClient> l = server.getClients();
		for( ServerManagementClient c : l) {
			c.serverRemoved(server2);
		}
	}
	
	public void serverAttributesChanged(ServerHandle server) {
		// TODO 
	}
	
	public void serverStateChanged(ServerHandle server, int state) {
		List<ServerManagementClient> l = this.server.getClients();
		for( ServerManagementClient c : l) {
			c.serverStateChanged(server, state);
		}
	}
	
	public void serverProcessCreated(ServerHandle server, String processId) {
		List<ServerManagementClient> l = this.server.getClients();
		for( ServerManagementClient c : l) {
			c.serverProcessCreated(server, processId);
		}
	}
	
	public void serverProcessTerminated(ServerHandle server, String processId) {
		List<ServerManagementClient> l = this.server.getClients();
		for( ServerManagementClient c : l) {
			c.serverProcessTerminated(server, processId);
		}
	}
	
	public void serverProcessOutputAppended(ServerHandle server, String processId, int streamType, String text) {
		List<ServerManagementClient> l = this.server.getClients();
		for( ServerManagementClient c : l) {
			c.serverProcessOutputAppended(server, processId, streamType, text);
		}
	}
	
	
	
	// To be ignored
	@Override
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void vmChanged(PropertyChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
	
}
