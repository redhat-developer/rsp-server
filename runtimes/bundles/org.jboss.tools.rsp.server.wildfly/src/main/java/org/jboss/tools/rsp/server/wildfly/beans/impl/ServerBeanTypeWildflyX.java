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

import org.jboss.tools.rsp.server.wildfly.impl.util.JBossManifestUtility;

public class ServerBeanTypeWildflyX extends JBossServerBeanType {
	private boolean web;
	private String versionPrefix;
	private String serverAdapterId;

	public ServerBeanTypeWildflyX( String id, String name, String systemJarPath,
			boolean web, String versionPrefix, String serverAdapterId) {
		super(id, name, systemJarPath);
		this.web = web;
		this.versionPrefix = versionPrefix;
		this.serverAdapterId = serverAdapterId;
	}
	
	protected String getServerTypeBaseName() {
		return getId();
	}
	
	@Override
	public String getFullVersion(File location, File systemFile) {
		if( !this.web ) 
			return getFullVersion(location, systemFile, this.versionPrefix);
		else
			return getFullVersionWeb(location, systemFile, this.versionPrefix);
	}

	public boolean isServerRoot(File location) {
		return getFullVersion(location, null) != null;
	}
	
	public String getServerAdapterTypeId(String version) {	
		return this.serverAdapterId;
	}
	

	public static String getFullVersion(File location, File systemFile, String prefix) {
		String vers = JBossManifestUtility.getManifestPropFromJBossModulesFolder(
				new File[]{new File(location, MODULES)}, 
				"org.jboss.as.product", "wildfly-full/dir/META-INF", 
				MANIFEST_PROD_RELEASE_VERS);
		if( vers != null && vers.startsWith(prefix)) {
			return vers;
		}
		return null;
	}

	public static String getFullVersionWeb(File location, File systemFile, String prefix) {
		String vers = JBossManifestUtility.getManifestPropFromJBossModulesFolder(
				new File[]{new File(location, MODULES)}, 
				"org.jboss.as.product", 
				"wildfly-web/dir/META-INF", MANIFEST_PROD_RELEASE_VERS);
		if( vers != null && vers.startsWith(prefix)) {
			return vers;
		}
		return null;
	}

}
