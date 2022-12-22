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

public class ServerBeanTypeWildflyPlus extends JBossServerBeanType {
	private boolean web;
	private String versionPrefix;
	private String serverAdapterId;
	private int majorVersion;

	public ServerBeanTypeWildflyPlus( String id, String name, String systemJarPath,
			boolean web, int majorVersion, String serverAdapterId) {
		super(id, name, systemJarPath);
		this.web = web;
		this.majorVersion = majorVersion;
		this.versionPrefix = Integer.toString(majorVersion) + ".";
		this.serverAdapterId = serverAdapterId;
	}
	
	protected String getServerTypeBaseName() {
		return getId();
	}
	
	@Override
	public String getFullVersion(File location, File systemFile) {
		if( !this.web ) 
			return getFullVersion(location, systemFile, this.majorVersion, this.versionPrefix);
		else
			return getFullVersionWeb(location, systemFile, this.majorVersion, this.versionPrefix);
	}

	public boolean isServerRoot(File location) {
		return getFullVersion(location, null) != null;
	}
	
	public String getServerAdapterTypeId(String version) {	
		return this.serverAdapterId;
	}
	
	private static boolean canHandleVersion(int beanTypeVersion, int foundVersion) {
		ArrayList<Integer> adaptersDeclared = new ArrayList<>();
		String[] allServerTypes = IServerConstants.ALL_JBOSS_SERVERS;
		for(int i = 0; i < allServerTypes.length; i++ ) {
			String st = allServerTypes[i];
			if( st.startsWith(IServerConstants.WF_SERVER_PREFIX)) {
				String suffix = st.substring(IServerConstants.WF_SERVER_PREFIX.length());
				int serverTypeMajor = -1;
				if( suffix.length() == 2 ) {
					serverTypeMajor = Integer.parseInt(suffix.substring(0,1));
				} else if( suffix.length() == 3 ) {
					serverTypeMajor = Integer.parseInt(suffix.substring(0, 2));
				}
				if( serverTypeMajor >= beanTypeVersion && serverTypeMajor <= foundVersion)
					adaptersDeclared.add(serverTypeMajor);
			}
		}
		Collections.sort(adaptersDeclared);
		boolean ret = adaptersDeclared.get(adaptersDeclared.size()-1) == beanTypeVersion;
		return ret;
	}
	
	public static String getFullVersion(File location, 
			File systemFile, int majorVersion, String myPrefix) {
		String found = JBossManifestUtility.getManifestPropFromJBossModulesFolder(
				new File[]{new File(location, MODULES)}, 
				"org.jboss.as.product", "main/dir/META-INF", 
				MANIFEST_PROD_RELEASE_VERS);
		return fullVersionIfResponsible(myPrefix, majorVersion, found);
	}

	private static String fullVersionIfResponsible(String myPrefix, int majorVersion, String found) {
		if( found != null && found.length() > 2) {
			// We found a version. 
			if( found.startsWith(myPrefix)) {
				return found;
			}
			String majorFound = found.substring(0,2); 
			int majorFoundInt = Integer.parseInt(majorFound);
			if( majorFoundInt < majorVersion ) {
				return null;
			}
			String myMajor = myPrefix.substring(0,2);
			if( canHandleVersion(Integer.parseInt(myMajor), majorFoundInt)) {
				return found;
			}

		}
		return null;
	}

	public static String getFullVersionWeb(File location, 
			File systemFile, int majorVersion, String prefix) {
		String vers = JBossManifestUtility.getManifestPropFromJBossModulesFolder(
				new File[]{new File(location, MODULES)}, 
				"org.jboss.as.product", 
				"wildfly-web/dir/META-INF", MANIFEST_PROD_RELEASE_VERS);
		return fullVersionIfResponsible(prefix, majorVersion, vers);
	}

}
