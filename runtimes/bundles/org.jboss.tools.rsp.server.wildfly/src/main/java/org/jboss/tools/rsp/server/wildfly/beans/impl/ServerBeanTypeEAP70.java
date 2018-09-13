/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
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

public class ServerBeanTypeEAP70 extends ServerBeanTypeEnterprise {
	public ServerBeanTypeEAP70() {
		super(ID_EAP, NAME_EAP, AS7_MODULE_LAYERED_SERVER_MAIN);
	}
	
	@Override
	public String getServerAdapterTypeId(String version) {
		return IServerConstants.SERVER_EAP_70;
	}
	public boolean isServerRoot(File location) {
		return getEAP6xVersion(location, EAP_LAYERED_PRODUCT_META_INF, "7.0", "eap", RELEASE_NAME_JBOSS_EAP) != null; //$NON-NLS-1$
	}
	public String getFullVersion(File location, File systemJarFile) {
		return getEAP6xVersion(location, EAP_LAYERED_PRODUCT_META_INF, "7.0", "eap", RELEASE_NAME_JBOSS_EAP); //$NON-NLS-1$
	}
}
