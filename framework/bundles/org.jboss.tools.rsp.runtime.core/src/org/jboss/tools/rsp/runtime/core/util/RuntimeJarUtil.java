/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core.util;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class RuntimeJarUtil {
	public static String IMPLEMENTATION_VERSION = "Implementation-Version"; //$NON-NLS-1$
	
	public static String getImplementationVersion(File dir, String file) {
		File jarFile = new File(dir, file);
		return getImplementationVersion(jarFile);
	}
	
	public static String getImplementationVersion(File jarFile) {
		return getImplementationVersion(jarFile, new String[]{IMPLEMENTATION_VERSION});
	}
	public static String getImplementationVersion(File jarFile, String[] attributes) {
		if(!jarFile.isFile()) {
			return null;
		}
		JarFile jar = null;
		try {
			jar = new JarFile(jarFile);
			for( int i = 0; i < attributes.length; i++ ) {
				Attributes attributes2 = jar.getManifest().getMainAttributes();
				String version = attributes2.getValue(attributes[i]);
				if( version != null )
					return version;
			}
		} catch (IOException e) {
			return null;
		} finally {
			try {
				if( jar != null ) {
					jar.close();
				}
			} catch(IOException ioe) {
				// ignore
			}
		}
		return null;
	}
}
