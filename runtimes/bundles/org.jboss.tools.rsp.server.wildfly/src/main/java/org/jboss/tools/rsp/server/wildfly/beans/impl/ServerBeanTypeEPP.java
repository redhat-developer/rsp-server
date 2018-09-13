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

public class ServerBeanTypeEPP extends JBossServerBeanType {
	
	private static final String JBOSS_PORTLETBRIDGE_PATH = "portletbridge"; //$NON-NLS-1$
	private static final String JBOSS_PORTAL_SAR = "jboss-portal.sar";  //$NON-NLS-1$
	public ServerBeanTypeEPP() {
		super(
				"EPP",//$NON-NLS-1$
				"Enterprise Portal Platform",//$NON-NLS-1$
				asPath(JBOSS_AS_PATH,BIN,RUN_JAR_NAME));
	}
	

	protected String getServerTypeBaseName() {
		return "JBoss Portal";
	}
	
	@Override
	public String getRootToAdapterRelativePath(File root, String version) {
		return JBOSS_AS_PATH;
	}
	public boolean superIsServerRoot(File location) {
		File asSystemJar = new File(location, asPath(JBOSS_AS_PATH,BIN,TWIDDLE_JAR_NAME));
		return asSystemJar.exists() && asSystemJar.isFile();
	}
	public boolean isServerRoot(File location) {
		if( !superIsServerRoot(location))
			return false;
		
		File portletBridgeFolder = new File(location, JBOSS_PORTLETBRIDGE_PATH);
		File portlalSarFolder = new File(location, 
				asPath( JBOSS_AS_PATH,  SERVER, CONFIG_DEFAULT, DEPLOY, JBOSS_PORTAL_SAR));			
		File asStdSystemJar = new File(location,
				asPath(JBOSS_AS_PATH,BIN, RUN_JAR_NAME));
		boolean pbfIsDir = portletBridgeFolder.exists() && portletBridgeFolder.isDirectory(); 
		boolean psfIsDir = portlalSarFolder.exists() && portlalSarFolder.isDirectory(); 
		boolean sysJarIsFile = asStdSystemJar.exists() && asStdSystemJar.isFile(); 
		return ( pbfIsDir || psfIsDir ) && sysJarIsFile; 
	}


	@Override
	public String getServerAdapterTypeId(String version) {
		return IServerConstants.SERVER_EAP_60;
	}
}
