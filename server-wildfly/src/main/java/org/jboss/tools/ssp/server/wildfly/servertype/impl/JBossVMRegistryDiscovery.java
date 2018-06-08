/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import java.io.File;

import org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMRunner;
import org.jboss.tools.ssp.server.LauncherSingleton;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;

public class JBossVMRegistryDiscovery {
	public static IVMRunner findVMInstall(IServerDelegate delegate, String mode) {
		String vmPath = delegate.getServer().getAttribute(IJBossServerAttributes.VM_INSTALL_PATH, (String)null);
		IVMInstall vmi = null;
		if( vmPath == null ) {
			vmi = getDefaultRegistry().getDefaultVMInstall();
		} else {
			vmi = getDefaultRegistry().findVMInstall(new File(vmPath));
		}
		if( vmi == null ) {
			return null;
		}
		return vmi.getVMRunner(mode);
	}
	
	public static IVMInstallRegistry getDefaultRegistry() {
		return LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel();
	}
}
