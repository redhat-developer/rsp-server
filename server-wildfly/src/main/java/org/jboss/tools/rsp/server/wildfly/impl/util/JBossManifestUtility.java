/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.impl.util;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jboss.tools.rsp.server.wildfly.beans.impl.ManifestUtility;

public class JBossManifestUtility extends ManifestUtility {
	/*
	 * Specific to as/wfly
	 */
	public static File[] getFilesForModule(File modulesFolder, String moduleName, String slot, FileFilter filter) {
		String slashed = moduleName.replaceAll("\\.", "/");
		slot = (slot == null ? "main" : slot);
		return getFiles(modulesFolder, Paths.get(slashed, slot).toString(), filter);
	}
	public static File[] getFiles(File modulesFolder, String moduleRelativePath, FileFilter filter) {
		File[] layeredPaths = LayeredModulePathFactory.resolveLayeredModulePath(modulesFolder);
		for( int i = 0; i < layeredPaths.length; i++ ) {
			File lay = new File(layeredPaths[i].getAbsolutePath());
			File layeredPath = new File(lay,moduleRelativePath);
			if( layeredPath.exists()) {
				return getFilesFrom(layeredPath, filter);
			}
		}
		return new File[0];
	}

	
	public static File[] getFilesFrom(File layeredPath, FileFilter filter) {
		ArrayList<File> list = new ArrayList<File>();
		File[] children = layeredPath.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			if( filter.accept(children[i])) {
				list.add(new File(children[i].getAbsolutePath()));
			}
		}
		return (File[]) list.toArray(new File[list.size()]);
	}
	


	public static boolean scanManifestPropFromJBossModulesFolder(File[] moduleRoots, String moduleId, String slot, String property, String propPrefix) {
		String value = getManifestPropFromJBossModulesFolder(moduleRoots, moduleId, slot, property);
		if( value != null && value.trim().startsWith(propPrefix))
			return true;
		return false;
	}
	
	public static String getManifestPropFromJBossModulesFolder(File[] moduleRoots, String moduleId, String slot, String property) {
		File[] layeredRoots = LayeredModulePathFactory.resolveLayeredModulePath(moduleRoots);
		for( int i = 0; i < layeredRoots.length; i++ ) {
			File[] manifests = getFilesForModule(layeredRoots[i], moduleId, slot, manifestFilter());
			if( manifests.length > 0 ) {
				String value = getManifestProperty(manifests[0], property);
				if( value != null )
					return value;
				return null;
			}
		}
		return null;
	}
	
	public static boolean scanManifestPropFromJBossModules(File[] moduleRoots, String moduleId, String slot, String property, String propPrefix) {
		String value = getManifestPropFromJBossModules(moduleRoots, moduleId, slot, property);
		if( value != null && value.trim().startsWith(propPrefix))
			return true;
		return false;
	}
	
	public static String getManifestPropFromJBossModules(File[] moduleRoots, String moduleId, String slot, String property) {
		File[] layeredRoots = LayeredModulePathFactory.resolveLayeredModulePath(moduleRoots);
		for( int i = 0; i < layeredRoots.length; i++ ) {
			File[] jars = getFilesForModule(layeredRoots[i], moduleId, slot, jarFilter());
			if( jars.length > 0 ) {
				String value = getJarProperty(jars[0], property);
				return value;
			}
		}
		return null;
	}

	/**
	 * This method is an older implementation on how to discover 
	 * the version of your server type. 
	 * 
	 * Only legacy code should call this. All new clients 
	 * should properly implement their own method. The method
	 * is still public for legacy and backwards compatibility reasons.
	 * 
	 * @param systemJarFile
	 * @return
	 */
	public static String getFullServerVersionFromZip(File systemJarFile) {
		return getFullServerVersionFromZipLegacy(systemJarFile, new String[]{
				"Bundle-Version", "Specification-Version", "Implementation-Version"});
	}
	
}
