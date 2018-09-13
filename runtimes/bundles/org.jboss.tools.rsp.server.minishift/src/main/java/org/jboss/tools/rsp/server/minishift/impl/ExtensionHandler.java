/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.impl;

import org.jboss.tools.rsp.server.minishift.discovery.MinishiftBeanTypeProvider;
import org.jboss.tools.rsp.server.minishift.servertype.impl.MinishiftServerTypes;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;

public class ExtensionHandler {
	
	public static void addExtensionsToModel(IServerManagementModel model) {
		model.getServerBeanTypeManager().addTypeProvider(new MinishiftBeanTypeProvider());
		model.getServerModel().addServerType(MinishiftServerTypes.MINISHIFT_1_12_SERVER_TYPE);
	}
	
}
