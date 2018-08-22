/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.eclipse.jdt.launching;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface IVMInstallRegistry {

	public void addActiveVM();
	
	public void addVMInstall(IVMInstall vm) throws IllegalArgumentException;
	
	public IVMInstall[] getVMs();

	public IVMInstall findVMInstall(String id);

	public IVMInstall findVMInstall(File installLocation);

	public void removeVMInstall(IVMInstall vm);

	public void removeVMInstall(String vmId);

	public void addListener(IVMInstallChangedListener l);

	public void removeListener(IVMInstallChangedListener l);

	public void fireVMChanged(PropertyChangeEvent event);

	public IVMInstall getDefaultVMInstall();
	
	public void load(File f) throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException;
	
	public void save(File f) throws IOException;

}
