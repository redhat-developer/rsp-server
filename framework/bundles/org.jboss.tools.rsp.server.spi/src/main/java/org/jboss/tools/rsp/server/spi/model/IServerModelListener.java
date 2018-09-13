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

public interface IServerModelListener {
	void serverAdded(ServerHandle server);
	
	void serverRemoved(ServerHandle server);
	
	void serverAttributesChanged(ServerHandle server);
	
	void serverStateChanged(ServerHandle server, int state);
	
	void serverProcessCreated(ServerHandle server, String processId);
	
	void serverProcessTerminated(ServerHandle server, String processId);
	
	void serverProcessOutputAppended(ServerHandle server, String processId, int streamType, String text);

}
