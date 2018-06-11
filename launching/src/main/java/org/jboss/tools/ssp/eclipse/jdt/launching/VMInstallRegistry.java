/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.eclipse.jdt.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.ssp.internal.launching.LaunchingCore;

public class VMInstallRegistry implements IVMInstallRegistry {

	private static final String JAVA_HOME = "JAVA_HOME";
	private static final String RUNNING_VM_ID = "running";

	private final Map<String, IVMInstall> vms;
	private final List<IVMInstallChangedListener> listeners;

	public VMInstallRegistry() {
		this.vms = new HashMap<>();
		this.listeners = new ArrayList<>();
	}

	public void addActiveVM() {
		try {
			Map<String,String> env = System.getenv();
			String home = env.get(JAVA_HOME);
			if (home == null) {
				throw new IllegalArgumentException("JAVA_HOME environment variable is not set");
			}
			File f = new File(home);
			if (f.exists()) {
				IVMInstall vmi = StandardVMType.getDefault().createVMInstall(RUNNING_VM_ID);
				vmi.setInstallLocation(f);
				addVMInstall(vmi);
			}
		} catch(IllegalArgumentException e) {
			LaunchingCore.log(e);
		}
	}

	public void addVMInstall(IVMInstall vm) throws IllegalArgumentException {
		if (vm == null) {
			throw new IllegalArgumentException();
		}
		String id = vm.getId();
		IVMInstall test = vms.get(id);
		if (test != null) {
			throw new IllegalArgumentException();
		}
		if( vm instanceof AbstractVMInstall ) {
			((AbstractVMInstall)vm).setRegistry(this);
		}
		vms.put(id, vm);
		fireVMAdded(vm);
	}

	public IVMInstall[] getVMs() {
		return vms.values().stream()
			.sorted((o1, o2) -> o1.getId().compareTo(o2.getId()))
			.toArray(IVMInstall[]::new);
	}

	public IVMInstall findVMInstall(String id) {
		return vms.values().stream()
			.filter(vm -> vm.getId().equals(id))
			.findAny()
			.orElse(null);
	}

	public IVMInstall findVMInstall(File installLocation) {
		return vms.values().stream()
			.filter(vm -> vm.getInstallLocation().equals(installLocation))
			.findAny()
			.orElse(null);
	}

	public void removeVMInstall(IVMInstall vm) {
		removeVMInstall(vm.getId());
	}

	public void removeVMInstall(String vmId) {
		IVMInstall vm = vms.get(vmId);
		if (vm != null) {
			vms.remove(vmId);
			fireVMRemoved(vm);
		}
	}

	public void addListener(IVMInstallChangedListener l) {
		listeners.add(l);
	}

	public void removeListener(IVMInstallChangedListener l) {
		listeners.remove(l);
	}

	public void fireVMChanged(PropertyChangeEvent event) {
		listeners.forEach(listener -> listener.vmChanged(event));
	}

	public IVMInstall getDefaultVMInstall() {
		return findVMInstall(RUNNING_VM_ID);
	}

	private void fireVMRemoved(IVMInstall vm) {
		listeners.forEach(listener -> listener.vmRemoved(vm));
	}

	private void fireVMAdded(IVMInstall vm) {
		listeners.forEach(listener -> listener.vmAdded(vm));
	}

}
