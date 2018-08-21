/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.launchers;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public interface IStartLauncher {
	public IServer getServer();
	
	public ILaunch launch(String mode) throws CoreException;

	public CommandLineDetails getLaunchedDetails();

	public CommandLineDetails getLaunchCommand(String mode) throws CoreException;

	public ILaunch getLaunch();
}
