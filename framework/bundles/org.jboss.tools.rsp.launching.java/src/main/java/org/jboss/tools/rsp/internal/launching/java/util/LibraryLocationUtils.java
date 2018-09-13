/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.internal.launching.java.util;

import java.io.File;
import java.net.URL;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.LibraryLocation;

public class LibraryLocationUtils {

	private LibraryLocationUtils() {
	}

	/**
	 * Evaluates library locations for a IVMInstall. If no library locations are set on the install, a default
	 * location is evaluated and checked if it exists.
	 * @param vm the {@link IVMInstall} to compute locations for
	 * @return library locations with paths that exist or are empty
	 * @since 2.0
	 */
	public static LibraryLocation[] getLibraryLocations(IVMInstall vm)  {
		IPath[] libraryPaths;
		IPath[] sourcePaths;
		IPath[] sourceRootPaths;
		IPath[] annotationPaths;
		URL[] javadocLocations;
		URL[] indexes;
		LibraryLocation[] locations= vm.getLibraryLocations();
		if (locations == null) {
            URL defJavaDocLocation = vm.getJavadocLocation();
			File installLocation = vm.getInstallLocation();
			if (installLocation == null) {
				return new LibraryLocation[0];
			}
			LibraryLocation[] dflts= vm.getVMInstallType().getDefaultLibraryLocations(installLocation);
			libraryPaths = new IPath[dflts.length];
			sourcePaths = new IPath[dflts.length];
			sourceRootPaths = new IPath[dflts.length];
			javadocLocations= new URL[dflts.length];
			indexes = new URL[dflts.length];
			annotationPaths = new IPath[dflts.length];
			for (int i = 0; i < dflts.length; i++) {
				libraryPaths[i]= dflts[i].getSystemLibraryPath();
                if (defJavaDocLocation == null) {
                    javadocLocations[i]= dflts[i].getJavadocLocation();
                } else {
                    javadocLocations[i]= defJavaDocLocation;
                }
                indexes[i] = dflts[i].getIndexLocation();
				if (!libraryPaths[i].toFile().isFile()) {
					libraryPaths[i]= Path.EMPTY;
				}

				annotationPaths[i] = Path.EMPTY;

				sourcePaths[i]= dflts[i].getSystemLibrarySourcePath();
				if (sourcePaths[i].toFile().isFile()) {
					sourceRootPaths[i]= dflts[i].getPackageRootPath();
				} else {
					sourcePaths[i]= Path.EMPTY;
					sourceRootPaths[i]= Path.EMPTY;
				}
			}
		} else {
			libraryPaths = new IPath[locations.length];
			sourcePaths = new IPath[locations.length];
			sourceRootPaths = new IPath[locations.length];
			javadocLocations= new URL[locations.length];
			indexes = new URL[locations.length];
			annotationPaths = new IPath[locations.length];
			for (int i = 0; i < locations.length; i++) {
				libraryPaths[i]= locations[i].getSystemLibraryPath();
				sourcePaths[i]= locations[i].getSystemLibrarySourcePath();
				sourceRootPaths[i]= locations[i].getPackageRootPath();
				javadocLocations[i]= locations[i].getJavadocLocation();
				annotationPaths[i] = locations[i].getExternalAnnotationsPath();
				indexes[i] = locations[i].getIndexLocation();
			}
		}
		locations = new LibraryLocation[sourcePaths.length];
		for (int i = 0; i < sourcePaths.length; i++) {
			locations[i] = new LibraryLocation(libraryPaths[i], sourcePaths[i], sourceRootPaths[i], javadocLocations[i], indexes[i], annotationPaths[i]);
		}
		return locations;
	}

}
