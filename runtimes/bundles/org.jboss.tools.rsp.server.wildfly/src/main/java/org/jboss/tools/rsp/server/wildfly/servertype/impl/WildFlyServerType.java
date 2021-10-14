/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.BaseJBossServerType;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;

public class WildFlyServerType extends BaseJBossServerType {
	public WildFlyServerType(String id, String name, String desc) {
		super(id, name, desc);
	}

	@Override
	public IServerDelegate createServerDelegateImpl(IServer server) {
		return new WildFlyServerDelegate(server);
	}
	
	protected void fillOptionalAttributes(CreateServerAttributesUtility attrs) {
		super.fillOptionalAttributes(attrs);
		attrs.addAttribute(IJBossServerAttributes.WILDFLY_CONFIG_FILE, 
				ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FILE, 
				"Set the configuration file you want your WildFly instance to use.", 
				IJBossServerAttributes.WILDFLY_CONFIG_FILE_DEFAULT);

		attrs.addAttribute(IJBossServerAttributes.WILDFLY_DEPLOY_DIR, 
				ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FOLDER, 
				"Override the directory tools should deploy to. Path may be relative to the server home, or absolute.", 
				"");
	}
}
