/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.beans.impl;

import java.io.File;
import java.util.Properties;

import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.launching.utils.FileUtil;

public abstract class ServerBeanTypeEnterprise extends JBossServerBeanType {

	protected ServerBeanTypeEnterprise(String id, String name, String jbossSystemJarPath) {
		super(id, name, jbossSystemJarPath);
	}
	protected String getServerTypeBaseName() {
		return "Red Hat JBoss " + getId();
	}
	

	/**
	 * Get the eap6-style version string, or null if not found.
	 * This method will check for a product.conf, a corresponding 'slot', 
	 * and a proper manifest.mf file to read in that product slot. 
	 * 
	 * @param location
	 * @param versionPrefix
	 * @return
	 */
	public String getEAP6xVersion(File location,  String metaInfPath,
			String versionPrefix, String slot, String releaseName) {
		IPath productConf = new Path(location.getAbsolutePath()).append(BIN).append(PRODUCT_CONF);
		if( productConf.toFile().exists()) {
			Properties p = FileUtil.loadProperties(productConf.toFile());
			String product = (String) p.get(PRODUCT_CONF_SLOT); //$NON-NLS-1$
			if(slot.equals(product)) { //$NON-NLS-1$
				return getEAP6xVersionNoSlotCheck(location, metaInfPath, versionPrefix, releaseName);
			}
		}
		return null;
	}
	public String getEAP6xVersionNoSlotCheck(File location,  String metaInfPath,
			String versionPrefix, String releaseName) {
		IPath rootPath = new Path(location.getAbsolutePath());
		IPath eapDir = rootPath.append(metaInfPath);
		if( eapDir.toFile().exists()) {
			IPath manifest = eapDir.append(MANIFEST_MF); //$NON-NLS-1$
			Properties p2 = FileUtil.loadProperties(manifest.toFile());
			String type = p2.getProperty(MANIFEST_PROD_RELEASE_NAME); //$NON-NLS-1$
			String version = p2.getProperty(MANIFEST_PROD_RELEASE_VERS); //$NON-NLS-1$
			boolean matchesName = releaseName == null || releaseName.equals(type);
			boolean matchesVersion = versionPrefix == null || version.startsWith(versionPrefix);
			if( matchesName && matchesVersion )
				return version;
		}
		return null;
	}
	
	public String getServerAdapterTypeEAPLegacy(String version) {
		// TODO this needs to be split up, does not belong here
		if( "4.2".equals(version)) return IServerConstants.SERVER_EAP_43;
		if( "4.3".equals(version)) return IServerConstants.SERVER_EAP_43;
		if( "5.0".equals(version)) return IServerConstants.SERVER_EAP_50;
		if( "5.1".equals(version)) return IServerConstants.SERVER_EAP_50;
		if( "5.2".equals(version)) return IServerConstants.SERVER_EAP_50;
		if( "5.3".equals(version)) return IServerConstants.SERVER_EAP_50;
		// All others should be declared in their proper subclass to ensure that
		// non-exact matches still get a non-null result.
		return null;
	}
	
	@Override
	public ServerBean createServerBean(File rootLocation) {
		String version = getFullVersion(rootLocation);
		String relative = getRootToAdapterRelativePath(rootLocation, version);
		File root = null;
		if( relative == null ) {
			root = rootLocation;
		} else {
			IPath p = new Path(rootLocation.getAbsolutePath()).append(relative);
			root = p.toFile();
		}
		ServerBean server = new ServerBean(
				root.getAbsolutePath(), getServerBeanName(rootLocation),
				getId(), getUnderlyingTypeId(rootLocation), version, 
				getMajorMinorVersion(version), getServerAdapterTypeId(version));
		return server;
	}

}
