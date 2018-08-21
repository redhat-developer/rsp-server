/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype;

import java.io.File;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMRunner;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class JBossVMRegistryDiscovery {
	public static IVMInstall findVMInstall(IServerDelegate delegate) {
		String vmPath = delegate.getServer().getAttribute(IJBossServerAttributes.VM_INSTALL_PATH, (String)null);
		IVMInstall vmi = null;
		if( vmPath == null ) {
			vmi = getDefaultRegistry().getDefaultVMInstall();
		} else {
			vmi = getDefaultRegistry().findVMInstall(new File(vmPath));
		}
		return vmi;
	}
	public static IVMRunner getVMRunner(IServerDelegate delegate, String mode) {
		IVMInstall vmi = findVMInstall(delegate);
		if( vmi == null ) {
			return null;
		}
		return vmi.getVMRunner(mode);
	}
	
	public static IVMInstallRegistry getDefaultRegistry() {
		return LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel();
	}
}
