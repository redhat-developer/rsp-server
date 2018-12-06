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
package org.jboss.tools.rsp.server.wildfly.runtimes.download;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractStacksDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.stacks.core.model.StacksManager;

/**
 * Pull runtimes from a stacks file and return them to runtimes framework
 */
public class DownloadRuntimesProvider extends AbstractStacksDownloadRuntimesProvider {

	private Map<String, String> LEGACY_HASHMAP = null;
	
	@Override
	public String getId() {
		return "jboss-wildfly-EAP";
	}
	
	// Given a stacks.yaml runtime id, get the legacy downloadRuntimes id that's required
	protected synchronized String getLegacyId(String id) {
		if( LEGACY_HASHMAP == null )
			loadLegacy();
		return LEGACY_HASHMAP.get(id);
	}
	
	private synchronized void loadLegacy() {
		LEGACY_HASHMAP = new HashMap<>();
		LEGACY_HASHMAP.put("jboss-as328SP1runtime", "org.jboss.tools.runtime.core.as.328" );
		LEGACY_HASHMAP.put("jboss-as405runtime", "org.jboss.tools.runtime.core.as.405" );
		LEGACY_HASHMAP.put("jboss-as423runtime", "org.jboss.tools.runtime.core.as.423" );
		LEGACY_HASHMAP.put("jboss-as501runtime", "org.jboss.tools.runtime.core.as.501" );
		LEGACY_HASHMAP.put("jboss-as510runtime", "org.jboss.tools.runtime.core.as.510" );
		LEGACY_HASHMAP.put("jboss-as610runtime", "org.jboss.tools.runtime.core.as.610" );
		LEGACY_HASHMAP.put("jboss-as701runtime", "org.jboss.tools.runtime.core.as.701" );
		LEGACY_HASHMAP.put("jboss-as702runtime", "org.jboss.tools.runtime.core.as.702" );
		LEGACY_HASHMAP.put("jboss-as710runtime", "org.jboss.tools.runtime.core.as.710" );
		LEGACY_HASHMAP.put("jboss-as711runtime", "org.jboss.tools.runtime.core.as.711" );
	}
	
	
	protected Stacks[] getStacks(IProgressMonitor monitor) {
		return new StacksManager().getStacks("Loading Downloadable Runtimes", monitor, StacksManager.StacksType.PRESTACKS_TYPE, StacksManager.StacksType.STACKS_TYPE);
	}
	
	protected void traverseStacks(Stacks stacks, List<DownloadRuntime> list, IProgressMonitor monitor) {
		traverseStacks(stacks, list, "SERVER", monitor);
	}

	@Override
	protected boolean requiresDisclaimer(String runtimeId) {
		return !runtimeId.startsWith(IServerConstants.EAP_RUNTIME_PREFIX);
	}

	@Override
	protected boolean runtimeTypeIsRegistered(String runtimeId) {
		String val = IServerConstants.RUNTIME_TO_SERVER.get(runtimeId);
		return val != null;
	}
}
