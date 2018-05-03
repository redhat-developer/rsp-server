/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import org.jboss.tools.ssp.api.ServerManagementAPIConstants;
import org.jboss.tools.ssp.api.beans.CreateServerAttributes;
import org.jboss.tools.ssp.api.beans.util.CreateServerAttributesUtility;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.ssp.server.spi.servertype.IServerType;
import org.jboss.tools.ssp.server.wildfly.beans.impl.IServerConstants;

public class JBossServerTypeFactory implements IServerType{
	private CreateServerAttributes required = null;
	private CreateServerAttributes optional = null;
	
	@Override
	public String getServerTypeId() {
		return IServerConstants.SERVER_WILDFLY_120;
	}

	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		// TODO Auto-generated method stub
		return new JBossServerDelegate(server);
	}

	@Override
	public CreateServerAttributes getRequiredAttributes() {
		if( required == null ) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			attrs.addAttribute(IJBossServerAttributes.SERVER_HOME, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"A filesystem path pointing to a WildFly installation", null);
			attrs.addAttribute(IJBossServerAttributes.VM_INSTALL_ID, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"A vm id referencing a virtual machine already in this model.", null);
			// TODO add some
			required = attrs.toPojo();
		}
		return required;
	}

	@Override
	public CreateServerAttributes getOptionalAttributes() {
		if( optional == null ) {
			CreateServerAttributes attrs = new CreateServerAttributes();
			// TODO add some
			optional = attrs;
		}
		return optional;
	}

}
