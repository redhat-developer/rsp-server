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
import org.jboss.tools.ssp.api.beans.SSPAttributes;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.ssp.server.spi.servertype.IServerType;
import org.jboss.tools.ssp.server.wildfly.beans.impl.IServerConstants;

public class JBossServerTypeFactory implements IServerType{
	private SSPAttributes required = null;
	private SSPAttributes optional = null;
	
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
	public SSPAttributes getRequiredAttributes() {
		if( required == null ) {
			SSPAttributes attrs = new SSPAttributes();
			attrs.addAttribute(IJBossServerAttributes.SERVER_HOME, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"A filesystem path pointing to a WildFly installation", null);
			attrs.addAttribute(IJBossServerAttributes.VM_INSTALL_ID, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"A vm id referencing a virtual machine already in this model.", null);
			// TODO add some
			required = attrs;
		}
		return required;
	}

	@Override
	public SSPAttributes getOptionalAttributes() {
		if( optional == null ) {
			SSPAttributes attrs = new SSPAttributes();
			// TODO add some
			optional = attrs;
		}
		return optional;
	}

}
