/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.BaseJBossServerType;

public class WildFlyServerType extends BaseJBossServerType {
	public WildFlyServerType(String id, String name, String desc) {
		super(id, name, desc);
	}

	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		WildFlyServerDelegate ret = new WildFlyServerDelegate(server);
		return ret;
	}
}
