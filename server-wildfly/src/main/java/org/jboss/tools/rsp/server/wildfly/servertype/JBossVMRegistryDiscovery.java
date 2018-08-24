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
import org.jboss.tools.rsp.eclipse.jdt.launching.StandardVMType;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
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
	
	public static boolean ensureVMInstallAdded(IServer server) {
		String vmi = server.getAttribute(IJBossServerAttributes.VM_INSTALL_PATH, (String)null);
		IVMInstallRegistry reg = LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel();
		File fVMI = vmi == null ? null : new File(vmi);
		if( fVMI == null ) {
			return true;
		}
		if( !fVMI.exists() || !fVMI.isDirectory()) {
			// Cannot be a java installation. Bad parameter.
			return false;
		}
		if( reg.findVMInstall(fVMI) == null) {
			// this java installation is not in the model yet
			String name = getNewVmName(fVMI.getName());
			IVMInstall ivmi = StandardVMType.getDefault().createVMInstall(name);
			ivmi.setInstallLocation(fVMI);
			try {
				String vers = ivmi.getJavaVersion();
				if( vers == null ) {
					StandardVMType.getDefault().disposeVMInstall(name);
					return false;
				}
				reg.addVMInstall(ivmi);
			} catch(IllegalArgumentException iae) {
				// TODO
				return false;
			}
		}
		return true;
	}
	
	private static String getNewVmName(String base) {
		IVMInstall vmi = StandardVMType.getDefault().findVMInstall(base);
		if( vmi == null )
			return base;
		String tmpName = null;
		int i = 1;
		while(true) {
			tmpName = base + " (" + i + ")";
			vmi = StandardVMType.getDefault().findVMInstall(tmpName);
			if( vmi == null )
				return tmpName;
			i++;
		}
	}
}
