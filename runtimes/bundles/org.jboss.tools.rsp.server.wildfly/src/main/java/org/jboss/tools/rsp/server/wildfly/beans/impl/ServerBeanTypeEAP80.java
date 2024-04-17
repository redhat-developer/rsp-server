/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.beans.impl;

import java.io.File;

public class ServerBeanTypeEAP80 extends ServerBeanTypeEnterprise {
	public ServerBeanTypeEAP80() {
		super(ID_EAP, NAME_EAP, AS7_MODULE_LAYERED_SERVER_MAIN);
	}
	@Override
	public String getServerAdapterTypeId(String version) {
		return IServerConstants.SERVER_EAP_80;
	}
	public boolean isServerRoot(File location) {
		String s1 = getEAP6xVersion(location, EAP_LAYERED_PRODUCT_META_INF, "8.0", "eap", RELEASE_NAME_JBOSS_EAP); //$NON-NLS-1$
		if( s1 != null )
			return true;
		String s2 = getEAP8xVersion(location, "8."); //$NON-NLS-1$
		if( s2 != null )
			return true;
		return false;
	}
	
	public String getFullVersion(File location, File systemJarFile) {
		String s1 = getEAP6xVersion(location, EAP_LAYERED_PRODUCT_META_INF, "8.0", "eap", RELEASE_NAME_JBOSS_EAP); //$NON-NLS-1$
		if( s1 != null ) {
			return s1;
		}
		String s2 = getEAP8xVersion(location, "8."); //$NON-NLS-1$
		if( s2 != null )
			return s2;
		return null;
	}
}
