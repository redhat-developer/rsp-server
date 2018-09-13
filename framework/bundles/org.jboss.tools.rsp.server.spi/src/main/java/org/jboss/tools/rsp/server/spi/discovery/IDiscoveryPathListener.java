/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.discovery;

import org.jboss.tools.rsp.api.dao.DiscoveryPath;

public interface IDiscoveryPathListener {
	public void discoveryPathAdded(DiscoveryPath path);
	
	public void discoveryPathRemoved(DiscoveryPath path);
	
}
