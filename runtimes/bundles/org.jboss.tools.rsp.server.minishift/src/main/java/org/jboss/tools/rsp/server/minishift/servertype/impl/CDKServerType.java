/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.server.minishift.servertype.BaseMinishiftServerType;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class CDKServerType extends BaseMinishiftServerType {
	public CDKServerType(String id, String name, String desc) {
		super(id, name, desc);
	}
	
	@Override
	public Attributes getRequiredAttributes() {
		if (this.required == null) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			
			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A filesystem path pointing to a minishift binary file.", null);

			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A registration username.", null);

			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A registration password", null);
			this.required = attrs.toPojo();
		}
		return this.required;
	}

	protected boolean isCDK() {
		return true;
	}
	
	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		MinishiftServerDelegate ret = new MinishiftServerDelegate(server);
		return ret;
	}
}
