/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.discovery;

import java.io.File;

import org.jboss.tools.rsp.api.dao.ServerBean;

public abstract class ServerBeanType {
	
	protected static final String UNKNOWN_STR = "UNKNOWN"; //$NON-NLS-1$
	
	protected String name;
	protected String id=UNKNOWN_STR;
	
	protected ServerBeanType(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	public String toString() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	
	/**
	 * Return true if this location is recognized as a server root
	 * for this server type; false otherwise.
	 * 
	 * @param location
	 * @return
	 */
	public abstract boolean isServerRoot(File location);
	

	/**
	 * Get a full version string for the server at this location
	 * 
	 * @param root
	 * @return
	 */
	public abstract String getFullVersion(File root);
	
	/**
	 * This method is for conditions where the underlying server may be 
	 * of a different id than the JBossServerType. For example, any 
	 * JBossServerType which represents an entire class of similar but
	 * not identical servers, the server type may have an id such as 
	 * AS-Product, and this method may return something like "JPP"
	 * 
	 * Note that differs from the method of the same name in 
	 * the AbstractCondition, which will return null if there is no 
	 * underlying type. This method will default to returning the
	 * value of 'id' in the case where there is no different underlying type.  
	 * 
	 * @param location
	 * @param systemFile
	 * @return an underlying type id, or the id of this JBossServerType
	 * 		   if the condition does not provide an underlying type. 
	 * @since 3.0 (actually 2.4.101)
	 */

	public abstract String getUnderlyingTypeId(File root); 

	

	
	/**
	 * This will return a version, if it can be discovered.
	 * If this is an UNKNOWN server bean, the return 
	 * value will be null
	 * 
	 * @param version
	 * @return
	 */
	public abstract String getServerAdapterTypeId(String version);
	
	
	/**
	 * Get the relative path from what is the server bean's root
	 * to what would be it's server adapter's root, or null if equal. 
	 * 
	 * @param root
	 * @param version
	 * @return
	 */
	public String getRootToAdapterRelativePath(File root, String version) {
		return null;
	}
	
	
	/**
	 * Get a name for this server bean. The default implementation 
	 * returns only the name of the folder, though
	 * subclasses may override this in cases where the default
	 * value does not seem to make sense.
	 * @param root
	 * @Since 3.0
	 * @return
	 */
	public String getServerBeanName(File root) {
		return root.getName();
	}

	public ServerBean createServerBean(File rootLocation) {
		String version = getFullVersion(rootLocation);
		ServerBean server = new ServerBean(
				rootLocation.getPath(), getServerBeanName(rootLocation),
				getId(), getUnderlyingTypeId(rootLocation), version, 
				getMajorMinorVersion(version), getServerAdapterTypeId(version));
		return server;
	}
	

	/**
	 * Turn a version string into a major.minor version string. 
	 * Example:
	 *    getMajorMinorVersion("4.1.3.Alpha3") -> "4.1"
	 *    
	 * @param version
	 * @return
	 */
	public static String getMajorMinorVersion(String version) {
		if(version==null) 
			return "";//$NON-NLS-1$

		int firstDot = version.indexOf(".");
		int secondDot = firstDot == -1 ? -1 : version.indexOf(".", firstDot + 1);
		if( secondDot != -1) {
			String currentVersion = version.substring(0, secondDot);
			return currentVersion;
		}
		if( firstDot != -1)
			// String only has one ".", and is assumed to be already in "x.y" form
			return version;
		return "";
	}
}
