/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.eclipse.jdt.launching;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.launching.memento.XMLMemento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

public class VMInstallRegistry implements IVMInstallRegistry {
	private static final Logger LOG = LoggerFactory.getLogger(VMInstallRegistry.class);

	private static final String JAVA_HOME = "java.home";
	private static final String RUNNING_VM_ID = "running";

	private final Map<String, IVMInstall> vms;
	private final List<IVMInstallChangedListener> listeners;

	public VMInstallRegistry() {
		this.vms = new HashMap<>();
		this.listeners = new ArrayList<>();
	}

	public void addActiveVM() {
		try {
			String home = System.getProperty(JAVA_HOME);
			File f = new File(home);
			if (f.exists()) {
				IVMInstall vmi = StandardVMType.getDefault().createVMInstall(RUNNING_VM_ID);
				vmi.setInstallLocation(f);
				addVMInstall(vmi);
			}
		} catch(IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public void addVMInstall(IVMInstall vm) throws IllegalArgumentException {
		if (vm == null) {
			throw new IllegalArgumentException();
		}
		String id = vm.getId();
		if (vms.containsKey(id)) {
			throw new IllegalArgumentException();
		}
		if (vm instanceof AbstractVMInstall) {
			((AbstractVMInstall) vm).setRegistry(this);
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

	public void save(File vmsFile) throws IOException {
		if (!vmsFile.exists()) {
			if( !vmsFile.createNewFile() ) {
				throw new IOException();
			}
		}
		JSONMemento memento = JSONMemento.createWriteRoot();
		for (IVMInstall vmInstall : getVMs()) {
			IMemento vmMemento = memento.createChild("vm");
			vmMemento.putString("id", vmInstall.getId());
			vmMemento.putString("installLocation", vmInstall.getInstallLocation().getAbsolutePath());
			vmMemento.putString("type", vmInstall.getVMInstallType().getClass().getName());			
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		memento.save(out);
		byte[] bytes = out.toByteArray();
		Files.write(vmsFile.toPath(), bytes);
	}
	
	public void load(File vmsFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException {
		if (!vmsFile.exists()) {
			return;
		}
		IMemento vmsMemento; 
		try {
			vmsMemento = JSONMemento.loadMemento(new FileInputStream(vmsFile));
		} catch (JsonSyntaxException se) {
			// most probably that it is still in the previous xml format
			vmsMemento = XMLMemento.loadMemento(new FileInputStream(vmsFile));
		}
		for (IMemento vmMemento : vmsMemento.getChildren()) {
			String id = vmMemento.getString("id");
			if (findVMInstall(id) != null) {
				continue;
			}
			String installLocation = vmMemento.getString("installLocation");
			String type = vmMemento.getString("type");
			
			@SuppressWarnings("unchecked")
			Class<IVMInstallType> typeClass = (Class<IVMInstallType>)Class.forName(type).asSubclass(IVMInstallType.class);
			IVMInstallType vmType = null;

			try {
				vmType = (IVMInstallType)typeClass.getMethod("getDefault").invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				vmType = typeClass.newInstance();
			}
			IVMInstall newVM = vmType.createVMInstall(id);
			newVM.setInstallLocation(new File(installLocation));
			addVMInstall(newVM);
		}
		
	}
	
}
