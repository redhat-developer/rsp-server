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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.launching.utils.IMemento;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.server.core.internal.SecuredBase;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class Server extends SecuredBase implements IServer {

	public static final String TYPE_ID = "org.jboss.tools.rsp.server.typeId";

	private IServerDelegate delegate;
	private IServerType serverType;
	
	public Server(File file, ISecureStorageProvider storage) {
		super(file, storage);
	}
	
	public Server(File file, IServerType type, String id, Map<String, Object> attributes, ISecureStorageProvider storage) {
		super(file, id, storage);
		this.serverType = type;
		if( this.serverType != null ) {
			setAttribute(TYPE_ID, type.getId());
			this.delegate = this.serverType.createServerDelegate(this);
		}
		setAttributes(attributes);
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
		if( this.delegate != null ) {
			memento.putString("mode", this.delegate.getMode());
			memento.putInteger("state", this.delegate.getServerState());
		}
	}
	
	@Override
	protected void loadState(IMemento memento) {
	}
	
	@Override
	public String getTypeId() {
		return getAttribute(TYPE_ID, (String)null);
	}

	public IServerType getServerType() {
		return serverType;
	}
	
	public void setServerType(IServerType type) {
		this.serverType = type;
		if( type != null ) 
			setAttribute(TYPE_ID, type.getId());
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

	private void setAttributes(Map<String, Object> attributes) {
		Set<String> keys = attributes.keySet();
		for( String k : keys) {
			setAttribute(k, attributes.get(k));
		}
	}

	protected void setAttribute(String k, Object val) {
		if( val instanceof Integer) {
			setAttribute(k, ((Integer)val).intValue());
		} else if( val instanceof Boolean) {
			setAttribute(k, ((Boolean)val).booleanValue());
		} else if( val instanceof String ) {
			setAttribute(k, (String)val);
		} else if( val instanceof List) {
			setAttribute(k, (List<?>)val);
		} else if( val instanceof Map) {
			setAttribute(k, (Map<?,?>)val);
		}
	}

}
