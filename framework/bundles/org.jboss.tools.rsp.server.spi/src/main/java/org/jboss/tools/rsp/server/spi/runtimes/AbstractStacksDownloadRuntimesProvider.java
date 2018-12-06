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
package org.jboss.tools.rsp.server.spi.runtimes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.jdf.stacks.model.Runtime;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.rsp.launching.utils.OSUtils;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesProvider;

/**
 * Pull runtimes from a stacks file and return them to runtimes framework
 */
public abstract class AbstractStacksDownloadRuntimesProvider implements IDownloadRuntimesProvider {

	/* The following constants are marked public but are in an internal package. */
	public static final String LABEL_FILE_SIZE = "runtime-size";
	public static final String LABEL_WTP_RUNTIME = "wtp-runtime-type";
	public static final String LABEL_RUNTIME_CATEGORY = "runtime-category";
	public static final String LABEL_RUNTIME_TYPE = "runtime-type";
	public static final String LABEL_FILE_TYPE = "file-type";
	
	public static final String PROP_WTP_RUNTIME = LABEL_WTP_RUNTIME;
	
	private List<DownloadRuntime> downloads = null;

	protected abstract Stacks[] getStacks(IProgressMonitor monitor);
	protected abstract String getLegacyId(String id);
	protected abstract boolean requiresDisclaimer(String runtimeId);
	protected abstract boolean runtimeTypeIsRegistered(String runtimeId);
	
	@Override
	public DownloadRuntime[] getDownloadableRuntimes(IProgressMonitor monitor) {
		if( downloads == null ) {
			List<DownloadRuntime> tmp = loadDownloadableRuntimes(monitor);
			if( monitor.isCanceled()) {
				// Return the incomplete list, but do not cache it
				return tmp.toArray(new DownloadRuntime[tmp.size()]);
			}
			// Cache this, as its assumed to be complete
			downloads = tmp;
		}
		return downloads.toArray(new DownloadRuntime[downloads.size()]);
	}
	
	/*
	 * Return an arraylist of downloadruntime objects
	 */
	private synchronized List<DownloadRuntime> loadDownloadableRuntimes(IProgressMonitor monitor) {
		monitor.beginTask("Loading remote runtimes...", 200);
		Stacks[] stacksArr = getStacks(new SubProgressMonitor(monitor, 100));
		List<DownloadRuntime> all = new ArrayList<>();
		monitor.beginTask("Creating DownloadRuntimes", stacksArr.length * 100);		
		for( int i = 0; i < stacksArr.length && !monitor.isCanceled(); i++ ) {
			IProgressMonitor inner = new SubProgressMonitor(monitor, 100);
			if( stacksArr[i] != null ) {
				traverseStacks(stacksArr[i], all, inner);
			}
		}
		monitor.done();
		return all;
	}
	
	protected abstract void traverseStacks(Stacks stacks, List<DownloadRuntime> list, IProgressMonitor monitor);
	
	protected void traverseStacks(Stacks stacks, List<DownloadRuntime> list, String category, IProgressMonitor monitor) {
		List<org.jboss.jdf.stacks.model.Runtime> runtimes = stacks.getAvailableRuntimes();
		Iterator<org.jboss.jdf.stacks.model.Runtime> i = runtimes.iterator();
		org.jboss.jdf.stacks.model.Runtime workingRT = null;
		monitor.beginTask("Creating Download Runtimes", runtimes.size() * 100);
		while(i.hasNext()) {
			workingRT = i.next();
			String categoryFromStacks = workingRT.getLabels().getProperty(LABEL_RUNTIME_CATEGORY);
			if( category.equals(categoryFromStacks)) {
				String wtpRT = workingRT.getLabels().getProperty(LABEL_WTP_RUNTIME);
				if( runtimeTypeIsRegistered(wtpRT) ) {
					DownloadRuntime dr = createDownloadRuntime(workingRT, wtpRT, category);
					if( dr != null )
						list.add(dr);
				}
			}
			monitor.worked(100);
		}
		monitor.done();
	}
	
	private DownloadRuntime createDownloadRuntime(org.jboss.jdf.stacks.model.Runtime workingRT, String wtpRT, String category) {
		// We can make a DL out of this
		String dlUrl = getDownloadURL(workingRT);
		if( dlUrl == null )
			return null;
		
		String fileSize = workingRT.getLabels().getProperty(LABEL_FILE_SIZE);
		String license = workingRT.getLicense();
		String id = workingRT.getId();
		String legacyId = getLegacyId(id);
		String effectiveId = legacyId == null ? id : legacyId;
		
		String name = workingRT.getName();
		String version = workingRT.getVersion();
		DownloadRuntime dr = new DownloadRuntime(effectiveId, name, version, dlUrl);
		dr.setDisclaimer(requiresDisclaimer(wtpRT)); 
		dr.setHumanUrl(workingRT.getUrl());
		dr.setLicenseURL(license);
		dr.setSize(fileSize);
		dr.setProperty(PROP_WTP_RUNTIME, wtpRT);
		dr.setProperty(LABEL_RUNTIME_CATEGORY, category);
		dr.setProperty(LABEL_RUNTIME_TYPE, workingRT.getLabels().getProperty(LABEL_RUNTIME_TYPE));
		if(workingRT.getLabels().get(DownloadRuntime.PROPERTY_REQUIRES_CREDENTIALS) != null ) 
			dr.setProperty(DownloadRuntime.PROPERTY_REQUIRES_CREDENTIALS, workingRT.getLabels().get(DownloadRuntime.PROPERTY_REQUIRES_CREDENTIALS).toString());
		if(workingRT.getLabels().get(LABEL_FILE_TYPE) != null ) 
			dr.setInstallationMethod((String)workingRT.getLabels().get(LABEL_FILE_TYPE));
		if( legacyId != null )
			dr.setProperty(DownloadRuntime.PROPERTY_ALTERNATE_ID, id);
		return dr;
	}
	
	/**
	 * The following supposes a yaml runtime that has no property "downloadURL", or
	 * has a downloadURL set but also has a label that indicates windows has a 
	 * different url.
	 * 
	 * Approved os types are:
	 *
	 *		"win32";
	 *  	"linux";
	 *  	"aix";
	 *  	"solaris";
	 *  	"hpux";
	 *  	"qnx";
	 *  	"macosx";
	 *  
	 *  These values are also the same as those in org.eclipse.core.runtime.Platform
	 * 
	 * The label "additionalDownloadURLs" will return a Map.
	 * The map will have the key of one of the above constants,
	 * and a value of a url. 
	 */
	protected String getDownloadURL(Runtime workingRT) {
		// First look for an override for this specific OS
		String[] os = getStacksOSStrings();
		Object o = workingRT.getLabels().get("additionalDownloadURLs");
		if( o instanceof Map ) {
			Map m = (Map)o;
			for( int i = 0; i < os.length; i++ ) {
				String val = getValueForKey(m, os[i]);
				if( val != null )
					return val;
			}
		}
		
		// Return a default
		String dlUrl = workingRT.getDownloadUrl();
		if( dlUrl != null )
			return dlUrl;
		return null;
	}
	
	private String getValueForKey(Map m, String key) {
		Iterator i = m.keySet().iterator();
		while(i.hasNext()) {
			Object iNext = i.next();
			if( iNext.equals(key)) {
				return (String)m.get(iNext);
			} 
		}
		return null;
	}
	
	private String[] getStacksOSStrings() {
		if( OSUtils.isWindows()) {
			return new String[] {"win32"};
		}
		if( OSUtils.isMac()) {
			return new String[] {"macosx", "linux"};
		}
		return new String[] {"linux"};
	}
	
}
