/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.server.minishift.servertype.AbstractLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IShutdownLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class MinishiftStopLauncher extends AbstractLauncher implements IShutdownLauncher{
	public MinishiftStopLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}

	public ILaunch launch(boolean force) throws CoreException {
		String mode = "run";
		return launch(mode);
	}
	public String getProgramArguments() {
		return "stop";
	}
}
