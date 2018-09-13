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


public class ServerBeanTypeEAPStandalone extends ServerBeanTypeEnterprise {
	public ServerBeanTypeEAPStandalone() {
		super("EAP_STD", NAME_EAP, BIN_TWIDDLE_PATH);
	}
	public boolean isServerRoot(File location) {
		File asSystemJar = new File(location, systemJarPath);
		if (asSystemJar.exists() && asSystemJar.isFile()) {
			String title = JBossManifestUtility.getJarProperty(asSystemJar, IMPLEMENTATION_TITLE);
			boolean isEAP = title != null && title.contains(ID_EAP); //$NON-NLS-1$
			return isEAP;
		}
		return false;
	}
	
	public String getServerAdapterTypeId(String version) {
		return getServerAdapterTypeEAPLegacy(version);
	}

}
