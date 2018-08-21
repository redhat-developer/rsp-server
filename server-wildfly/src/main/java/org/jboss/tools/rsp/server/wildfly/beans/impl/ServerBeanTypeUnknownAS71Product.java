/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
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
import java.nio.file.Path;
import java.util.Properties;

import org.jboss.tools.rsp.launching.utils.FileUtil;
import org.jboss.tools.rsp.server.wildfly.impl.util.JBossManifestUtility;

public class ServerBeanTypeUnknownAS71Product extends JBossServerBeanType {
	public ServerBeanTypeUnknownAS71Product() {
		this( AS7_MODULE_SERVER_MAIN);
	}
	
	public ServerBeanTypeUnknownAS71Product(String path) {
		this("AS-Product", "Application Server",path);
	}
	
	/**
	 * @since 3.0 (actually 2.4.101)
	 */
	protected ServerBeanTypeUnknownAS71Product(String id, String desc, String path) {
		super( id, desc, path);
	}
	
	@Override
	public String getServerBeanName(File root) {
		if( getId().equals("AS-Product"))
			return root.getName();
		return super.getServerBeanName(root);
	}
	@Override
	public String getServerAdapterTypeId(String version) {
		return IServerConstants.SERVER_EAP_60;
	}
	@Override
	public boolean isServerRoot(File location) {
		return getFullVersion(location, null) != null;
	}
	@Override
	public String getFullVersion(File location, File systemJarFile) {
		String productSlot = getSlot(location);
		String product = "org.jboss.as.product";
		if( productSlot != null ) {
			product += "." + productSlot;
		}
		product += ".dir";
		File[] modules = new File[]{new File(location, MODULES)};
		String vers = JBossManifestUtility.getManifestPropFromJBossModulesFolder(modules, product, "META-INF", "JBoss-Product-Release-Version");
		return vers;
	}
	
	/**
	 * @since 3.0 (actually 2.4.101)
	 */
	protected String getSlot(File location) {
		Path rootPath = location.toPath();
		Path productConf = rootPath.resolve(BIN).resolve(PRODUCT_CONF);
		if( productConf.toFile().exists()) {
			Properties p = FileUtil.loadProperties(productConf.toFile());
			return (String) p.get(PRODUCT_CONF_SLOT); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * @since 3.0 (actually 2.4.101)
	 */
	protected String[] getLayers(File location) {
		Path rootPath = location.toPath();
		Path layersConf = rootPath.resolve(MODULES).resolve(LAYERS_CONF);
		String[] layers = new String[0];
		if( layersConf.toFile().exists()) {
			Properties p = FileUtil.loadProperties(layersConf.toFile());
			String layers2 = (String) p.get(LAYERS_CONF_LAYERS); //$NON-NLS-1$
			layers = layers2 == null ? new String[0] : layers2.trim().split(",");
		}
		return layers;
	}
	
	// Provided mostly for subclass to override
	protected String[] getManifestFoldersToFindVersion(String productSlot, String[] layers) {
		return new String[]{ getMetaInfFolderForSlot(productSlot)};
	}
	
	protected String getMetaInfFolderForSlot(String slot) {
		return "modules/org/jboss/as/product/" + slot + "/dir/META-INF"; //$NON-NLS-1$
	}
	
	@Override
	public String getUnderlyingTypeId(File location) {
		String s = getSlot(location);
		return s == null ? null : s.toUpperCase();
	}

}
