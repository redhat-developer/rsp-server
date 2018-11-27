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
package org.jboss.tools.rsp.runtime.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesProvider;

public class DownloadRuntimesModel {

	private static DownloadRuntimesModel manager = null;
	public static DownloadRuntimesModel getDefault() {
		if( manager == null )
			manager = new DownloadRuntimesModel();
		return manager;
	}

	// Member variables
	private Map<String, DownloadRuntime> cachedDownloadRuntimes = null;
	private List<IDownloadRuntimesProvider> downloadRuntimeProviders = null;
	public Map<String, DownloadRuntime> getDownloadRuntimes(IProgressMonitor monitor) {

		// Cache for now, since we still fetch remote files
		// Once fetching remote files is removed, we no longer
		// need to cache this, and in fact should not. 
		// Individual providers can cache on their own, or not, a they wish
		// We still return the actual data map. This is pretty bad. 
		Map<String, DownloadRuntime> cached = getDownloadRuntimesCache();
		if( cached == null ) {
			cached = loadDownloadRuntimes(monitor);
			if( monitor.isCanceled()) {
				// Do not cache, as the list is incomplete and should be loaded again.
				return cached;
			}
			setDownloadRuntimesCache(cached);
		}
		return cached;
	}
	private synchronized void setDownloadRuntimesCache(Map<String, DownloadRuntime> cache) {
		this.cachedDownloadRuntimes = cache;
	}
	private synchronized Map<String, DownloadRuntime> getDownloadRuntimesCache() {
		return cachedDownloadRuntimes == null ? null : new HashMap<>(cachedDownloadRuntimes);
	}
	private synchronized void clearCache() {
		cachedDownloadRuntimes = null;
	}
	public void addDownloadRuntimeProvider(IDownloadRuntimesProvider provider) {
		downloadRuntimeProviders.add(provider);
		clearCache();
	}

	public void removeDownloadRuntimeProvider(IDownloadRuntimesProvider provider) {
		downloadRuntimeProviders.remove(provider);
		clearCache();
	}

	public DownloadRuntime findDownloadRuntime(String id, IProgressMonitor monitor) {
		Map<String, DownloadRuntime> runtimes = getDownloadRuntimes(monitor);
		return findDownloadRuntime(id, runtimes);
	}
	
	private DownloadRuntime findDownloadRuntime(String id, Map<String, DownloadRuntime> runtimes) {
		if( id == null )
			return null;
		
		DownloadRuntime rt = runtimes.get(id);
		if( rt != null )
			return rt;
		Collection<DownloadRuntime> rts = runtimes.values();
		Iterator<DownloadRuntime> i = rts.iterator();
		while(i.hasNext()) {
			DownloadRuntime i1 = i.next();
			Object propVal = i1.getProperty(DownloadRuntime.PROPERTY_ALTERNATE_ID);
			if( propVal != null ) {
				if( propVal instanceof String[]) {
					String[] propVal2 = (String[]) propVal;
					for( int it = 0; it < propVal2.length; it++ ) {
						if( id.equals(propVal2[it]))
							return i1;
					}
				} else if( propVal instanceof String ) {
					if( id.equals(propVal))
						return i1;
				}
			}
		}
		return null;
	}
	
	private Map<String, DownloadRuntime> loadDownloadRuntimes(IProgressMonitor monitor) {
		HashMap<String, DownloadRuntime> tmp = new HashMap<String, DownloadRuntime>();
		monitor.beginTask("Loading Downloadable Runtimes", 300);
		loadDownloadableRuntimesFromProviders(tmp, new SubProgressMonitor(monitor, 300));
		return tmp;
	}	
	

	/**
	 * This method is NOT PUBLIC. 
	 * It is only exposed for TESTING purposes.
	 * 
	 * @param map
	 */
	public void loadDownloadableRuntimesFromProviders(Map<String, DownloadRuntime> map, IProgressMonitor monitor) {
		IDownloadRuntimesProvider[] providers = getDownloadRuntimeProviders();
		monitor.beginTask("Loading Download Runtime Providers", providers.length * 100);
		for( int i = 0; i < providers.length && !monitor.isCanceled(); i++ ) {
			IProgressMonitor inner = new SubProgressMonitor(monitor, 100);
			DownloadRuntime[] runtimes = providers[i].getDownloadableRuntimes(null, inner);
			if( runtimes != null ) {
				for( int j = 0; j < runtimes.length; j++ ) {
					if( runtimes[j] != null )
						map.put(runtimes[j].getId(), runtimes[j]);
				}
			}
			inner.done();
		}
	}
	
	private IDownloadRuntimesProvider[] getDownloadRuntimeProviders() {
		ensureDownloadRuntimeProvidersInitialized();
		return downloadRuntimeProviders.toArray(new IDownloadRuntimesProvider[downloadRuntimeProviders.size()]);
	}
	private void ensureDownloadRuntimeProvidersInitialized() {
		if( downloadRuntimeProviders == null ) {
			downloadRuntimeProviders = loadDownloadRuntimeProviders();
		}
	}
	
	private List<IDownloadRuntimesProvider> loadDownloadRuntimeProviders() {
		ArrayList<IDownloadRuntimesProvider> list = new ArrayList<IDownloadRuntimesProvider>();
		return list;
	}
}
