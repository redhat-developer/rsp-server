/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.internal.launching.java;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.jdt.internal.launching.LibraryInfo;

public class LibraryInfoCache {

	/**
	 * Mapping of top-level VM installation directories to library info for that
	 * VM.
	 */
	private Map<String, LibraryInfo> fgLibraryInfoMap = new HashMap<>(10);

	/**
	 * Mapping of the last time the directory of a given SDK was modified.
	 * <br><br>
	 * Mapping: <code>Map&lt;String,Long&gt;</code>
	 * @since 3.7
	 */
	private Map<String, Long> fgInstallTimeMap = new HashMap<>();
	/**
	 * List of install locations that have been detected to have changed
	 *
	 * @since 3.7
	 */
	private HashSet<String> fgHasChanged = new HashSet<>();
	/**
	 * Mutex for checking the time stamp of an install location
	 *
	 * @since 3.7
	 */
	private Object installLock = new Object();	

	private static LibraryInfoCache instance = new LibraryInfoCache();

	public static LibraryInfoCache getDefault() {
		return instance;
	}
	
	private LibraryInfoCache() {
	}

	/**
	 * Returns the library info that corresponds to the specified JRE install
	 * path, or <code>null</code> if none.
	 *
	 * @param javaInstallPath the absolute path to the java executable
	 * @return the library info that corresponds to the specified JRE install
	 * path, or <code>null</code> if none
	 */
	public LibraryInfo get(String javaInstallPath) {
		return fgLibraryInfoMap.get(javaInstallPath);
	}

	/**
	 * Checks to see if the time stamp of the file describe by the given location string
	 * has been modified since the last recorded time stamp. If there is no last recorded
	 * time stamp we assume it has changed. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information
	 *
	 * @param location the location of the SDK we want to check the time stamp for
	 * @return <code>true</code> if the time stamp has changed compared to the cached one or if there is
	 * no recorded time stamp, <code>false</code> otherwise.
	 *
	 * @since 3.7
	 */
	public boolean isTimeStampChanged(String location) {
		synchronized (installLock) {
			if (fgHasChanged.contains(location)) {
				return true;
			}
			File file = new File(location);
			if (file.exists()) {
				Long stamp = fgInstallTimeMap.get(location);
				long fstamp = file.lastModified();
				if (stamp != null 
						&& stamp.longValue() == fstamp) {
					return false;
				}
				//if there is no recorded stamp we have to assume it is new
				stamp = Long.valueOf(fstamp);
				fgInstallTimeMap.put(location, stamp);
				fgHasChanged.add(location);
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the library info that corresponds to the specified JRE install
	 * path.
	 *
	 * @param javaInstallPath home location for a JRE
	 * @param info the library information, or <code>null</code> to remove
	 */
	public void put(String javaInstallPath, LibraryInfo info) {
		if (info == null) {
			fgLibraryInfoMap.remove(javaInstallPath);
			fgInstallTimeMap.remove(javaInstallPath);
		} else {
			fgLibraryInfoMap.put(javaInstallPath, info);
		}
		//once the library info has been set we can forget it has changed
		fgHasChanged.remove(javaInstallPath);
	}
}
