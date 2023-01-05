/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.discovery;

import java.io.File;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.eclipse.jdt.launching.StandardVMType;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerAttributes;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class GenericVMRegistryDiscovery {
	
	public IVMInstall findVMInstall(IServerDelegate delegate) {
		String vmPath = getVMPath(delegate);
		if( vmPath != null && vmPath.isEmpty()) {
			vmPath = null;
		}
		return findVMInstall(vmPath, delegate);
	}

	private String getVMPath(IServerDelegate delegate) {
		if (delegate == null
				|| delegate.getServer() == null) {
			return null;
		}
		return delegate.getServer().getAttribute(GenericServerAttributes.VM_INSTALL_PATH, (String)null);
	}
	public IVMInstall findVMInstall(String vmPath) {
		return findVMInstall(vmPath, null);
	}
	
	public IVMInstall findVMInstall(String vmPath, IServerDelegate delegate) {
		IVMInstallRegistry reg = findDefaultRegistry(delegate);
		if( reg == null )
			return null;
		
		IVMInstall vmi = null;
		if( vmPath == null ) {
			vmi = reg.getDefaultVMInstall();
		} else {
			if (ensureVMInstallAdded(vmPath, reg)) {
				vmi = reg.findVMInstall(new File(vmPath));
			}
		}
		return vmi;
	}
	
	private IVMInstallRegistry findDefaultRegistry(IServerDelegate delegate) {
		if( delegate != null && delegate.getServer() != null 
				&& delegate.getServer().getServerManagementModel() != null) {
			return delegate.getServer().getServerManagementModel().getVMInstallModel();
		}
		return getDefaultRegistry();
	}
	
	public IVMInstallRegistry getDefaultRegistry() {
		IVMInstallRegistry registry = null;
		if (LauncherSingleton.getDefault() != null
				&& LauncherSingleton.getDefault().getLauncher() != null
				&& LauncherSingleton.getDefault().getLauncher().getModel() != null) {
					registry = LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel();
		}
		return registry;
	}
	
	public boolean ensureVMInstallAdded(String vmPath, IVMInstallRegistry reg) {
		File fVMI = vmPath == null ? null : new File(vmPath);
		if( fVMI == null ) {
			IVMInstall vmi = reg.getDefaultVMInstall();
			return vmi != null;
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
