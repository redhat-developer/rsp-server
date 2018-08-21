/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.impl;

import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerTypes;

public class ExtensionHandler {
	
	public static void addExtensionsToModel(IServerManagementModel model) {
		model.getServerBeanTypeManager().addTypeProvider(new JBossServerBeanTypeProvider());
		model.getServerModel().addServerType(WildFlyServerTypes.WF13_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.WF12_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.WF11_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.WF10_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.WF9_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.WF8_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.WF71_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.WF7_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.AS6_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.AS51_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.AS5_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.AS42_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.AS4_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.AS32_SERVER_TYPE);
		
		model.getServerModel().addServerType(WildFlyServerTypes.EAP43_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.EAP50_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.EAP60_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.EAP61_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.EAP70_SERVER_TYPE);
		model.getServerModel().addServerType(WildFlyServerTypes.EAP71_SERVER_TYPE);
	}
	
}
