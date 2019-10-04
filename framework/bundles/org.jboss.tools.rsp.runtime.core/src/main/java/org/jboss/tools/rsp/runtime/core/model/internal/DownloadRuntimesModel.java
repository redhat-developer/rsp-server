/*************************************************************************************
 * Copyright (c) 2013-2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.runtime.core.model.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesModel;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesProvider;
import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.model.installer.RuntimesInstallerModel;

public class DownloadRuntimesModel implements IDownloadRuntimesModel {

	private Map<String, DownloadRuntime> cachedDownloadRuntimes = null;
	private Map<String, Map<String, DownloadRuntime>> cachedDownloadRuntimesByProvider = null;
	
	private List<IDownloadRuntimesProvider> downloadRuntimeProviders = null;
	private RuntimesInstallerModel installers = null;
	private File dataLocationRoot = null;
	
	public DownloadRuntimesModel() {
		this.downloadRuntimeProviders = new ArrayList<>();
		this.installers = new RuntimesInstallerModel(this);
	}

	@Override
	public void addDownloadRuntimeProvider(IDownloadRuntimesProvider provider) {
		downloadRuntimeProviders.add(provider);
		clearCache();
	}

	@Override
	public void removeDownloadRuntimeProvider(IDownloadRuntimesProvider provider) {
		downloadRuntimeProviders.remove(provider);
		clearCache();
	}

	@Override
	public DownloadRuntime findDownloadRuntime(String id, IProgressMonitor monitor) {
		Map<String, DownloadRuntime> runtimes = getOrLoadDownloadRuntimes(monitor);
		return findDownloadRuntime(id, runtimes);
	}
	
	@Override
	public Map<String, DownloadRuntime> getOrLoadDownloadRuntimes(IProgressMonitor monitor) {
		// Always return a new instance and not the actual model object
		ensureCacheLoaded(monitor);
		return new HashMap<>(getDownloadRuntimesCache()); 
	}
	
	private void ensureCacheLoaded(IProgressMonitor monitor) {
		Map<String, DownloadRuntime> cached = getDownloadRuntimesCache();
		if( cached == null ) {
			cacheDownloadRuntimes(monitor);
		}
	}
	
	private synchronized void setDownloadRuntimesCache(Map<String, DownloadRuntime> cache) {
		this.cachedDownloadRuntimes = cache;
	}

	private synchronized void setByProviderRuntimesCache(Map<String, Map<String, DownloadRuntime>> cache) {
		this.cachedDownloadRuntimesByProvider = cache;
	}

	private synchronized Map<String, DownloadRuntime> getDownloadRuntimesCache() {
		return cachedDownloadRuntimes == null ? null : new HashMap<>(cachedDownloadRuntimes);
	}

	private synchronized void clearCache() {
		cachedDownloadRuntimes = null;
		cachedDownloadRuntimesByProvider = null;
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
			DownloadRuntime runtime = i.next();
			if (matchesInAlternativeId(id, runtime)) {
				return runtime;
			}
		}
		return null;
	}

	private boolean matchesInAlternativeId(String id, DownloadRuntime runtime) {
		Object alternativeId = runtime.getProperty(DownloadRuntime.PROPERTY_ALTERNATE_ID);
		if( alternativeId instanceof String 
				&& id.equals(alternativeId)) {
			return true;
		}
		return false;
	}
	
	private void cacheDownloadRuntimes(IProgressMonitor monitor) {
		Map<String, DownloadRuntime> map = new HashMap<>();
		Map<String, Map<String, DownloadRuntime>> byProvider = new HashMap<>();

		IDownloadRuntimesProvider[] providers = getDownloadRuntimeProviders();
		monitor.beginTask("Loading Download Runtime Providers", providers.length * 100);
		for( int i = 0; i < providers.length && !monitor.isCanceled(); i++ ) {
			IProgressMonitor subMon = new SubProgressMonitor(monitor, 100);
			
			Map<String, DownloadRuntime> byProviderInner = new HashMap<>();
			byProvider.put(providers[i].getId(), byProviderInner);
			
			DownloadRuntime[] runtimes = providers[i].getDownloadableRuntimes(subMon);
			if( runtimes != null ) {
				for( int j = 0; j < runtimes.length; j++ ) {
					if( runtimes[j] != null ) {
						map.put(runtimes[j].getId(), runtimes[j]);
						byProviderInner.put(runtimes[j].getId(), runtimes[j]);
					}
				}
			}
			subMon.done();
		}
		setDownloadRuntimesCache(map);
		setByProviderRuntimesCache(byProvider);
	}
	
	public IDownloadRuntimesProvider[] getDownloadRuntimeProviders() {
		return downloadRuntimeProviders.toArray(new IDownloadRuntimesProvider[downloadRuntimeProviders.size()]);
	}
	
	@Override
	public IDownloadRuntimesProvider findProviderForRuntime(String id, IProgressMonitor monitor) {
		ensureCacheLoaded(monitor);
		return findProviderForRuntime(id);
	}

	@Override
	public IDownloadRuntimesProvider findProviderForRuntime(String id) {
		if (id == null 
				|| id.isEmpty() 
				|| cachedDownloadRuntimesByProvider == null) {
			return null;
		}
		Set<String> providerKeys = cachedDownloadRuntimesByProvider.keySet();
		for( String k : providerKeys ) {
			Map<String,DownloadRuntime> val = cachedDownloadRuntimesByProvider.get(k);
			if( val != null && val.containsKey(id) ) {
				return findDownloadRuntimeProvider(k);
			}
		}
		return null;
	}

	private IDownloadRuntimesProvider findDownloadRuntimeProvider(String id) {
		IDownloadRuntimesProvider[] all = getDownloadRuntimeProviders();
		for( int i = 0; i < all.length; i++ ) {
			if( id.equals(all[i].getId())) 
				return all[i];
		}
		return null;
	}

	@Override
	public void setDataLocation(File loc) {
		this.dataLocationRoot = loc;
	}

	@Override
	public File getDataLocation() {
		return this.dataLocationRoot;
	}

	@Override
	public IRuntimeInstaller getRuntimeInstaller(String installationMethod) {
		return installers.getRuntimeInstaller(installationMethod);
	}

}
