/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathModel;
import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeManager;

public interface IServerManagementModel {
	public IServerBeanTypeManager getServerBeanTypeManager();
	public IServerModel getServerModel();
	public IDiscoveryPathModel getDiscoveryPathModel();
	public IVMInstallRegistry getVMInstallModel();
}
