/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model;

import java.util.Map;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;

public interface ICapabilityManagement {
	public void clientAdded(RSPClient client);
	public void clientRemoved(RSPClient client);
	public String getCapabilityProperty(RSPClient c, String key);
	public IStatus registerClientCapabilities(RSPClient c, ClientCapabilitiesRequest response);
	public Map<String,String> getServerCapabilities();
}
