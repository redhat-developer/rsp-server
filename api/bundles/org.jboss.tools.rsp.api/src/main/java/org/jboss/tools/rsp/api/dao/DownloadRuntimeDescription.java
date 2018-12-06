/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.HashMap;
import java.util.Map;


public class DownloadRuntimeDescription {
	/**
	 * A property setting which indicates this runtime may be found
	 * under an alternate id. Suitable values are either
	 * String or String[]
	 */
	public static final String PROPERTY_ALTERNATE_ID = "PROPERTY_ALTERNATE_ID"; //$NON-NLS-1$

	/**
	 * This property will indicate that the given url requires credentials
	 */
	public static final String PROPERTY_REQUIRES_CREDENTIALS = "requiresCredentials"; //$NON-NLS-1$

	/**
	 * This property will indicate how to install the given DownloadRuntime
	 */
	public static final String PROPERTY_INSTALLATION_METHOD = "installationMethod"; //$NON-NLS-1$
	
	
	private String name;
	private String id;
	private String version;
	private String url;
	private String licenseURL;
	private String humanUrl;
	private boolean disclaimer = true;
	private Map<String, String> properties;
	
	private String size = "?"; //$NON-NLS-1$
	private String installationMethod;
	
	public DownloadRuntimeDescription() {
	}
	
	public DownloadRuntimeDescription(String id, String name, String version, String url) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.url = url;
		this.properties = new HashMap<>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isDisclaimer() {
		return disclaimer;
	}

	public void setDisclaimer(boolean disclaimer) {
		this.disclaimer = disclaimer;
	}
	
	public String getLicenseURL() {
		return licenseURL;
	}

	public void setLicenseURL(String url) {
		this.licenseURL = url;
	}
	
	public String getHumanUrl() {
		return humanUrl;
	}

	public void setHumanUrl(String humanUrl) {
		this.humanUrl = humanUrl;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
	/**
	 * Get the installation method to use on this archive once the given url is downloaded. 
	 * Should be one of the constants in {@link IRuntimeInstaller}, such as 
	 * {@link IRuntimeInstaller}.EXTRACT_INSTALLER or {@link IRuntimeInstaller}.JAR_INSTALLER  
	 * 
	 * @return The selected installation method, or IRuntimeInstaller.EXTRACT_INSTALLER if none
	 */
	public String getInstallationMethod() {
		return installationMethod;
	}

	/**
	 * Set the installation method to use on this archive once the given url is downloaded. 
	 * Should be one of the constants in {@link IRuntimeInstaller}, such as 
	 * {@link IRuntimeInstaller}.EXTRACT_INSTALLER or {@link IRuntimeInstaller}.JAR_INSTALLER  
	 */
	public void setInstallationMethod(String installationMethod) {
		this.installationMethod = installationMethod;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DownloadRuntimeDescription other = (DownloadRuntimeDescription) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "DownloadRuntime: name=" //$NON-NLS-1$ //$NON-NLS-2$
				+ name + ", id=" + id + ", version=" + version + ", url=" + url //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ ", licenseURL=" + licenseURL + ", humanUrl=" + humanUrl //$NON-NLS-1$ //$NON-NLS-2$
				+ ", size=" + size + ", disclaimer=" + disclaimer + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
