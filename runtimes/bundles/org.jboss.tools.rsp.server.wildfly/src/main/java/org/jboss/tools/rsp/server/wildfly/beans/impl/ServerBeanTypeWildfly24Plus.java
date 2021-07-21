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
import java.util.ArrayList;
import java.util.Collections;

import org.jboss.tools.rsp.server.wildfly.impl.util.JBossManifestUtility;

public class ServerBeanTypeWildfly24Plus extends JBossServerBeanType {
	private boolean web;
	private String versionPrefix;
	private String serverAdapterId;

	public ServerBeanTypeWildfly24Plus( String id, String name, String systemJarPath,
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
	
	private static boolean canHandleVersion(int myVersion, int foundVersion) {
		ArrayList<Integer> adaptersDeclared = new ArrayList<>();
		String[] all = IServerConstants.ALL_JBOSS_SERVERS;
		for(int i = 0; i < all.length; i++ ) {
			if( all[i].startsWith(IServerConstants.WF_SERVER_PREFIX)) {
				String suffix = all[i].substring(IServerConstants.WF_SERVER_PREFIX.length());
				int major = -1;
				if( suffix.length() == 2 ) {
					major = Integer.parseInt(suffix);
				} else if( suffix.length() == 3 ) {
					major = Integer.parseInt(suffix.substring(0, 2));
				}
				if( major >= myVersion && major <= foundVersion)
					adaptersDeclared.add(major);
			}
		}
		Collections.sort(adaptersDeclared);
		return adaptersDeclared.get(adaptersDeclared.size()-1) == myVersion;
	}
	
	public static String getFullVersion(File location, File systemFile, String myPrefix) {
		String found = JBossManifestUtility.getManifestPropFromJBossModulesFolder(
				new File[]{new File(location, MODULES)}, 
				"org.jboss.as.product", "main/dir/META-INF", 
				MANIFEST_PROD_RELEASE_VERS);
		return fullVersionIfResponsible(myPrefix, found);
	}

	private static String fullVersionIfResponsible(String myPrefix, String found) {
		if( found != null && found.length() > 2) {
			// We found a version. 
			if( found.startsWith(myPrefix)) {
				return found;
			}
			String majorFound = found.substring(0,2); 
			int majorFoundInt = Integer.parseInt(majorFound);
			if( majorFoundInt < 24 ) {
				return null;
			}
			String myMajor = myPrefix.substring(0,2);
			if( canHandleVersion(Integer.parseInt(myMajor), majorFoundInt)) {
				return found;
			}

		}
		return null;
	}

	public static String getFullVersionWeb(File location, File systemFile, String prefix) {
		String vers = JBossManifestUtility.getManifestPropFromJBossModulesFolder(
				new File[]{new File(location, MODULES)}, 
				"org.jboss.as.product", 
				"wildfly-web/dir/META-INF", MANIFEST_PROD_RELEASE_VERS);
		return fullVersionIfResponsible(prefix, vers);
	}

}
