/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.generic.servertype.variables.ServerStringVariableManager.IExternalVariableResolver;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

/**
 * This class currently only exposes java.home as a resolvable / dynamic attribute.
 * However, extenders can replace or subclass this file and expose variables representing
 * properties file values, or other dynamically-discovered values.
 * 
 */
public class DefaultExternalVariableResolver implements IExternalVariableResolver {

	private GenericServerBehavior genericServerBehavior;

	public DefaultExternalVariableResolver(GenericServerBehavior genericServerBehavior) {
		this.genericServerBehavior = genericServerBehavior;
	}

	@Override
	public String getNonServerKeyValue(String key) {
		if( "java.home".equals(key)) {
			IVMInstall vmi = getVMInstall(getGenericServerBehavior());
			if( vmi != null ) {
				return vmi.getInstallLocation().getAbsolutePath();
			}
		}
		return null;
	}

	protected IVMInstall getVMInstall(IServerDelegate delegate) {
		IVMInstallRegistry reg = getDefaultRegistry();
		if( reg != null )
			return reg.getDefaultVMInstall();
		return null;
	}

	protected IVMInstallRegistry getDefaultRegistry() {
		IVMInstallRegistry registry = null;
		if (LauncherSingleton.getDefault() != null
				&& LauncherSingleton.getDefault().getLauncher() != null
				&& LauncherSingleton.getDefault().getLauncher().getModel() != null) {
					registry = LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel();
		}
		return registry;
	}

	public GenericServerBehavior getGenericServerBehavior() {
		return genericServerBehavior;
	}

}
