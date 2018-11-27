/*************************************************************************************
 * Copyright (c) 2010-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core;

import java.util.Collection;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.foundation.core.plugin.BaseCorePlugin;
import org.jboss.tools.rsp.foundation.core.plugin.IPluginLog;
import org.jboss.tools.rsp.foundation.core.plugin.StatusFactory;
import org.jboss.tools.rsp.runtime.core.internal.DownloadRuntimesModel;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimes;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author snjeza
 */
public class RuntimeCoreActivator extends BaseCorePlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.rsp.runtime.core"; //$NON-NLS-1$

	// The shared instance
	private static RuntimeCoreActivator plugin;

	private BundleContext context;
	private IDownloadRuntimes downloader = null;
	
	/**
	 * The constructor
	 */
	public RuntimeCoreActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RuntimeCoreActivator getDefault() {
		return plugin;
	}

	public BundleContext getBundleContext() {
		return context;
	}
	
	public IDownloadRuntimes getDownloader() {
		return downloader;
	}
	
	public void setDownloader(IDownloadRuntimes downloader) {
		this.downloader = downloader;
	}

	
	/**
	 * Get a map of download runtime ID to the actual downloadruntime object
	 * Warning:  This method may involve plugin loading or long-running file or wire IO tasks.
	 * This should not be called from the UI without the ability to respond to progress or initiate a cancelation
	 * @return
	 */
	public Map<String, DownloadRuntime> getDownloadRuntimes(IProgressMonitor monitor) {
		return DownloadRuntimesModel.getDefault().getDownloadRuntimes(monitor);
	}

	/**
	 * Get an array of download runtime objects
	 * 
	 * Warning:  This method may involve plugin loading or long-running file or wire IO tasks.
	 * This should not be called from the UI without the ability to respond to progress or initiate a cancelation
	 * 
	 * @return
	 */
	public DownloadRuntime[] getDownloadRuntimeArray(IProgressMonitor monitor) {
		Map<String, DownloadRuntime> map = DownloadRuntimesModel.getDefault().getDownloadRuntimes(monitor);
		if( map == null )
			return new DownloadRuntime[0];
		Collection<DownloadRuntime> arr = map.values();
		return (DownloadRuntime[]) arr.toArray(new DownloadRuntime[arr.size()]);
	}

	
	/**
	 * This method will check for a download runtime by checking it's
	 * id, or, if none is found, by checking for a PROPERTY_ALTERNATE_ID
	 * property key which matches the id. 
	 * 
	 * @param id A found DownloadRuntime or null
	 * @param IProgressMonitor monitor
	 * @return
	 */
	public DownloadRuntime findDownloadRuntime(String id, IProgressMonitor monitor) {
		return DownloadRuntimesModel.getDefault().findDownloadRuntime(id, monitor);
	}

	
	/**
	 * Get the IPluginLog for this plugin. This method 
	 * helps to make logging easier, for example:
	 * 
	 *     FoundationCorePlugin.pluginLog().logError(etc)
	 *  
	 * @return IPluginLog object
	 */
	public static IPluginLog pluginLog() {
		return getDefault().pluginLogInternal();
	}

	/**
	 * Get a status factory for this plugin
	 * @return status factory
	 */
	public static StatusFactory statusFactory() {
		return getDefault().statusFactoryInternal();
	}
}
