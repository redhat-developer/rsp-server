/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal.publishing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableResourceDelta;

public class DeployableDelta implements IDeployableResourceDelta {
	
	private static final int UNKNOWN_KIND = -1;
	private DeployableReference reference;
	private Map<Path, Integer> changes;

	public DeployableDelta(DeployableReference reference) {
		this.reference = reference;
		this.changes  = new HashMap<>();
	}
	
	public DeployableDelta(DeployableReference reference, Map<Path, Integer> ch) {
		this.reference = reference;
		this.changes = ch;
	}

	/**
	 * Returns a map of relative paths and their change type
	 */
	@Override
	public Map<Path, Integer> getResourceDeltaMap() {
		return new HashMap<>(changes);
	}

	/**
	 * Returns the DeployableReference for this delta.
	 * 
	 * @return the deployable reference
	 * 
	 * @see DeployableReference
	 */
	public DeployableReference getReference() {
		return reference;
	}
	
	/**
	 * Registers a change for the given file watcher event.
	 * 
	 * @param event the event to register the change for
	 */
	public void registerChange(FileWatcherEvent event) {
		Path changedFile = event.getPath();
		Path referenceBase = Paths.get(reference.getPath());
		Path relative = referenceBase.relativize(changedFile);
		
		int currentChangeConverted = convert(event.getKind());
		
		if (UNKNOWN_KIND == currentChangeConverted) {
			return;
		}

		if(!changes.containsKey(relative)) {
			changes.put(relative, currentChangeConverted);
		} else {
			// Ok, this file has already been changed... ugh
			int existingChange = changes.get(relative);
			if( existingChange == DELETED && currentChangeConverted != DELETED ) {
				changes.put(relative, MODIFIED);
			} else if( existingChange == CREATED && currentChangeConverted == DELETED) {
				changes.remove(relative);
			} else if( existingChange == MODIFIED && currentChangeConverted == DELETED) {
				changes.put(relative, DELETED);
			}
		}
	}
	
	/**
	 * Clears all the changes that were registered in this delta.
	 */
	public void clear() {
		changes.clear();
	}
	
	private int convert(WatchEvent.Kind<?> kind) {
		if( kind == StandardWatchEventKinds.ENTRY_CREATE)
			return CREATED;
		else if( kind == StandardWatchEventKinds.ENTRY_MODIFY)
			return MODIFIED;
		else if( kind == StandardWatchEventKinds.ENTRY_DELETE)
			return DELETED;
		else
			return UNKNOWN_KIND;
	}

}
