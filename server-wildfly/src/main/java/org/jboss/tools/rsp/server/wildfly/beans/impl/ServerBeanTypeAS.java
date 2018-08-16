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

public class ServerBeanTypeAS extends JBossServerBeanType {

	public ServerBeanTypeAS() {
		super(ID_AS,NAME_AS, BIN_TWIDDLE_PATH);
	}
	
	@Override
	public boolean isServerRoot(File location) {
		File asSystemJar = new File(location, systemJarPath);
		if (asSystemJar.exists() && asSystemJar.isFile()) {
			String title = ManifestUtility.getJarProperty(asSystemJar, IMPLEMENTATION_TITLE);
			boolean isEAP = title != null && title.contains(ID_EAP); //$NON-NLS-1$
			return !isEAP;
		}
		return false;
	}

	@Override
	public String getFullVersion(File root, File systemJar) {
		return getFullServerVersionFromZipLegacy(systemJar);
	}

	@Override
	public String getServerAdapterTypeId(String version) {
		// V6_0, V6_1, V5_1, V5_0, V4_2, V4_0, V3_2
		if( version.startsWith("3.2.")) return IServerConstants.SERVER_AS_32;
		if( version.startsWith("4.0.")) return IServerConstants.SERVER_AS_40;
		if( version.startsWith("4.2.")) return IServerConstants.SERVER_AS_42;
		if( version.startsWith("5.0.")) return IServerConstants.SERVER_AS_50;
		if( version.startsWith("5.1.")) return IServerConstants.SERVER_AS_51;
		if( version.startsWith("6.0.")) return IServerConstants.SERVER_AS_60;
		if( version.startsWith("6.1.")) return IServerConstants.SERVER_AS_60;
		return null;
	}
}
