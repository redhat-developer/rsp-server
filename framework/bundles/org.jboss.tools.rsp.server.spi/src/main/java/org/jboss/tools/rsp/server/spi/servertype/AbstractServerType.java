/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.servertype;

import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;

public abstract class AbstractServerType implements IServerType {
	private String id;
	private String name;
	private String desc;
	public AbstractServerType(String id, String name, String desc) {
		this.id = id;
		this.name = name;
		this.desc = desc;
	}

	public abstract IServerDelegate createServerDelegate(IServer server);
	
	public Attributes getRequiredAttributes() {
		return new CreateServerAttributesUtility().toPojo();
	}
	public Attributes getOptionalAttributes() {
		return new CreateServerAttributesUtility().toPojo();
	}
	public Attributes getRequiredLaunchAttributes() {
		return new CreateServerAttributesUtility().toPojo();
	}
	public Attributes getOptionalLaunchAttributes() {
		return new CreateServerAttributesUtility().toPojo();
	}
	
	public ServerLaunchMode[] getLaunchModes() {
		return new ServerLaunchMode[] {};
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return desc;
	}
}
