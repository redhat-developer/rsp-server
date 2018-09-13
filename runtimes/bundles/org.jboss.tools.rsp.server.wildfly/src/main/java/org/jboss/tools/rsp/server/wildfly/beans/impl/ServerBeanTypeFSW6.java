/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
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

import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.wildfly.impl.util.JBossManifestUtility;

/**
 * Verifies the installation has:
 *   1) slot=soa
 *   2) layers contains soa and sramp
 *   3) modules/system/layers/sramp/org/jboss/as/product/sramp/dir/META-INF/MANIFEST.MF
 *         has a key JBoss-Product-Release-Version that begins with "6."
 * @since 3.0
 */
public class ServerBeanTypeFSW6 extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeFSW6() {
		super("FSW", "JBoss Fuse Source Works", AS7_MODULE_LAYERED_SERVER_MAIN);
	}
	
	@Override
	public String getServerBeanName(File root) {
		return "JBoss Fuse Service Works " + ServerBeanType.getMajorMinorVersion(getFullVersion(root, null));
	}
	
	@Override
	public String getFullVersion(File location, File systemJarFile) {
		String productSlot = getSlot(location);
		boolean hasSoa = "soa".equalsIgnoreCase(productSlot);
		if( hasSoa || "sramp".equals(productSlot)) {
			List<String> layers = Arrays.asList(getLayers(location));
			if( (hasSoa ? layers.contains("soa") : true ) && layers.contains("sramp")) {
				String srampProductDir = "org.jboss.as.product.sramp.dir";
				File[] modules = new File[]{new File(location, MODULES)};
				String vers = JBossManifestUtility.getManifestPropFromJBossModulesFolder(modules, srampProductDir, 
						META_INF, MANIFEST_PROD_RELEASE_VERS);
				if( vers.startsWith("6.0"))
					return vers;
			}
		}
		return null;
	}
	public String getUnderlyingTypeId(File location, File systemFile) {
		if( getFullVersion(location, systemFile) != null ) 
			return "FSW";
		return null;
	}
}
