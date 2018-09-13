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

public class ServerBeanTypeJPP6 extends ServerBeanTypeEAP6 {
	private static final String JPP60a_DIR_META_INF = "modules/org/jboss/as/product/jpp/dir/META-INF"; //$NON-NLS-1$
	private static final String JPP60b_DIR_META_INF = "modules/system/layers/base/org/jboss/as/product/jpp/dir/META-INF"; //$NON-NLS-1$
	public ServerBeanTypeJPP6() {
		super("JPP", "JBoss Portal Platform", AS7_MODULE_SERVER_MAIN);
	}	
	

	protected String getServerTypeBaseName() {
		return "JBoss Portal";
	}

	public boolean isServerRoot(File location) {
		return getFullVersion(location, null) != null;
	}
	public String getFullVersion(File location, File systemJarFile) {
		String s1 = getEAP6xVersion(location, JPP60a_DIR_META_INF, "6.", "jpp", "Portal Platform"); //$NON-NLS-1$
		if( s1 == null )
			s1 = getEAP6xVersion(location, JPP60b_DIR_META_INF, "6.", "jpp", "Portal Platform"); //$NON-NLS-1$
		return s1;
	}
	public String getServerAdapterTypeId(String version) {
		if( "6.0".equals(version))
			return IServerConstants.SERVER_EAP_60;
		return null;
	}

}
