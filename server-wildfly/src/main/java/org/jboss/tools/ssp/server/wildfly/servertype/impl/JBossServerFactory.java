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
import org.jboss.tools.ssp.api.dao.Attributes;
import org.jboss.tools.ssp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.ssp.server.spi.servertype.IServerType;
import org.jboss.tools.ssp.server.wildfly.beans.impl.IServerConstants;

public class JBossServerFactory implements IServerType{
	private Attributes required = null;
	private Attributes optional = null;
	
	@Override
	public String getId() {
		return IServerConstants.SERVER_WILDFLY_120;
	}

	@Override
	public String getName() {
		return "WildFly 12.x";
	}
	@Override
	public String getDescription() {
		return "A server adapter capable of discovering and controlling a WildFly 12.x runtime instance.";
	}
	
	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		JBossServerDelegate ret = new JBossServerDelegate(server);
		return ret;
	}

	@Override
	public Attributes getRequiredAttributes() {
		if( required == null ) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			attrs.addAttribute(IJBossServerAttributes.SERVER_HOME, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"A filesystem path pointing to a WildFly installation", null);
			required = attrs.toPojo();
		}
		return required;
	}

	@Override
	public Attributes getOptionalAttributes() {
		if( optional == null ) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			attrs.addAttribute(IJBossServerAttributes.VM_INSTALL_PATH, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"A string representation pointing to a java home. If not set, $JAVA_HOME will be used instead.", null);
			optional = attrs.toPojo();
		}
		return optional;
	}

	@Override
	public Attributes getRequiredLaunchAttributes() {
		CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
		return attrs.toPojo();
	}

	@Override
	public Attributes getOptionalLaunchAttributes() {
		CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
		return attrs.toPojo();
	}

}
