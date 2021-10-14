/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.server.minishift.servertype.BaseMinishiftServerType;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class CRCServerType extends BaseMinishiftServerType{

	public CRCServerType(String id, String name, String desc) {
		super(id, name, desc);
	}

	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		return new CRCServerDelegate(server);
	}
	
	@Override
	protected void fillRequiredAttributes(CreateServerAttributesUtility attrs) {
		attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, 
				ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FILE,
				"A filesystem path pointing to a crc binary file.", null);
		attrs.addAttribute(IMinishiftServerAttributes.CRC_IMAGE_PULL_SECRET, 
				ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FILE,
				"A filesystem path pointing to your CRC Pull Secret file.", null);
	}
	
	@Override
	protected void fillOptionalAttributes(CreateServerAttributesUtility attrs) {
	}

}
