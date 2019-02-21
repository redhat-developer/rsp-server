/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.launching.java.ILaunchModes;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public abstract class BaseMinishiftServerType implements IServerType {
	protected Attributes required = null;
	protected Attributes optional = null;

	private String id;
	private String name;
	private String desc;

	public BaseMinishiftServerType(String id, String name, String desc) {
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
	public Attributes getRequiredAttributes() {
		if (required == null) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			
			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A filesystem path pointing to a minishift binary file.", null);
			this.required = attrs.toPojo();
		}
		return required;
	}
	
	protected boolean isCDK() {
		return false;
	}

	@Override
	public Attributes getOptionalAttributes() {
		if (optional == null) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();

			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_VM_DRIVER, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"The driver to use for the Minishift VM. Possible values: [virtualbox vmwarefusion kvm xhyve hyperv] (default \"kvm\")", null);
			
			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_PROFILE, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A minishift profile. Default value is 'minishift'", "minishift");

			attrs.addAttribute(IMinishiftServerAttributes.MINISHIFT_HOME, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"A attribute to set the MINISHIFT_HOME environment variable when interacting with the server. The MINISHIFT_HOME environment variable allows you to choose a different home directory for Minishift", null);

			this.optional = attrs.toPojo();
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
		return new ServerLaunchMode[] { new ServerLaunchMode(ILaunchModes.RUN, ILaunchModes.RUN_DESC), };
	}

}
