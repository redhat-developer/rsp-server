/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.launching.java.ILaunchModes;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public abstract class BaseJBossServerType implements IServerType{
	protected Attributes required = null;
	protected Attributes optional = null;
	
	private String id;
	private String name;
	private String desc;
	
	public BaseJBossServerType(String id, String name, String desc) {
		this.name = name;
		this.id = id;
		this.desc = desc;
	}
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public String getDescription() {
		return desc;
	}
	
	@Override
	public abstract IServerDelegate createServerDelegate(IServer server);


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
			attrs.addAttribute(IJBossServerAttributes.VM_INSTALL_PATH, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING, 
					"A string representation pointing to a java home. If not set, java.home will be used instead.", null);
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

	@Override
	public ServerLaunchMode[] getLaunchModes() {
		return new ServerLaunchMode[] {
				new ServerLaunchMode(ILaunchModes.RUN, ILaunchModes.RUN_DESC),
				new ServerLaunchMode(ILaunchModes.DEBUG, ILaunchModes.DEBUG_DESC)
		};
	}

}
