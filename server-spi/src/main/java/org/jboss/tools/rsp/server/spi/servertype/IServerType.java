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

public interface IServerType {
	public String getId();
	public String getName();
	public String getDescription();
	public IServerDelegate createServerDelegate(IServer server);
	public Attributes getRequiredAttributes();
	public Attributes getOptionalAttributes();
	public Attributes getRequiredLaunchAttributes();
	public Attributes getOptionalLaunchAttributes();
	public ServerLaunchMode[] getLaunchModes();
}
