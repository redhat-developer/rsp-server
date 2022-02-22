/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc. Distributed under license by Red Hat, Inc.
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

public class EapXpServerType extends AbstractServerType {
	protected Attributes required = null;
	protected Attributes optional = null;
	public EapXpServerType(String id, String name, String desc) {
		super(id, name, desc);
	}

	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		return new EapXpServerDelegate(server);
	}
	
	@Override
	public Attributes getRequiredAttributes() {
		if (required == null) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			fillRequiredAttributes(attrs);
			this.required = attrs.toPojo();
		}
		return required;
	}
	
	protected void fillRequiredAttributes(CreateServerAttributesUtility attrs) {
		attrs.addAttribute(IEapXpServerAttributes.PROJECT_HOME, 
				ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FOLDER,
				"A filesystem path pointing to a standalone web application project folder.", null);
		attrs.addAttribute(IEapXpServerAttributes.MAVEN_BIN, 
				ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FILE,
				"A filesystem path pointing to a maven command.", null);
		attrs.addAttribute(IEapXpServerAttributes.VM_INSTALL_PATH, 
				ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FOLDER,
				"A filesystem path pointing to a JVM to use when running.", null);
	}

	@Override
	public Attributes getOptionalAttributes() {
		if (optional == null) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			fillOptionalAttributes(attrs);
			this.optional = attrs.toPojo();
		}
		return optional;
	}

	protected void fillOptionalAttributes(CreateServerAttributesUtility attrs) {
	}

	@Override
	public ServerLaunchMode[] getLaunchModes() {
		return new ServerLaunchMode[] {
				new ServerLaunchMode(ILaunchModes.RUN, ILaunchModes.RUN_DESC),
				new ServerLaunchMode(ILaunchModes.DEBUG, ILaunchModes.DEBUG_DESC)
		};
	}

}
