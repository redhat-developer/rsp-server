/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.launchers;

import org.jboss.tools.rsp.foundation.core.launchers.CommandConfig;
import org.jboss.tools.rsp.foundation.core.launchers.GenericProcessRunner;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class GenericServerProcessRunner extends GenericProcessRunner {

	private IServerDelegate serverDel;

	public GenericServerProcessRunner(IServerDelegate serverDel, CommandConfig config) {
		super(config);
		this.serverDel = serverDel;
	}
	
	public IServer getServer() {
		return serverDel == null ? null : serverDel.getServer();
	}
}
