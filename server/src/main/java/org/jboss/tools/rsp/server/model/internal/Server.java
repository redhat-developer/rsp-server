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

import org.jboss.tools.rsp.server.core.internal.Base;
import org.jboss.tools.rsp.server.core.internal.IMemento;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class Server extends Base implements IServer {
	private static final String TYPE_ID = "org.jboss.tools.rsp.server.typeId";
	private IServerDelegate delegate;
	private IServerType serverType;
	public Server(File file, IServerType type) {
		super(file);
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
		// Intentionally empty, may be removed
	}

	@Override
	protected void loadState(IMemento memento) {
		// Intentionally empty, may be removed
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

}
