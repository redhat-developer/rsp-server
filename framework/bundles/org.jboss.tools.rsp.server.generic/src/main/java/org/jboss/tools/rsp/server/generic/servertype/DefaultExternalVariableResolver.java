/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype;

import java.util.Map;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.generic.discovery.GenericVMRegistryDiscovery;
import org.jboss.tools.rsp.server.generic.servertype.variables.ServerStringVariableManager.IExternalVariableResolver;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

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
			IVMInstall vm1 = new GenericVMRegistryDiscovery().findVMInstall(getGenericServerBehavior());
			return vm1 == null ? null : vm1.getInstallLocation().getAbsolutePath();
		}
		IServerType st = this.genericServerBehavior.getServer().getServerType();
		if( st != null && st instanceof GenericServerType) {
			GenericServerType st1 = (GenericServerType)st;
			Map<String, Object> map = st1.getDefaults();
			if( map.containsKey(key)) {
				Object v = map.get(key);
				return v == null ? null : v.toString();
			}
		}
		return null;
	}

	protected IVMInstall getVMInstall(IServerDelegate delegate) {
		return new GenericVMRegistryDiscovery().findVMInstall(delegate);
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
