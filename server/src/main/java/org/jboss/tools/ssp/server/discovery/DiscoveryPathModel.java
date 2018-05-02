/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.tools.ssp.api.beans.DiscoveryPath;

public class DiscoveryPathModel {
	private List<DiscoveryPath> paths;
	private List<IDiscoveryPathListener> listeners;
	
	// TODO persistence? 
	public DiscoveryPathModel() {
		paths = new ArrayList<DiscoveryPath>();
		listeners = new ArrayList<IDiscoveryPathListener>();
	}
	
	public void addListener(IDiscoveryPathListener l) {
		listeners.add(l);
	}

	public void removeListener(IDiscoveryPathListener l) {
		listeners.remove(l);
	}

	public List<DiscoveryPath> getPaths() {
		return Collections.unmodifiableList(paths);
	}
	
	public void addPath(DiscoveryPath path) {
		if( !paths.contains(path)) {
			paths.add(path);
			for(IDiscoveryPathListener l : listeners ) {
				l.discoveryPathAdded(path);
			}
		}
	}
	
	public void removePath(DiscoveryPath path) {
		if( paths.contains(path)) {
			paths.remove(path);
			for(IDiscoveryPathListener l : listeners ) {
				l.discoveryPathRemoved(path);
			}
		}
	}

}
