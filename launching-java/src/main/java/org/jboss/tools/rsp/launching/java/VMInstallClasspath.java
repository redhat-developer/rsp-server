/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching.java;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.jdt.internal.launching.LibraryInfo;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.LibraryLocation;
import org.jboss.tools.rsp.internal.launching.java.LibraryInfoCache;

public class VMInstallClasspath {

	private VMInstallClasspath() {
	}

	/**
	 * Returns the classpath for the given virtual machine installation. Only
	 * bootstrap- and endorsed classpath entries are returned, leaving "ext"
	 * directories alone.
	 * 
	 * @param vm
	 *            the IVMInstall to retrieve the class path from
	 * @return
	 */
	public static String[] get(IVMInstall vm) {
		if (vm == null) {
			return new String[] {};
		}
		File vmInstallLocation = vm.getInstallLocation();
		if (vmInstallLocation == null) {
			return new String[] {};
		}
		LibraryInfo libraryInfo = LibraryInfoCache.getDefault().get(vmInstallLocation.getAbsolutePath());
		if (libraryInfo == null) {
			return new String[] {};
		}

		Set<String> extensionDirs = toSet(libraryInfo.getExtensionDirs());
		LibraryLocation[] libs = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
		Collection<String> classpath = getNonExtDirEntries(extensionDirs, libs);
		return classpath.toArray(new String[classpath.size()]);
	}

	/**
	 * Returns endorsed and bootstrap classpath entries. Libraries is in the "ext"
	 * directories are not loaded by the boot class loader.
	 * 
	 * @param extensionDirs
	 * @param libs
	 * @return
	 */
	private static List<String> getNonExtDirEntries(Set<String> extensionDirs, LibraryLocation[] libs) {
		List<String> nonExtDirEntries = new ArrayList<>();
		for (int i = 0; i < libs.length; i++) {
			LibraryLocation location = libs[i];
			IPath libraryPath = location.getSystemLibraryPath();
			String dir = libraryPath.toFile().getParent();
			// exclude extension directory entries
			if (!extensionDirs.contains(dir)) {
				nonExtDirEntries.add(libraryPath.toOSString());
			}
		}
		return nonExtDirEntries;
	}

	private static Set<String> toSet(String[] extensionDirs) {
		Set<String> extensionDirsSet = new HashSet<>();
		for (int i = 0; i < extensionDirs.length; i++) {
			extensionDirsSet.add(extensionDirs[i]);
		}
		return extensionDirsSet;
	}
}
