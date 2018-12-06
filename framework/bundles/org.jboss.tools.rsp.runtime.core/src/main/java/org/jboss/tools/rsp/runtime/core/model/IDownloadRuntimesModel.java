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
package org.jboss.tools.rsp.runtime.core.model;

import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;

public interface IDownloadRuntimesModel {

	public Map<String, DownloadRuntime> getOrLoadDownloadRuntimes(IProgressMonitor monitor);
	public DownloadRuntime findDownloadRuntime(String id, IProgressMonitor monitor);
	
	public void addDownloadRuntimeProvider(IDownloadRuntimesProvider provider);
	public void removeDownloadRuntimeProvider(IDownloadRuntimesProvider provider);

	public String[] getRegisteredProviders();
	public Map<String, DownloadRuntime> getDownloadRuntimesForProvider(String id);
}
