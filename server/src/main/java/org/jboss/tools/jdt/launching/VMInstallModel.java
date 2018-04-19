package org.jboss.tools.jdt.launching;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.PropertyChangeEvent;

public class VMInstallModel {
	public static VMInstallModel model = new VMInstallModel();
	public static final VMInstallModel getDefault() {
		return model;
	}
	
	private HashMap<String, IVMInstall> map;
	private List<IVMInstallChangedListener> list;
	public VMInstallModel() {
		map = new HashMap<String, IVMInstall>();
		list = new ArrayList<IVMInstallChangedListener>();
	}
	
	public void addVMInstall(IVMInstall vm) throws IllegalArgumentException {
		String id = vm.getId();
		IVMInstall test = map.get(id);
		if( test != null ) {
			throw new IllegalArgumentException();
		}
		map.put(id, vm);
		fireVMAdded(vm);
	}

	public IVMInstall[] getVMs() {
		ArrayList<IVMInstall> vms = new ArrayList<IVMInstall>(map.values());
		vms.sort(new Comparator<IVMInstall>() {
			@Override
			public int compare(IVMInstall o1, IVMInstall o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		return (IVMInstall[]) vms.toArray(new IVMInstall[vms.size()]);
	}
	
	public void removeVMInstall(IVMInstall vm) {
		removeVMInstall(vm.getId());
	}
	public void removeVMInstall(String vmId) {
		IVMInstall vm = map.get(vmId);
		if( vm != null ) {
			map.put(vmId, null);
			fireVMRemoved(vm);
		}
	}
	
	public void addListener(IVMInstallChangedListener l) {
		list.add(l);
	}

	public void removeListener(IVMInstallChangedListener l) {
		list.remove(l);
	}

	
	private void fireVMRemoved(IVMInstall vm) {
		for(IVMInstallChangedListener l : list) {
			l.vmRemoved(vm);
		}
	}

	private void fireVMAdded(IVMInstall vm) {
		for(IVMInstallChangedListener l : list) {
			l.vmAdded(vm);
		}
	}

	public void fireVMChanged(PropertyChangeEvent event) {
		for(IVMInstallChangedListener l : list) {
			l.vmChanged(event);
		}
		
	}
	
}
