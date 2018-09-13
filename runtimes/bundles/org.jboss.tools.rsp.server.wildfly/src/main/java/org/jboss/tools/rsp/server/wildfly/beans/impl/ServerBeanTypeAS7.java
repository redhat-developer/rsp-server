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


public class ServerBeanTypeAS7 extends JBossServerBeanType {
	public ServerBeanTypeAS7() {
		super(ID_AS,NAME_AS,AS7_MODULE_SERVER_MAIN);
	}
	public boolean isServerRoot(File location) {
		return scanFolderJarsForManifestProp(location, systemJarPath, JBAS7_RELEASE_VERSION, "7.");
	}
	
	public String getFullVersion(File location, File systemFile) {
		return JBossManifestUtility.getManifestPropFromFolderJars(location, systemJarPath, JBAS7_RELEASE_VERSION);
	}

	@Override
	public String getServerAdapterTypeId(String version) {
		if( version.equals("7.0")) return IServerConstants.SERVER_AS_70;
		if( version.equals("7.1")) return IServerConstants.SERVER_AS_71;
		if( version.equals("7.2")) return IServerConstants.SERVER_AS_71;
		return null;
	}
}
