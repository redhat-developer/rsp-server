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

public class ServerBeanTypeEAP extends ServerBeanTypeEnterprise {

	public ServerBeanTypeEAP() {
		super(ID_EAP, NAME_EAP, JBOSSAS_TWIDDLE_PATH);
	}
	
	protected ServerBeanTypeEAP(String id, String name, String jbossSystemJarPath) {
		super(id, name, jbossSystemJarPath);
	}
	
	@Override
	public String getRootToAdapterRelativePath(File root, String version) {
		return JBOSS_AS_PATH;
	}

	public String getServerAdapterTypeId(String version) {
		return getServerAdapterTypeEAPLegacy(version);
	}
	

	public boolean isServerRoot(File location) {
		File asSystemJar = new File(location, systemJarPath);
		return asSystemJar.exists() && asSystemJar.isFile();
	}

}
