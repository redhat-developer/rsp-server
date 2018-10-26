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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.launching.utils.StatusConverter;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.secure.model.NullSecureStorageProvider;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.model.internal.DaoUtilities;
import org.jboss.tools.rsp.server.model.internal.Server;
import org.jboss.tools.rsp.server.spi.model.IServerModel;
import org.jboss.tools.rsp.server.spi.model.IServerModelListener;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerModel implements IServerModel {
	private static final Logger LOG = LoggerFactory.getLogger(ServerModel.class);

	private static final String SERVERS_DIRECTORY = "servers";

	private HashMap<String, IServerType> serverTypes;
	private HashMap<String, IServer> servers;
	private HashMap<String, IServerDelegate> serverDelegates;
	private List<IServerModelListener> listeners;
	
	private Set<String> approvedAttributeTypes;
	private ISecureStorageProvider secureStorageProvider;

	
	public ServerModel() {
		this(new NullSecureStorageProvider());
	}
	
	public ServerModel(ISecureStorageProvider provider) {
		this.secureStorageProvider = provider;
		this.serverTypes = new HashMap<>();
		this.servers = new HashMap<>();
		this.serverDelegates = new HashMap<>();
		this.listeners = new ArrayList<>();
		
		// Server attributes must be one of the following types
		this.approvedAttributeTypes = new HashSet<>();
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_INT);
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_BOOL);
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_STRING);
		// List must be List<String>
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_LIST);
		// Map must be Map<String, String>
		approvedAttributeTypes.add(ServerManagementAPIConstants.ATTR_TYPE_MAP);
	}
	
	@Override
	public ISecureStorageProvider getSecureStorageProvider() {
		return secureStorageProvider;
	}
	
	@Override
	public void addServerModelListener(IServerModelListener l) {
		listeners.add(l);
	}

	public void removeServerModelListener(IServerModelListener l) {
		listeners.remove(l);
	}

	@Override
	public void addServerType(IServerType type) {
		if( type != null && type.getId() != null ) {
			serverTypes.put(type.getId(), type);
		}
	}
	
	@Override
	public void addServerTypes(IServerType[] types) {
		if (types == null) {
			return;
		}
		
		for (IServerType type : types) {
			addServerType(type);
		}
	}

	@Override
	public void removeServerType(IServerType type) {
		if( type != null && type.getId() != null ) {
			serverTypes.remove(type.getId());
		}
	}
	
	@Override
	public void removeServerTypes(IServerType[] types) {
		if (types == null) {
			return;
		}
		
		for (IServerType type : types) {
			removeServerType(type);
		}
	}

	@Override
	public IServer getServer(String id) {
		return servers.get(id);
	}
	
	@Override
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
		File servers = new File(data, SERVERS_DIRECTORY);
		loadServers(servers);
	}

	public void loadServers(File folder) {
		if (!folder.exists()) {
			return;
		}
		for (File serverFile: folder.listFiles()) {
			Server server = loadServer(serverFile);
			if( server != null )
				addServer(server, server.getDelegate());

		}
	}
	
	private Server loadServer(File serverFile) {
		Server server = new Server(serverFile, secureStorageProvider);
		try {
			server.load(new NullProgressMonitor());
			String tid = server.getTypeId();
			IServerType st = getIServerType(tid);
			if( st != null ) {
				server.setServerType(st);
				server.setDelegate(st.createServerDelegate(server));
			}
			
			if( server.getServerType() == null ) {
				String typeId = server.getAttribute(Server.TYPE_ID, (String)null);
				if( typeId == null ) {
					log(new Exception(
							"Unable to load server from file " + serverFile.getAbsolutePath() + "; server type is missing or null."));
				} else if( getServerType(typeId) == null ) {
					log(new Exception(
							"Unable to load server from file " + serverFile.getAbsolutePath() + "; server type " + typeId + " is not found in model."));
				}
				return null;
			} else {
				return server;
			}
		} catch(CoreException ce) {
			log(new Exception("Unable to load server from file " + serverFile.getAbsolutePath(), ce));
			return null;
		}
	}
	
	private void log(Exception e) {
		LOG.error(e.getMessage(), e);
	}
	
	@Override
	public CreateServerResponse createServer(String serverType, String id, Map<String, Object> attributes) {
		try {
			return createServerUnprotected(serverType, id, attributes);
		} catch(CoreException e) {
			return new CreateServerResponse(StatusConverter.convert(e.getStatus()), null);
		} catch(Exception e) {
			Status s = new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
					"An unexpected error occurred", e);
			return new CreateServerResponse(StatusConverter.convert(s), null);
		}
	}
	
	private CreateServerResponse createServerUnprotected(String serverType, String id, Map<String, Object> attributes) throws CoreException {
		IServerType type = getServerType(serverType, id);
		IStatus validAttributes = validateAttributes(type, attributes);
		if( !validAttributes.isOK()) {
			throw new CoreException(validAttributes);
		}
		
		Server server = createServer2(type, id, attributes);
		IServerDelegate del = server.getDelegate();

		CreateServerValidation valid = del.validate();
		if( !valid.getStatus().isOK()) {
			return valid.toDao();
		}
		addServer(server, del);
		server.save(new NullProgressMonitor());
		return valid.toDao();
	}

	private IServerType getServerType(String serverType, String id) throws CoreException {
		IServerType type = null;
		if( servers.get(id) != null ) {
			throw new CoreException(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server with id " + id + " already exists."));
		} else {
			type = serverTypes.get(serverType);
			if( type == null ) {
				throw new CoreException(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server Type " + serverType + " not found"));
			}
		}
		return type;
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
			return Integer.valueOf(((Double)value).intValue());
		}
		return null;
	}

	private Server createServer2(IServerType serverType, String id, Map<String, Object> attributes) {
		File data = LaunchingCore.getDataLocation();
		File serversDirectory = new File(data, SERVERS_DIRECTORY);
		if( !serversDirectory.exists()) {
			serversDirectory.mkdirs();
		}
		// TODO check for duplicates
		File serverFile = new File(serversDirectory, id);
		Server s = new Server(serverFile, serverType, secureStorageProvider);
		s.setAttribute("id", id);

		Set<String> keys = attributes.keySet();
		for( String k : keys) {
			setAttribute(s, k, attributes.get(k));
		}
		
		return s;
	}
	
	private void setAttribute(Server s, String k, Object val) {
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

	@Override
	public void fireServerProcessTerminated(IServer server, String processId) {
		for( IServerModelListener l : listeners ) {
			l.serverProcessTerminated(toHandle(server), processId);
		}
	}

	@Override
	public void fireServerProcessCreated(IServer server, String processId) {
		for( IServerModelListener l : listeners ) {
			l.serverProcessCreated(toHandle(server), processId);
		}
	}

	@Override
	public void fireServerStreamAppended(IServer server, String processId, int streamType, String text) {
		for( IServerModelListener l : listeners ) {
			l.serverProcessOutputAppended(toHandle(server), processId, streamType, text);
		}
	}
	
	@Override
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
	
	@Override
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
			log(e);
		}
		return true;
	}
	
	@Override
	public ServerHandle[] getServerHandles() {
		Set<String> serverKeys = servers.keySet();
		ArrayList<ServerHandle> handles = new ArrayList<>();
		for( String serverKey : serverKeys ) {
			String id = serverKey;
			String type = servers.get(id).getTypeId();
			handles.add(new ServerHandle(id,  getServerType(type)));
		}
		return handles.toArray(new ServerHandle[handles.size()]);
	}
	
	private ServerType getServerType(String typeId) {
		IServerType st = serverTypes.get(typeId);
		if( st == null )
			return null;
		return new ServerType(typeId, st.getName(), st.getDescription());
		
	}
	
	@Override
	public IServerType getIServerType(String typeId) {
		return serverTypes.get(typeId);
	}
	
	@Override
	public ServerType[] getServerTypes() {
		Set<String> types = serverTypes.keySet();
		ArrayList<String> types2 = new ArrayList<>(types);
		Collections.sort(types2);
		ArrayList<ServerType> ret = new ArrayList<>();
		for( String t : types2 ) {
			IServerType type = serverTypes.get(t);
			ret.add(new ServerType(t, type.getName(), type.getDescription()));
		}
		return ret.toArray(new ServerType[ret.size()]);
	}

	@Override
	public ServerType[] getAccessibleServerTypes() {
		List<ServerType> free = new ArrayList<>();
		List<ServerType> all = new ArrayList<>();
		
		Set<String> types = serverTypes.keySet();
		ArrayList<String> types2 = new ArrayList<>(types);
		Collections.sort(types2);
		for( String t : types2 ) {
			// Always add to 'all',   add to 'free' if type does not require secure storage
			IServerType type = serverTypes.get(t);
			ServerType st = new ServerType(t, type.getName(), type.getDescription());
			if( !hasSecureAttributes(type)) {
				free.add(st);
			}
			all.add(st);
		}
		
		if (all.size() > free.size()
				&& !hasPermissions()) {
			return free.toArray(new ServerType[free.size()]);
		}
		
		return all.toArray(new ServerType[all.size()]);
	}
	
	private boolean hasPermissions() {
		return secureStorageProvider.getSecureStorage(true) != null;
	}

	private boolean hasSecureAttributes(IServerType type) {
		Attributes a = type.getRequiredAttributes();
		Attributes b = type.getOptionalAttributes();
		Set<String> all = new HashSet<>();
		all.addAll(a.getAttributes().keySet());
		all.addAll(b.getAttributes().keySet());
		for( Iterator<String> i = all.iterator(); i.hasNext(); ) {
			if( i.next().startsWith(IServerModel.SECURE_ATTRIBUTE_PREFIX)) 
				return true;
		}
		return false;
	}

	@Override
	public Attributes getRequiredAttributes(String type) {
		IServerType t = serverTypes.get(type);
		Attributes ret = t == null ? null : t.getRequiredAttributes();
		return validateAttributes(ret, type);
	}
	
	@Override
	public Attributes getOptionalAttributes(String type) {
		IServerType t = serverTypes.get(type);
		Attributes ret = t == null ? null : t.getOptionalAttributes();
		return validateAttributes(ret, type);
	}

	@Override
	public List<ServerLaunchMode> getLaunchModes(String serverType) {
		IServerType t = serverTypes.get(serverType);
		ServerLaunchMode[] ret = t == null ? null : t.getLaunchModes();
		return ret == null ? null : Arrays.asList(ret);
	}
	
	@Override
	public Attributes getRequiredLaunchAttributes(String type) {
		IServerType t = serverTypes.get(type);
		Attributes ret = t == null ? null : t.getRequiredLaunchAttributes();
		return validateAttributes(ret, type);
	}
	
	@Override
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
					LOG.error("Extension for servertype {} is invalid and requires an attribute of an invalid type.", serverType);
					util.removeAttribute(all1);
				}
			}
			return util.toPojo();
		}
		return null;
	}
}
