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

import org.jboss.tools.rsp.server.wildfly.impl.util.JBossManifestUtility;

public class ServerBeanTypeUnknownAS72Product extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeUnknownAS72Product() {
		super("EAP-Product", "EAP-Based Product", AS7_MODULE_LAYERED_SERVER_MAIN);
	}
	protected ServerBeanTypeUnknownAS72Product(String id, String desc, String path) {
		super( id, desc, path);
	}


	@Override
	public boolean isServerRoot(File location) {
		return server72OrHigher(location) && getFullVersion(location, null) != null;
	}
	
	protected boolean server72OrHigher(File loc) {
		File[] mods = new File[]{new File(loc, MODULES)};
		String serverVersion = JBossManifestUtility.getManifestPropFromJBossModules(mods, 
				"org.jboss.as.server", SLOT_MAIN, MANIFEST_PROD_RELEASE_VERS);
		if( serverVersion == null ) {
			serverVersion = JBossManifestUtility.getManifestPropFromJBossModules(mods, 
				"org.jboss.as.server", SLOT_MAIN, IMPLEMENTATION_VERSION);
		}
		if( serverVersion != null && serverVersion.length() > 3) {
			if( serverVersion.startsWith("7.") && "2".compareTo(""+serverVersion.charAt(2)) <= 0) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getServerAdapterTypeId(String version) {
		return IServerConstants.SERVER_EAP_61;
	}
}
