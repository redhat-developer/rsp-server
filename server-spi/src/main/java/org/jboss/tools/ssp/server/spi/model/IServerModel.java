/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.spi.model;

import java.util.Map;

import org.jboss.tools.ssp.api.dao.Attributes;
import org.jboss.tools.ssp.api.dao.ServerHandle;
import org.jboss.tools.ssp.api.dao.ServerType;
import org.jboss.tools.ssp.eclipse.core.runtime.IStatus;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerType;

public interface IServerModel {

	ServerType[] getServerTypes();

	IServer getServer(String id);

	ServerHandle[] getServerHandles();

	Attributes getRequiredAttributes(String id);

	Attributes getOptionalAttributes(String id);

	Attributes getRequiredLaunchAttributes(String id);

	Attributes getOptionalLaunchAttributes(String id);

	IStatus createServer(String serverType, String id, Map<String, Object> attributes);

	void removeServer(String id);

	void fireServerStateChanged(IServer server, int state);

	void fireServerProcessTerminated(IServer server, String processId);

	void fireServerProcessCreated(IServer server, String processId);

	void fireServerStreamAppended(IServer server2, String processId, int streamType, String text);

	void addServerModelListener(IServerModelListener listener);

	void addServerType(IServerType serverType);
	void removeServerType(IServerType serverType);
	

}
