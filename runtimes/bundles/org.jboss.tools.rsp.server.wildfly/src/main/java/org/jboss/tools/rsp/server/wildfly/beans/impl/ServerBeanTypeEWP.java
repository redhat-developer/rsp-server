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

public class ServerBeanTypeEWP extends ServerBeanTypeEnterprise {
	private static final String JBOSS_AS_WEB_PATH = "jboss-as-web"; //$NON-NLS-1$
	public ServerBeanTypeEWP() {
		super(
			"EWP",//$NON-NLS-1$
			"Enterprise Web Platform",//$NON-NLS-1$
			asPath(JBOSS_AS_WEB_PATH,BIN,RUN_JAR_NAME));
	}
	@Override
	public String getRootToAdapterRelativePath(File root, String version) {
		return "jboss-as-web";
	}


	protected String getServerTypeBaseName() {
		return "JBoss Web Platform";
	}
	
	public boolean isServerRoot(File location) {
		File ewpSystemJar = new File(location,systemJarPath);
		return ewpSystemJar.exists() && ewpSystemJar.isFile();
	}
	@Override
	public String getServerAdapterTypeId(String version) {
		return getServerAdapterTypeEAPLegacy(version);
	}

}
