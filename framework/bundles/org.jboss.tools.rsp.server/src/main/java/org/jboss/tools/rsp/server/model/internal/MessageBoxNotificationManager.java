/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal;

import java.util.List;

import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.dao.MessageBoxNotification;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.spi.model.ICapabilityManagement;

public class MessageBoxNotificationManager {
	public static void messageAllClients(MessageBoxNotification message) {
		List<RSPClient> clients = LauncherSingleton.getDefault().getLauncher().getClients();
		for( RSPClient c : clients ) {
			messageClient(c, message);
		}
	}

	public static void messageClient(RSPClient client, MessageBoxNotification message) {
		ICapabilityManagement mgmt = LauncherSingleton.getDefault().getLauncher().getModel().getCapabilityManagement();
		String val = mgmt.getCapabilityProperty(client, ICapabilityKeys.BOOLEAN_MESSAGEBOX);
		if( val != null && Boolean.parseBoolean(val)) {
			client.messageBox(message);
		}
	}
}
