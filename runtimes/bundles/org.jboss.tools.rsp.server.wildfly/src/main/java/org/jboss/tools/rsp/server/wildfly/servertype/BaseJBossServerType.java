/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.launching.java.ILaunchModes;
import org.jboss.tools.rsp.server.spi.servertype.AbstractServerType;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public abstract class BaseJBossServerType extends AbstractServerType {
	protected Attributes required = null;
	protected Attributes optional = null;
	
	public BaseJBossServerType(String id, String name, String desc) {
		super(id, name, desc);
	}
	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		return createServerDelegateImpl(server);
	}
	
	protected abstract IServerDelegate createServerDelegateImpl(IServer server);

	@Override
	public Attributes getRequiredAttributes() {
		if( required == null ) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			attrs.addAttribute(IJBossServerAttributes.SERVER_HOME, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"A filesystem path pointing to a server installation's root directory", null);
			required = attrs.toPojo();
		}
		return required;
	}

	@Override
	public Attributes getOptionalAttributes() {
		if( optional == null ) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			fillOptionalAttributes(attrs);
			optional = attrs.toPojo();
		}
		return optional;
	}

	protected void fillOptionalAttributes(CreateServerAttributesUtility attrs) {
		attrs.addAttribute(IJBossServerAttributes.VM_INSTALL_PATH, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING, 
				"A string representation pointing to a java home. If not set, java.home will be used instead.", null);

		attrs.addAttribute(IJBossServerAttributes.AUTOPUBLISH_ENABLEMENT, 
				ServerManagementAPIConstants.ATTR_TYPE_BOOL, 
				"Enable the autopublisher.", 
				IJBossServerAttributes.AUTOPUBLISH_ENABLEMENT_DEFAULT);

		attrs.addAttribute(IJBossServerAttributes.AUTOPUBLISH_INACTIVITY_LIMIT, 
				ServerManagementAPIConstants.ATTR_TYPE_INT, 
				"Set the inactivity limit before the autopublisher runs.", 
				IJBossServerAttributes.AUTOPUBLISH_INACTIVITY_LIMIT_DEFAULT);

		attrs.addAttribute(IJBossServerAttributes.JBOSS_SERVER_HOST, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING, 
				"Set the host you want your JBoss / WildFly instance to bind to. Use 0.0.0.0 for all.", 
				IJBossServerAttributes.JBOSS_SERVER_HOST_DEFAULT);

		attrs.addAttribute(IJBossServerAttributes.JBOSS_SERVER_PORT, 
				ServerManagementAPIConstants.ATTR_TYPE_INT, 
				"Set the port you want your JBoss / WildFly instance to bind to", 
				IJBossServerAttributes.JBOSS_SERVER_PORT_DEFAULT);
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

	@Override
	public ServerLaunchMode[] getLaunchModes() {
		return new ServerLaunchMode[] {
				new ServerLaunchMode(ILaunchModes.RUN, ILaunchModes.RUN_DESC),
				new ServerLaunchMode(ILaunchModes.DEBUG, ILaunchModes.DEBUG_DESC)
		};
	}

}
