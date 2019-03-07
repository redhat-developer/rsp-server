/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.util;

import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.ServerTypeStringConstants;

/**
 * 
 * @author odockal
 *
 */
public class RSPServerUtility {

	public static ServerType getServerType(String serverID) {
		switch (serverID) {
			case IServerConstants.SERVER_WILDFLY_130:
				return createServerType(serverID, ServerTypeStringConstants.WF13_NAME, ServerTypeStringConstants.WF13_DESC);
			case IServerConstants.SERVER_WILDFLY_140:
				return createServerType(serverID, ServerTypeStringConstants.WF14_NAME, ServerTypeStringConstants.WF14_DESC);
			case IServerConstants.SERVER_WILDFLY_150:
				return createServerType(serverID, ServerTypeStringConstants.WF15_NAME, ServerTypeStringConstants.WF15_DESC);
			case IServerConstants.SERVER_WILDFLY_160:
				return createServerType(serverID, ServerTypeStringConstants.WF16_NAME, ServerTypeStringConstants.WF16_DESC);
			default:
				return createServerType(IServerConstants.SERVER_WILDFLY_120, ServerTypeStringConstants.WF12_NAME, ServerTypeStringConstants.WF12_DESC);
		}
	}
	
	public static ServerType createServerType(String id, String name, String description) {
		return new ServerType(id, name, description);
	}
	
}
