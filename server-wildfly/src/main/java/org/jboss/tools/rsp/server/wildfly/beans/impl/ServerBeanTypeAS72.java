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


public class ServerBeanTypeAS72 extends JBossServerBeanType {
	public ServerBeanTypeAS72() {
		super(ID_AS, NAME_AS, AS7_MODULE_LAYERED_SERVER_MAIN);
	}

	public boolean isServerRoot(File location) {
		return checkAS72Version(location, JBAS7_RELEASE_VERSION, "7.2"); //$NON-NLS-1$
	}
	protected boolean checkAS72Version(File location, String property, String propPrefix) {
		return scanFolderJarsForManifestProp(location, systemJarPath, property, propPrefix);
	}

	@Override
	public String getServerAdapterTypeId(String version) {
		if( version.equals("7.2")) return IServerConstants.SERVER_EAP_61;
		return null;
	}
}
