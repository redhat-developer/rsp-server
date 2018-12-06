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

import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.core.runtime.SubMonitor;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.runtime.core.Messages;
import org.jboss.tools.rsp.runtime.core.RuntimeCoreActivator;

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
public class DownloadRuntime extends DownloadRuntimeDescription {

	public DownloadRuntime(String effectiveId, String name, String version, String dlUrl) {
		setId(effectiveId);
		setName(name);
		setVersion(version);
		setUrl(dlUrl);
	}

	public void setProperty(String key, String value) {
		if( getProperties() != null ) 
			getProperties().put(key, value);
	}
	
	public String getProperty(String key) {
		if( getProperties() != null ) 
			return getProperties().get(key);
		return null;
	}
	
	
	public String getLicense(IProgressMonitor monitor) throws CoreException {
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			if (getLicenseURL() == null)
				return null;
			
			URL url = new URL(getLicenseURL());
			InputStream in = url.openStream();
			copyWithSize(in, out, null, 0);
			return new String(out.toByteArray());
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, 
					RuntimeCoreActivator.PLUGIN_ID, 0,
					NLS.bind(Messages.DownloadRuntime_Unable_to_fetch_license, e.getLocalizedMessage()), e));
		}
	}
	
	private void copyWithSize(InputStream in, OutputStream out, IProgressMonitor monitor, int size) throws IOException {
		byte[] BUFFER = new byte[8192];
		SubMonitor progress = SubMonitor.convert(monitor, size);
		int r = in.read(BUFFER);
		while (r >= 0) {
			out.write(BUFFER, 0, r);
			progress.worked(r);
			r = in.read(BUFFER);
		}
	}
	
	public DownloadRuntimeDescription toDao() {
		DownloadRuntimeDescription ret = new DownloadRuntimeDescription();
		ret.setDisclaimer(isDisclaimer());
		ret.setHumanUrl(getHumanUrl());
		ret.setId(getId());
		ret.setInstallationMethod(getInstallationMethod());
		ret.setName(getName());
		ret.setProperties(getProperties());
		ret.setVersion(getVersion());
		ret.setSize(getSize());
		ret.setLicenseURL(getLicenseURL());
		ret.setUrl(getUrl());
		return ret;
	}

}
