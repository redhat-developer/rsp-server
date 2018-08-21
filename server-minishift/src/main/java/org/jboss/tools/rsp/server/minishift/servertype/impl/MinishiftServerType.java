/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.server.minishift.servertype.BaseMinishiftServerType;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class MinishiftServerType extends BaseMinishiftServerType {
	public MinishiftServerType(String id, String name, String desc) {
		super(id, name, desc);
	}

	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		MinishiftServerDelegate ret = new MinishiftServerDelegate(server);
		return ret;
	}
}
