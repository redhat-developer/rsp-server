/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.servertype;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.IServerModel;

public interface IServer extends IServerAttributes {
	
	String getId();
	
	String getTypeId();
	
	IServerType getServerType();
	
	IServerDelegate getDelegate();
	
	void save(IProgressMonitor monitor) throws CoreException;
	
	void load(IProgressMonitor monitor) throws CoreException;
	
	void delete() throws CoreException;

	IServerManagementModel getServerManagementModel();

	IServerModel getServerModel();

}
