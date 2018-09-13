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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jboss.tools.rsp.server.wildfly.impl.util.JBossManifestUtility;

public class ServerBeanTypeAS7GateIn extends JBossServerBeanType {

	public ServerBeanTypeAS7GateIn() {
		super(ID_GATEIN, NAME_GATEIN,AS7_MODULE_SERVER_MAIN);
	}
	
	protected String getServerTypeBaseName() {
		return getId();
	}

	public boolean isServerRoot(File location) {
		boolean isAS7 = scanFolderJarsForManifestProp(location, systemJarPath, JBAS7_RELEASE_VERSION, "7.");
		if( isAS7 && getFullVersion(location, null) != null ) {
			return true;
		}
		return false;
	}

	public String getFullVersion(File location, File systemJarFile) {
		File f = new File(location, AS7_GATE_IN_SYSTEM_JAR_FOLDER);
		if( f.exists() ) {
			File[] children = f.listFiles();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i].getName().endsWith(IServerConstants.EXT_JAR)) {
					String value = JBossManifestUtility.getJarProperty(children[i], "Specification-Version");
					return value;
				}
			}
		}
		
		File f2 = new File(location, GATEIN_35_PROPERTY_FILE);
		if( f2.exists()) {
			try {
				Properties p = new Properties();
				p.load(new FileInputStream(f2));
				return p.getProperty(VERSION_PROP);
			} catch(IOException ioe) {
				// ignore
			}
		}
		return null;
	}
	
	public String getServerAdapterTypeId(String version) {
		return IServerConstants.SERVER_AS_71;
	}
}
