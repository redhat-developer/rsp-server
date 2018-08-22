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

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.launching.utils.IMemento;
import org.jboss.tools.rsp.server.core.internal.Base;
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
		memento.putString("mode", this.delegate.getMode());
		memento.putInteger("state", this.delegate.getServerState());
	}
	
	@Override
	protected void loadState(IMemento memento) {
		loadServerType(memento);
		this.delegate = this.serverType.createServerDelegate(this);
	}
	
	protected void loadServerType(IMemento memento) {
		this.serverType = ServerManagementModel.getDefault().getServerModel().getIServerType(getTypeId());
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
