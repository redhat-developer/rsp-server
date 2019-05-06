/******************************************************************************* 
 * Copyright (c) 2012-2019 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util;

import java.io.File;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.launching.utils.OSUtils;

public class JavaUtils {

	private static final int[] NO_MAJOR_MINOR_VERSION = new int[] { -1, -1 };

	private JavaUtils() {
		// prevent instantiation
	}
	
	public static boolean supportsServerMode(IVMInstall install) {
		String version = install.getJavaVersion();

		// Maintain legacy behaviour for all older server adapters
		if( version == null || OSUtils.isMac())
			return true;
		
		File jdkLibFolder = null;
		File jreLibFolder = null;
		if( OSUtils.isWindows()) {
			jdkLibFolder = getWindowsServerLibFolder(install, true);
			jreLibFolder = getWindowsServerLibFolder(install, false);
		} else {
			jdkLibFolder = getLinuxServerLibFolder(install, true);
			jreLibFolder = getLinuxServerLibFolder(install, false);
		}
		if( jdkLibFolder != null && jdkLibFolder.exists() && 
				jdkLibFolder.isDirectory() && jdkLibFolder.list().length > 0)
			return true;

		return jreLibFolder != null && jreLibFolder.exists() && 
				jreLibFolder.isDirectory() && jreLibFolder.list().length > 0;
	}

	private static File getLinuxServerLibFolder(IVMInstall install, boolean jdk) {
		File serverFolder = null;
		IPath locPath = new Path(install.getInstallLocation().getAbsolutePath());
		if( jdk )
			locPath = locPath.append("jre");//$NON-NLS-1$
		serverFolder = findServerFolder(locPath.append("lib")); //$NON-NLS-1$
		return serverFolder;
	}
	
	private static File findServerFolder(IPath parent) {
		File f = parent.toFile();
		if( !f.exists())
			return null;
		File[] children = f.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i].isDirectory() ) {
				String[] second = children[i].list();
				for( int j = 0; j < second.length; j++ ) {
					if( second[j].equalsIgnoreCase("server")) //$NON-NLS-1$
							return new File(children[i], second[j]);
				}
			}
		}
		return null;
	}
	
	/**
	 * This method performs a simple jdk check based on file structure ONLY
	 * @return
	 */
	public static boolean isJDK(IVMInstall install) {
		IPath locPath = new Path(install.getInstallLocation().getAbsolutePath());
		// Find a javac at pre-determined locations
		if( locPath.append("bin").append("javac").toFile().exists())
			return true;
		if( locPath.append("bin").append("javac.exe").toFile().exists())
			return true;
		
		// Oracle-style folder structure
		return locPath.append("bin").toFile().exists() 
				&& locPath.append("jre").append("bin").toFile().exists();
	}

	private static File getWindowsServerLibFolder(IVMInstall install, boolean jdk) {
		IPath locPath = new Path(install.getInstallLocation().getAbsolutePath());
		if( jdk )
			locPath = locPath.append("jre");//$NON-NLS-1$
		return locPath.append("bin").append("server").toFile(); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	/**
	 * Returns the major and minor version of the given vm install.
	 * 
	 * @param install
	 * @return the major and minor versions
	 */
	public static int[] getMajorMinorVersion(IVMInstall install) {
		if (install == null) {
			return NO_MAJOR_MINOR_VERSION;
		}
		return getMajorMinorVersion(install.getJavaVersion());
	}

	/**
	 * Returns the numeric major and minor version of the given version string. 
	 * 
	 * Accept a non-null string as per the following document:
	 * https://blogs.oracle.com/java-platform-group/a-new-jdk-9-version-string-
	 * scheme
	 * 
	 * Strings may or may not have all segments, however, the returned array will be
	 * of length 2.
	 * 
	 * @param version
	 * 
	 * @return a integer array of length 2 representing the major+minor version of
	 * the string
	 */
	public static int[] getMajorMinorVersion(String version) {
		if (version == null)
			return NO_MAJOR_MINOR_VERSION;

		int pos = version.indexOf('.');
		if (pos == -1) {
			return new int[] { Integer.parseInt(version), 0 };
		}
		String[] split = version.split("\\.");
		if (split != null && split.length > 1) {
			try {
				return new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]) };
			} catch (NumberFormatException nfe) {
				// intentionally swallow
			}
		}
		return NO_MAJOR_MINOR_VERSION ;
	}

}
