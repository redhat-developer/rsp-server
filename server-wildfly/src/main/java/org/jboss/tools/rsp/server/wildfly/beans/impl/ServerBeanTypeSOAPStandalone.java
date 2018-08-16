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

public class ServerBeanTypeSOAPStandalone extends ServerBeanTypeEnterprise {
	private static final String JBOSS_ESB_PATH = "jboss-esb"; //$NON-NLS-1$
	private static final String SOAP_JBPM_JPDL_PATH = "jbpm-jpdl"; //$NON-NLS-1$
	public ServerBeanTypeSOAPStandalone() {
		super(
				"SOA-P-STD",//$NON-NLS-1$
				"SOA Platform Standalone",//$NON-NLS-1$
				asPath(JBOSS_ESB_PATH,BIN,RUN_JAR_NAME));
	}
	@Override
	public String getRootToAdapterRelativePath(File root, String version) {
		return "jboss-esb";
	}

	public boolean isServerRoot(File location) {
		File jbpmFolder = new File(location, SOAP_JBPM_JPDL_PATH);
		File soaStdSystemJar = new File(location,JBOSS_ESB_PATH 
				+ File.separatorChar + BIN + File.separatorChar + RUN_JAR_NAME);			
		boolean sysJarIsFile = soaStdSystemJar.exists() && soaStdSystemJar.isFile();
		boolean jbpmFolderIsDir = jbpmFolder.exists() && jbpmFolder.isDirectory(); 
		return jbpmFolderIsDir && sysJarIsFile;
	}
	@Override
	public String getServerAdapterTypeId(String version) {
		return getServerAdapterTypeEAPLegacy(version);
	}
}
