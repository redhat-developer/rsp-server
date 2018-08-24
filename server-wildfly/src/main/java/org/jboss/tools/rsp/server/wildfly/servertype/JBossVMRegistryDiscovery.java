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
	public IVMInstall findVMInstall(IServerDelegate delegate) {
		String vmPath = delegate.getServer().getAttribute(IJBossServerAttributes.VM_INSTALL_PATH, (String)null);
		return findVMInstall(vmPath);
	}
	public IVMInstall findVMInstall(String vmPath) {
		IVMInstall vmi = null;
		if( vmPath == null ) {
			vmi = getDefaultRegistry().getDefaultVMInstall();
		} else {
			vmi = getDefaultRegistry().findVMInstall(new File(vmPath));
		}
		return vmi;
	}

	public IVMRunner getVMRunner(IServerDelegate delegate, String mode) {
		IVMInstall vmi = findVMInstall(delegate);
		if( vmi == null ) {
			return null;
		}
		return vmi.getVMRunner(mode);
	}
	
	public IVMInstallRegistry getDefaultRegistry() {
		return LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel();
	}
	
	public boolean ensureVMInstallAdded(IServer server) {
		String vmi = server.getAttribute(IJBossServerAttributes.VM_INSTALL_PATH, (String)null);
		IVMInstallRegistry reg = LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel();
		return ensureVMInstallAdded(vmi, reg);
	}
	public boolean ensureVMInstallAdded(String vmPath, IVMInstallRegistry reg) {
		File fVMI = vmPath == null ? null : new File(vmPath);
		if( fVMI == null ) {
			IVMInstall vmi = reg.getDefaultVMInstall();
			if( vmi == null )
				return false;
			return true;
		}
		
		if( !fVMI.exists() || !fVMI.isDirectory()) {
			// Cannot be a java installation. Bad parameter.
			return false;
		}
		if( reg.findVMInstall(fVMI) == null) {
			// this java installation is not in the model yet
			String name = getNewVmName(fVMI.getName(), reg);
			try {
				IVMInstall ivmi = StandardVMType.getDefault().createVMInstall(name);
				ivmi.setInstallLocation(fVMI);
				String vers = ivmi.getJavaVersion();
				if( vers == null ) {
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
	
	protected String getNewVmName(String base, IVMInstallRegistry reg) {
		IVMInstall vmi = reg.findVMInstall(base);
		if( vmi == null )
			return base;
		String tmpName = null;
		int i = 1;
		while(true) {
			tmpName = base + " (" + i + ")";
			vmi = reg.findVMInstall(tmpName);
			if( vmi == null )
				return tmpName;
			i++;
		}
	}
}
