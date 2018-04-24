package org.jboss.tools.ssp.server.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.ssp.api.beans.SSPAttributes;
import org.jboss.tools.ssp.api.beans.ServerHandle;
import org.jboss.tools.ssp.launching.LaunchingCore;
import org.jboss.tools.ssp.server.model.internal.Server;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.ssp.server.spi.servertype.IServerType;

public class ServerModel {
	private HashMap<String, IServerType> factories;
	private HashMap<String, IServer> servers;
	private HashMap<String, IServerDelegate> serverDelegates;
	
	
	public ServerModel() {
		factories = new HashMap<String, IServerType>();
		servers = new HashMap<String, IServer>();
		serverDelegates = new HashMap<String, IServerDelegate>();
		// TODO load / save of servers?
	}
	
	public void addServerFactory(IServerType fact) {
		if( fact != null && fact.getServerTypeId() != null ) {
			factories.put(fact.getServerTypeId(), fact);
		}
	}
	
	public void removeServerFactory(IServerType fact) {
		if( fact != null && fact.getServerTypeId() != null ) {
			factories.remove(fact.getServerTypeId());
		}
	}
	
	public IStatus createServer(String serverType, String id, Map<String, Object> attributes) {
		IServerType fact = factories.get(serverType);
		if( fact != null ) {
			IStatus valid = validateAttributes(fact, attributes);
			if( !valid.isOK()) {
				return valid;
			}
			IServer server = createServer2(serverType, id, attributes);
			IServerDelegate del = fact.createServerDelegate(server);
			addServer(server, del);
			return Status.OK_STATUS;
		} else {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server", "Server Type " + serverType + " not found");
		}
	}
	
	private IStatus validateAttributes(IServerType type, Map<String, Object> attrs) {
		SSPAttributes a = type.getRequiredAttributes();
		Set<String> required = a.listAttributes();
		for( String str : required ) {
			if( attrs.get(str) == null ) {
				return Status.CANCEL_STATUS; // TODO fix this
			}
			Object v = attrs.get(str);
			Class actual = v.getClass();
			Class expected = a.getAttributeType(str);
			if( !actual.equals(expected)) {
				return Status.CANCEL_STATUS;
			}
		}
		return Status.OK_STATUS;
	}
	
	private Server createServer2(String serverType, String id, Map<String, Object> attributes) {
		File data = LaunchingCore.getDataLocation();
		File servers = new File(data, "servers");
		if( !servers.exists()) {
			servers.mkdirs();
		}
		// TODO check for duplicates
		File thisServer = new File(servers, id);
		Server s = new Server(thisServer, serverType);
		return s;
	}
	

	private void addServer(IServer server, IServerDelegate del) {
		servers.put(server.getId(), server);
		serverDelegates.put(server.getId(), del);
		// TODO fire events?
	}
	public void removeServer(String serverId) {
		servers.remove(serverId);
		serverDelegates.remove(serverId);
		// TODO fire events?
	}
	
	public ServerHandle[] getServerHandles() {
		Set<String> s = servers.keySet();
		ArrayList<ServerHandle> handles = new ArrayList<>();
		for( String s1 : s ) {
			String id = s1;
			String type = servers.get(id).getTypeId();
			handles.add(new ServerHandle(id,  type));
		}
		return (ServerHandle[]) handles.toArray(new ServerHandle[handles.size()]);
	}
	
	public String[] getServerTypes() {
		Set<String> types = factories.keySet();
		return (String[]) types.toArray(new String[types.size()]);
	}
	
	public SSPAttributes getRequiredAttributes(String type) {
		IServerType t = factories.get(type);
		return t == null ? null : t.getRequiredAttributes();
	}
	
	public SSPAttributes getOptionalAttributes(String type) {
		IServerType t = factories.get(type);
		return t == null ? null : t.getOptionalAttributes();
	}
}
