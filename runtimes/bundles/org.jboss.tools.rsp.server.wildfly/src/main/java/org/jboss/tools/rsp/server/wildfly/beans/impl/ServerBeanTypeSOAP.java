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
import java.util.regex.Pattern;

public class ServerBeanTypeSOAP extends ServerBeanTypeEAP {
	private static final String SOAP_JBPM_JPDL_PATH = "jbpm-jpdl"; //$NON-NLS-1$
	public ServerBeanTypeSOAP() {
		super(
		"SOA-P",//$NON-NLS-1$
		"SOA Platform",//$NON-NLS-1$
		JBOSSAS_TWIDDLE_PATH);
	}

	@Override
	public String getRootToAdapterRelativePath(File root, String version) {
		return JBOSS_AS_PATH;
	}

	
	public boolean isServerRoot(File location) {
		File jbpmFolder = new File(location, SOAP_JBPM_JPDL_PATH);
		return super.isServerRoot(location) && jbpmFolder.exists() && jbpmFolder.isDirectory();
	}
	
	public String getFullVersion(File location, File systemFile) {
		String fullVersion = super.getFullVersion(location, systemFile);
		if (fullVersion != null && fullVersion.length() >= 5) {
			// SOA-P 5.2, SOA-P 5.3 ...
			String check = fullVersion.substring(0,5); 
			Pattern pattern = Pattern.compile("5\\.1\\.[1-9]");
			Pattern pattern531 = Pattern.compile("5\\.2\\.[0-9]");
			if (pattern.matcher(check).matches() || pattern531.matcher(check).matches()) {
				String runJar = JBOSS_AS_PATH + File.separatorChar + 
					BIN+ File.separatorChar + RUN_JAR_NAME;
				fullVersion = super.getFullVersion(location,new File(location, runJar));
			}
		}
		return fullVersion;
	}
}
