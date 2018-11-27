/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.core.runtime.SubMonitor;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.runtime.core.Messages;
import org.jboss.tools.rsp.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.rsp.runtime.core.model.internal.ExtractionRuntimeInstaller;

/**
 * An object that represents a downloadable runtime. 
 * It must have several key settings, as well as some optional. 
 * It also allows the setting of arbitrary properties for filtering
 * at later points. 
 * 
 * DownloadRuntime objects are most often instantiated by an {@link IDownloadRuntimesProvider},
 * which is in charge of exposing the known runtimes to the framework. 
 * 
 * @author snjeza
 *
 */
public class DownloadRuntime {
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
	
	
	
	
	private byte[] BUFFER = null;
	private String name;
	private String id;
	private String version;
	private String url;
	private String licenseURL;
	private String humanUrl;
	private String size = "?"; //$NON-NLS-1$
	private boolean disclaimer = true;
	private HashMap<String, Object> properties;
	private String installationMethod = ExtractionRuntimeInstaller.ID;
	
	
	public DownloadRuntime(String id, String name, String version, String url) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.url = url;
		this.properties = new HashMap<String, Object>();
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
		DownloadRuntime other = (DownloadRuntime) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public boolean isDisclaimer() {
		return disclaimer;
	}

	public void setDisclaimer(boolean disclaimer) {
		this.disclaimer = disclaimer;
	}
	
	public void setLicenseURL(String url) {
		this.licenseURL = url;
	}
	
	public String getLicenceURL() {
		return licenseURL;
	}

	/*
	 * @see IInstallableRuntime#getLicense(IProgressMonitor)
	 */
	public String getLicense(IProgressMonitor monitor) throws CoreException {
		URL url = null;
		ByteArrayOutputStream out = null;
		try {
			if (licenseURL == null)
				return null;
			
			url = new URL(licenseURL);
			InputStream in = url.openStream();
			out = new ByteArrayOutputStream();
			copyWithSize(in, out, null, 0);
			return new String(out.toByteArray());
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, 
					RuntimeCoreActivator.PLUGIN_ID, 0,
					NLS.bind(Messages.DownloadRuntime_Unable_to_fetch_license, e.getLocalizedMessage()), e));
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	private void copyWithSize(InputStream in, OutputStream out, IProgressMonitor monitor, int size) throws IOException {
		if (BUFFER == null)
			BUFFER = new byte[8192];
		SubMonitor progress = SubMonitor.convert(monitor, size);
		int r = in.read(BUFFER);
		while (r >= 0) {
			out.write(BUFFER, 0, r);
			progress.worked(r);
			r = in.read(BUFFER);
		}
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

	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	
	public Object getProperty(String key) {
		return properties.get(key);
	}
	
	/**
	 * Get the installation method to use on this archive once the given url is downloaded. 
	 * Should be one of the constants in {@link IRuntimeInstaller}, such as 
	 * {@link IRuntimeInstaller}.EXTRACT_INSTALLER or {@link IRuntimeInstaller}.JAR_INSTALLER  
	 * 
	 * @return The selected installation method, or IRuntimeInstaller.EXTRACT_INSTALLER if none
	 */
	public String getInstallationMethod() {
		return installationMethod == null ? IRuntimeInstaller.EXTRACT_INSTALLER : installationMethod;
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
	public String toString() {
		return "DownloadRuntime [BUFFER=" + Arrays.toString(BUFFER) + ", name=" //$NON-NLS-1$ //$NON-NLS-2$
				+ name + ", id=" + id + ", version=" + version + ", url=" + url //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ ", licenseURL=" + licenseURL + ", humanUrl=" + humanUrl //$NON-NLS-1$ //$NON-NLS-2$
				+ ", size=" + size + ", disclaimer=" + disclaimer + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
