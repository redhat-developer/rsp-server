/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.model.internal.DaoUtilities;
import org.jboss.tools.rsp.server.model.internal.Server;
import org.jboss.tools.rsp.server.spi.model.IServerModel;
import org.jboss.tools.rsp.server.spi.model.IServerModelListener;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class ServerModel implements IServerModel {
	private HashMap<String, IServerType> serverTypes;
	private HashMap<String, IServer> servers;
	private HashMap<String, IServerDelegate> serverDelegates;
	private List<IServerModelListener> listeners;
	
	private Set<String> approvedAttributeTypes;

	
	public ServerModel() {
		serverTypes = new HashMap<String, IServerType>();
		servers = new HashMap<String, IServer>();
		serverDelegates = new HashMap<String, IServerDelegate>();
		listeners = new ArrayList<IServerModelListener>();
		
		// Server attributes must be one of the following types
		approvedAttributeTypes = new HashSet<String>();
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_INT);
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_BOOL);
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_STRING);
		// List must be List<String>
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_LIST);
		// Map must be Map<String, String>
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_MAP);
	}
	
	public void addServerModelListener(IServerModelListener l) {
		listeners.add(l);
	}

	public void removeServerModelListener(IServerModelListener l) {
		listeners.remove(l);
	}

	public void addServerType(IServerType type) {
		if( type != null && type.getId() != null ) {
			serverTypes.put(type.getId(), type);
		}
	}
	
	public void removeServerType(IServerType type) {
		if( type != null && type.getId() != null ) {
			serverTypes.remove(type.getId());
		}
	}
	
	public IServer getServer(String id) {
		return servers.get(id);
	}
	
	public Map<String, IServer> getServers() {
		return Collections.unmodifiableMap(servers);
	} 
	
	@Override
	public void saveServers() throws CoreException {
		for (IServer server : getServers().values()) {
			server.save(new NullProgressMonitor());
		}
	}
	
	@Override
	public void loadServers() throws CoreException {
		File data = LaunchingCore.getDataLocation();
		File servers = new File(data, "servers");
		if (!servers.exists()) {
			return;
		}
		for (File serverFile: servers.listFiles()) {
			Server server = new Server(serverFile);
			server.load(new NullProgressMonitor());	
			addServer(server, server.getDelegate());
		}
	}
	
	public IStatus createServer(String serverType, String id, Map<String, Object> attributes) {
		try {
			return createServerUnprotected(serverType, id, attributes);
		} catch(Exception e) {
			return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
					"An unexpected error occurred", e);
		}
	}
	
	private IStatus createServerUnprotected(String serverType, String id, Map<String, Object> attributes) throws CoreException {
		if( servers.get(id) != null ) {
			return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server with id " + id + " already exists.");
		}
		IServerType fact = serverTypes.get(serverType);
		if( fact == null ) {
			return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server Type " + serverType + " not found");
		}
		IStatus valid = validateAttributes(fact, attributes);
		if( !valid.isOK()) {
			return valid;
		}
		Server server = createServer2(fact, id, attributes);
		IServerDelegate del = fact.createServerDelegate(server);
		server.setDelegate(del);
		
		valid = del.validate();
		if( !valid.isOK()) {
			return valid;
		}
		addServer(server, del);
		server.save(new NullProgressMonitor());
		return Status.OK_STATUS;
	}
	
	private IStatus validateAttributes(IServerType type, Map<String, Object> map) {
		Attributes attr = type.getRequiredAttributes();
		CreateServerAttributesUtility util = new CreateServerAttributesUtility(attr);
		Set<String> required = util.listAttributes();
		for( String attrKey : required ) {
			if( map.get(attrKey) == null ) {
				return new Status(IStatus.ERROR, "org.jboss.tools.rsp.server", "Attribute " + attrKey + " must not be null");
			}
			Object v = map.get(attrKey);
			Class actual = v.getClass();
			Class expected = DaoUtilities.getAttributeTypeClass(util.getAttributeType(attrKey));
			if( !actual.equals(expected)) {
				// Something's different than expectations based on json transfer
				// Try to convert it
				Object converted = convertJSonTransfer(v, expected);
				if( converted == null ) {
					return new Status(IStatus.ERROR, "org.jboss.tools.rsp.server", 
							"Attribute " + attrKey + " must be of type " + expected.getName() 
							+ " but is of type " + actual.getName());
				} else {
					map.put(attrKey, converted);
				}
			}
		}
		return Status.OK_STATUS;
	}
	
	
	private Object convertJSonTransfer(Object value, Class expected) {
		// TODO check more things here for errors in the transfer
		if( Integer.class.equals(expected) && Double.class.equals(value.getClass())) {
			return new Integer(((Double)value).intValue());
		}
		return null;
	}
	
	private Server createServer2(IServerType serverType, String id, Map<String, Object> attributes) {
		File data = LaunchingCore.getDataLocation();
		File servers = new File(data, "servers");
		if( !servers.exists()) {
			servers.mkdirs();
		}
		// TODO check for duplicates
		File thisServer = new File(servers, id);
		Server s = new Server(thisServer, serverType);
		s.setAttribute("id", id);
		
		Set<String> keys = attributes.keySet();
		for( String k : keys) {
			Object val = attributes.get(k);
			if( val instanceof Integer) {
				s.setAttribute(k, ((Integer)val).intValue());
			} else if( val instanceof Boolean) {
				s.setAttribute(k, ((Boolean)val).booleanValue());
			} else if( val instanceof String ) {
				s.setAttribute(k, (String)val);
			} else if( val instanceof List) {
				s.setAttribute(k, (List)val);
			} else if( val instanceof Map) {
				s.setAttribute(k, (Map)val);
			}
		}
		return s;
	}
	

	private void addServer(IServer server, IServerDelegate del) {
		servers.put(server.getId(), server);
		serverDelegates.put(server.getId(), del);
		fireServerAdded(server);
	}
	private void fireServerAdded(IServer server) {
		for( IServerModelListener l : listeners ) {
			l.serverAdded(toHandle(server));
		}
	}
	public void fireServerProcessTerminated(IServer server, String processId) {
		for( IServerModelListener l : listeners ) {
			l.serverProcessTerminated(toHandle(server), processId);
		}
	}
	public void fireServerProcessCreated(IServer server, String processId) {
		for( IServerModelListener l : listeners ) {
			l.serverProcessCreated(toHandle(server), processId);
		}
	}

	public void fireServerStreamAppended(IServer server, String processId, int streamType, String text) {
		for( IServerModelListener l : listeners ) {
			l.serverProcessOutputAppended(toHandle(server), processId, streamType, text);
		}
	}
	
	public void fireServerStateChanged(IServer server, int state) {
		for( IServerModelListener l : listeners ) {
			l.serverStateChanged(toHandle(server), state);
		}
	}
	
	private void fireServerRemoved(IServer server) {
		for( IServerModelListener l : listeners ) {
			l.serverRemoved(toHandle(server));
		}
	}
	
	private ServerHandle toHandle(IServer s) {
		String typeId = s.getTypeId();
		return new ServerHandle(s.getId(), getServerType(typeId));
	}
	
	public boolean removeServer(String serverId) {
		IServer toRemove = servers.get(serverId);
		if( toRemove == null ) {
			return false;
		}
		servers.remove(serverId);
		IServerDelegate s = serverDelegates.get(serverId);
		serverDelegates.remove(serverId);
		s.dispose();
		fireServerRemoved(toRemove);
		try {
			toRemove.delete();
		} catch (CoreException e) {
			//log silently. Looks like nothing crucial
			LaunchingCore.log(e);
		}
		return true;
	}
	

	public ServerHandle[] getServerHandles() {
		Set<String> s = servers.keySet();
		ArrayList<ServerHandle> handles = new ArrayList<>();
		for( String s1 : s ) {
			String id = s1;
			String type = servers.get(id).getTypeId();
			handles.add(new ServerHandle(id,  getServerType(type)));
		}
		return (ServerHandle[]) handles.toArray(new ServerHandle[handles.size()]);
	}
	
	private ServerType getServerType(String typeId) {
		IServerType st = serverTypes.get(typeId);
		return new ServerType(typeId, st.getName(), st.getDescription());
		
	}
	
	@Override
	public IServerType getIServerType(String typeId) {
		return serverTypes.get(typeId);
	}
	
	public ServerType[] getServerTypes() {
		Set<String> types = serverTypes.keySet();
		ArrayList<String> types2 = new ArrayList<String>(types);
		Collections.sort(types2);
		ArrayList<ServerType> ret = new ArrayList<ServerType>();
		for( String t : types2 ) {
			IServerType type = serverTypes.get(t);
			ret.add(new ServerType(t, type.getName(), type.getDescription()));
		}
		return (ServerType[]) ret.toArray(new ServerType[ret.size()]);
	}
	
	public Attributes getRequiredAttributes(String type) {
		IServerType t = serverTypes.get(type);
		Attributes ret = t == null ? null : t.getRequiredAttributes();
		return validateAttributes(ret, type);
	}
	
	public Attributes getOptionalAttributes(String type) {
		IServerType t = serverTypes.get(type);
		Attributes ret = t == null ? null : t.getOptionalAttributes();
		return validateAttributes(ret, type);
	}

	public List<ServerLaunchMode> getLaunchModes(String serverType) {
		IServerType t = serverTypes.get(serverType);
		ServerLaunchMode[] ret = t.getLaunchModes();
		return Arrays.asList(ret);
	}
	
	public Attributes getRequiredLaunchAttributes(String type) {
		IServerType t = serverTypes.get(type);
		Attributes ret = t == null ? null : t.getRequiredLaunchAttributes();
		return validateAttributes(ret, type);
	}
	
	public Attributes getOptionalLaunchAttributes(String type) {
		IServerType t = serverTypes.get(type);
		Attributes ret = t == null ? null : t.getOptionalLaunchAttributes();
		return validateAttributes(ret, type);
	}

	private Attributes validateAttributes(Attributes ret, String serverType) {
		if( ret != null ) {
			CreateServerAttributesUtility util = new CreateServerAttributesUtility(ret);
			Set<String> all = util.listAttributes();
			for( String all1 : all ) {
				String attrType = util.getAttributeType(all1);
				if( !approvedAttributeTypes.contains(attrType)) {
					LaunchingCore.log("Extension for servertype " + serverType + " is invalid and requires an attribute of an invalid class.");
				}
			}
		}
		return ret;
	}
}
