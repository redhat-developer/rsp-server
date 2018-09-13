/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
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
import java.util.Arrays;
import java.util.List;

import org.jboss.tools.rsp.server.wildfly.impl.util.JBossManifestUtility;

public class DataVirtualization6ServerBeanType extends ServerBeanTypeUnknownAS72Product {
	public DataVirtualization6ServerBeanType() {
		super( "DV",//$NON-NLS-1$
				"JBoss Data Virtualization",//$NON-NLS-1$
				AS7_MODULE_LAYERED_SERVER_MAIN);
	}	
	
	@Override
	public String getServerBeanName(File root) {
		return "JBoss Data Virtualization " + getFullVersion(root, null);
	}
	
	public String getFullVersion(File location, File systemJarFile) {
		String productSlot = getSlot(location);
		boolean hasDV = "dv".equalsIgnoreCase(productSlot);
		if( hasDV ) {
			List<String> layers = Arrays.asList(getLayers(location));
			if( layers.contains("dv") ) {
				String dvProductDir = "org.jboss.as.product.dv.dir";
				File[] modules = new File[]{new File(location, "modules")};
				String vers = JBossManifestUtility.getManifestPropFromJBossModulesFolder(modules, dvProductDir, 
						"META-INF", "JBoss-Product-Release-Version");
				if( vers.startsWith("6."))
					return vers;
			}
		}
		return null;
	}
	
	public String getUnderlyingTypeId(File location, File systemFile) {
		if( getFullVersion(location, systemFile) != null ) 
			return "DV";
		return null;
	}
}
