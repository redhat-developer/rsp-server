/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model;

import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;

public class ServerModelListenerAdapter implements IServerModelListener {

	@Override
	public void serverAdded(ServerHandle server) {
	}

	@Override
	public void serverRemoved(ServerHandle server) {
	}

	@Override
	public void serverAttributesChanged(ServerHandle server) {
	}

	@Override
	public void serverStateChanged(ServerHandle server, ServerState state) {
	}

	@Override
	public void serverProcessCreated(ServerHandle server, String processId) {
	}

	@Override
	public void serverProcessTerminated(ServerHandle server, String processId) {
	}

	@Override
	public void serverProcessOutputAppended(ServerHandle server, String processId, int streamType, String text) {
	}

}
