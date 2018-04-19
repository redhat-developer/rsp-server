/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.tools.jdt.launching.VMInstallModel;

/**
 * An implementation of IVMInstall that is used for manipulating VMs without necessarily
 * committing changes.
 * <p>
 * Instances of this class act like wrappers.  All other instances of IVMInstall represent
 * 'real live' VMs that may be used for building or launching.  Instances of this class
 * behave like 'temporary' VMs that are not visible and not available for building or launching.
 * </p>
 * <p>
 * Instances of this class may be constructed as a preliminary step to creating a 'live' VM
 * or as a preliminary step to making changes to a 'real' VM.
 * </p>
 * When <code>convertToRealVM</code> is called, a corresponding 'real' VM is created
 * if one did not previously exist, or the corresponding 'real' VM is updated.
 * </p>
 * <p>
 * Clients may instantiate this class.
 * </p>
 *
 * @since 2.1
 * @noextend This class is not intended to be sub-classed by clients.
 */
public class VMStandin extends AbstractVMInstall {

    /**
     * <code>java.version</code> system property, or <code>null</code>
     * @since 3.1
     */
    private String fJavaVersion = null;

	/*
	 * @see org.eclipse.jdt.launching.AbstractVMInstall#AbstractVMInstall(org.eclipse.jdt.launching.IVMInstallType, java.lang.String)
	 */
	public VMStandin(IVMInstallType type, String id) {
		super(type, id);
		setNotify(false);
	}

	/**
	 * Constructs a copy of the specified VM with the given identifier.
	 *
	 * @param sourceVM the original VM
	 * @param id the new ID to use
	 * @since 3.2
	 */
	public VMStandin(IVMInstall sourceVM, String id) {
		super(sourceVM.getVMInstallType(), id);
		setNotify(false);
		init(sourceVM);
	}

	/**
	 * Construct a <code>VMStandin</code> instance based on the specified <code>IVMInstall</code>.
	 * Changes to this stand-in will not be reflected in the 'real' VM until <code>convertToRealVM</code>
	 * is called.
	 *
	 * @param realVM the 'real' VM from which to construct this stand-in VM
	 */
	public VMStandin(IVMInstall realVM) {
		this (realVM.getVMInstallType(), realVM.getId());
		init(realVM);
	}

	/**
	 * Initializes the settings of this stand-in based on the settings in the given
	 * VM install.
	 *
	 * @param realVM VM to copy settings from
	 */
	private void init(IVMInstall realVM) {
		setName(realVM.getName());
		setInstallLocation(realVM.getInstallLocation());
		setLibraryLocations(realVM.getLibraryLocations());
		setJavadocLocation(realVM.getJavadocLocation());
		if (realVM instanceof IVMInstall2) {
			IVMInstall2 vm2 = (IVMInstall2) realVM;
			setVMArgs(vm2.getVMArgs());
	        fJavaVersion = vm2.getJavaVersion();
		} else {
			setVMArguments(realVM.getVMArguments());
			fJavaVersion = null;
		}
		if (realVM instanceof AbstractVMInstall) {
			AbstractVMInstall vm2 = (AbstractVMInstall) realVM;
			Map<String, String> attributes = vm2.getAttributes();
			Iterator<Entry<String, String>> iterator = attributes.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> entry = iterator.next();
				setAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * If no corresponding 'real' VM exists, create one and populate it from this stand-in instance.
	 * If a corresponding VM exists, update its attributes from this stand-in instance.
	 *
	 * @return IVMInstall the 'real' corresponding to this stand-in VM
	 */
	@SuppressWarnings("deprecation")
	public IVMInstall convertToRealVM() {
		IVMInstallType vmType= getVMInstallType();
		IVMInstall realVM= vmType.findVMInstall(getId());
		boolean notify = true;

		if (realVM == null) {
			realVM= vmType.createVMInstall(getId());
			notify = false;
		}
		// do not notify of property changes on new VMs
		if (realVM instanceof AbstractVMInstall) {
			 ((AbstractVMInstall)realVM).setNotify(notify);
		}
		realVM.setName(getName());
		realVM.setInstallLocation(getInstallLocation());
		realVM.setLibraryLocations(getLibraryLocations());
		realVM.setJavadocLocation(getJavadocLocation());
		if (realVM instanceof IVMInstall2) {
			IVMInstall2 vm2 = (IVMInstall2) realVM;
			vm2.setVMArgs(getVMArgs());
		} else {
			realVM.setVMArguments(getVMArguments());
		}

		if (realVM instanceof AbstractVMInstall) {
			AbstractVMInstall avm = (AbstractVMInstall) realVM;
			Iterator<Entry<String, String>> iterator = getAttributes().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> entry = iterator.next();
				avm.setAttribute(entry.getKey(), entry.getValue());
			}
			avm.setNotify(true);
		}
		if (!notify) {
			fireVMAdded(realVM);
		}
		return realVM;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jdt.launching.IVMInstall#getJavaVersion()
     */
    @Override
	public String getJavaVersion() {
        return fJavaVersion;
    }
    
    private void fireVMAdded(IVMInstall realVM) {
    	VMInstallModel.getDefault().addVMInstall(realVM);
    }
}
