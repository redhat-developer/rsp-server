/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.server.core.internal.Base;
import org.jboss.tools.rsp.server.core.internal.IMemento;
import org.jboss.tools.rsp.server.model.ServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class Server extends Base implements IServer {
	private static final String TYPE_ID = "org.jboss.tools.rsp.server.typeId";
	private IServerDelegate delegate;
	private IServerType serverType;
	
	public Server(File file) {
		super(file);
	}
	
	public Server(File file, IServerType type) {
		super(file, type.getId());
		this.serverType = type;
		setAttribute(TYPE_ID, type.getId());
	}
	
	@Override
	public String getName() {
		return getId();
	}

	@Override
	protected String getXMLRoot() {
		return "server";
	}

	@Override
	protected void saveState(IMemento memento) {
		saveServerType(memento);
		
		memento.putString("mode", this.delegate.getMode());
		memento.putInteger("state", this.delegate.getServerState());
	}
	
	protected void saveServerType(IMemento memento) {
		Map<String, Attribute> optionalAttributes = serverType.getOptionalAttributes().getAttributes();
		Map<String, Attribute> requiredAttributes = serverType.getRequiredAttributes().getAttributes();
		Map<String, Attribute> optionalLaunchAttributes = serverType.getOptionalLaunchAttributes().getAttributes();
		Map<String, Attribute> requiredLaunchAttributes = serverType.getRequiredLaunchAttributes().getAttributes();
		
		IMemento serverTypeMemento = memento.createChild("serverType");
		saveAttributes(serverTypeMemento, optionalAttributes, "optionalAttributes");
		saveAttributes(serverTypeMemento, requiredAttributes, "requiredAttributes");
		saveAttributes(serverTypeMemento, optionalLaunchAttributes, "optionalLaunchAttributes");
		saveAttributes(serverTypeMemento, requiredLaunchAttributes, "requiredLaunchAttributes");
	}
	
	protected void saveAttributes(IMemento root, Map<String, Attribute> attributes, String name) {
		IMemento attributesMemento = root.createChild(name);
		for (Map.Entry<String, Attribute> attributeEntry: attributes.entrySet()) {
			IMemento attributeMemento = attributesMemento.createChild(attributeEntry.getKey());
			Attribute attribute = attributeEntry.getValue();
			
			attributeMemento.putString("type", attribute.getType());
			attributeMemento.putString("description", attribute.getDescription());
			
			switch(attribute.getType()) {
				case ServerManagementAPIConstants.ATTR_TYPE_INT: 
					attributeMemento.putInteger("defaultValue", (Integer)attribute.getDefaultVal());
					break;
				case ServerManagementAPIConstants.ATTR_TYPE_BOOL:
					attributeMemento.putBoolean("defaultValue", (Boolean)attribute.getDefaultVal());
					break;
				case ServerManagementAPIConstants.ATTR_TYPE_STRING:
					attributeMemento.putString("defaultValue", (String)attribute.getDefaultVal());
					break;
				case ServerManagementAPIConstants.ATTR_TYPE_LIST:
					saveList(attributeMemento, "defaultValue", (List)attribute.getDefaultVal());
					break;
				case ServerManagementAPIConstants.ATTR_TYPE_MAP:
					saveMap(attributeMemento, "defaultValue", (Map)attribute.getDefaultVal());
					break;
				default:
					attributeMemento.putString("defaultValue", String.valueOf(attribute.getDefaultVal()));
					break;
			}
		}
	}

	@Override
	protected void loadState(IMemento memento) {
		loadServerType(memento);
		this.delegate = this.serverType.createServerDelegate(this);
	}
	
	protected void loadServerType(IMemento memento) {
		this.serverType = ServerManagementModel.getDefault().getServerModel().getIServerTypeById(getTypeId());
		
		IMemento serverTypeMemento = memento.getChild("serverType");
		if (serverTypeMemento != null) {
			loadAttributes(serverTypeMemento, serverType.getOptionalAttributes(), "optionalAttributes");
			loadAttributes(serverTypeMemento, serverType.getRequiredAttributes(), "requiredAttributes");
			loadAttributes(serverTypeMemento, serverType.getOptionalLaunchAttributes(), "optionalLaunchAttributes");
			loadAttributes(serverTypeMemento, serverType.getRequiredLaunchAttributes(), "requiredLaunchAttributes");
		}
	}
	
	protected void loadAttributes(IMemento root, Attributes loadTo, String name) {
		IMemento attributesMemento = root.getChild(name);
		if (attributesMemento == null) {
			return;
		}
		Map<String, Attribute> attributesMap = new HashMap<>();
		for (IMemento attributeMemento: attributesMemento.getChildren()) {
			String type = attributeMemento.getString("type");
			String desc = attributeMemento.getString("description");
			Object defaultValue = null;
			switch(type) {
				case ServerManagementAPIConstants.ATTR_TYPE_INT: 
					defaultValue = attributeMemento.getInteger("defaultValue");
					break;
				case ServerManagementAPIConstants.ATTR_TYPE_BOOL:
					defaultValue = attributeMemento.getBoolean("defaultValue");
					break;
				case ServerManagementAPIConstants.ATTR_TYPE_STRING:
					defaultValue = attributeMemento.getString("defaultValue");
					break;
				case ServerManagementAPIConstants.ATTR_TYPE_LIST:
					IMemento listValueMemento = attributeMemento.getChild("list");
					defaultValue = getListFromMemento(listValueMemento);
					break;
				case ServerManagementAPIConstants.ATTR_TYPE_MAP:
					IMemento mapValueMemento = attributeMemento.getChild("map");
					defaultValue = getMapFromMemento(mapValueMemento);
					break;
			}
			attributesMap.put(attributeMemento.getNodeName(), new Attribute(type, desc, defaultValue));
		}
		loadTo.setAttributes(attributesMap);
	}

	@Override
	public String getTypeId() {
		return getAttribute(TYPE_ID, (String)null);
	}

	public IServerType getServerType() {
		return serverType;
	}
	
	public void setDelegate(IServerDelegate del) {
		delegate = del;
	}

	@Override
	public IServerDelegate getDelegate() {
		return delegate;
	}
	
	public void save(IProgressMonitor monitor) throws CoreException {
		super.saveToFile(monitor);
	}
	
	public void load(IProgressMonitor monitor) throws CoreException {
		super.loadFromFile(monitor);
	}

}
