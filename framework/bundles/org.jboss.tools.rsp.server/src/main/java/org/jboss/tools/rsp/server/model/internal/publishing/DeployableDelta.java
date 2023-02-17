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
import org.jboss.tools.rsp.server.spi.servertype.IDeployableDelta;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableResourceDelta;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableResourceDelta.DELTA_TYPE;
import org.jboss.tools.rsp.server.spi.servertype.IDeploymentAssemblyMapping;

public class DeployableDelta implements IDeployableDelta {
	
	private static final int UNKNOWN_KIND = -1;
	private DeployableReference reference;
	private Map<Path, IDeployableResourceDelta> changes;

	public DeployableDelta(DeployableReference reference) {
		this.reference = reference;
		this.changes  = new HashMap<>();
	}
	
	public DeployableDelta(DeployableReference reference, Map<Path, IDeployableResourceDelta> ch) {
		this.reference = reference;
		this.changes = ch;
	}

	private static class DeployableResourceDelta implements IDeployableResourceDelta {
		private Path sourcePath;
		private DELTA_TYPE type;
		public DeployableResourceDelta(Path sourcePath, DELTA_TYPE type) {
			this.type = type;
			this.sourcePath = sourcePath;
		}
		public DELTA_TYPE getDeltaType() {
			return type;
		}
		public Path getSourcePath() {
			return sourcePath;
		}
		
	}
	/**
	 * Returns a map of relative paths and their change type
	 */
	@Override
	public Map<Path, IDeployableResourceDelta> getResourceDeltaMap() {
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
		registerChange(event, relative);
	}
	
	/**
	 * Registers a change for the given file watcher event.
	 * 
	 * @param event the event to register the change for
	 * @param assembly the assembly file structure
	 */
	public void registerChange(FileWatcherEvent event, DeploymentAssemblyFile assembly) {
		Path changedFile = event.getPath();
		IDeploymentAssemblyMapping matchedMapping = findMatchedMapping(assembly, changedFile);
		if( matchedMapping != null ) {
			Path mappingSourcePath = Paths.get(reference.getPath(), matchedMapping.getSource());
			Path relativeToSource = mappingSourcePath.relativize(changedFile);
			
			String depPath = matchedMapping.getDeployPath();
			depPath = depPath.startsWith("/") ? depPath.substring(1) : depPath;
			Path mappingDestPath = Paths.get(depPath);
			Path relativeToOutput = mappingDestPath.resolve(relativeToSource);
			registerChange(event, relativeToOutput);
		}
	}
	
	private IDeploymentAssemblyMapping findMatchedMapping(DeploymentAssemblyFile assembly, Path changedFile) {
		IDeploymentAssemblyMapping[] mappings = assembly.getMappings();
		for( int i = 0; i < mappings.length; i++ ) {
			Path p = Paths.get(reference.getPath(), mappings[i].getSource());
			if( changedFile.startsWith(p)) {
				return mappings[i];
			}
		}
		return null;
	}
	
	public void registerChange(FileWatcherEvent event, Path outputRelative) {
		Path changedFile = event.getPath();
		DELTA_TYPE currentChangeConverted = convert(event.getKind());

		if (DELTA_TYPE.UNKNOWN == currentChangeConverted) {
			return;
		}

		if(!changes.containsKey(outputRelative)) {
			changes.put(outputRelative, new DeployableResourceDelta(changedFile, currentChangeConverted));
		} else {
			// Ok, this file has already been changed... ugh
			IDeployableResourceDelta existingChange = changes.get(outputRelative);
			DELTA_TYPE existingChangeType = existingChange.getDeltaType();
			if( existingChangeType == DELTA_TYPE.DELETED && currentChangeConverted != DELTA_TYPE.DELETED ) {
				changes.put(outputRelative, new DeployableResourceDelta(changedFile, DELTA_TYPE.MODIFIED));
			} else if( existingChangeType == DELTA_TYPE.CREATED && currentChangeConverted == DELTA_TYPE.DELETED) {
				changes.remove(outputRelative);
			} else if( existingChangeType == DELTA_TYPE.CREATED && currentChangeConverted == DELTA_TYPE.MODIFIED) {
				changes.put(outputRelative, new DeployableResourceDelta(changedFile, DELTA_TYPE.CREATED));
			} else if( existingChangeType == DELTA_TYPE.MODIFIED && currentChangeConverted == DELTA_TYPE.DELETED) {
				changes.put(outputRelative, new DeployableResourceDelta(changedFile, DELTA_TYPE.DELETED));
			} else {
				// Always replace the delta in case two folders map to same output
				// and the most recent change is a different file than the previous
				changes.put(outputRelative, new DeployableResourceDelta(changedFile, currentChangeConverted));
			}
		}
	}
	
	/**
	 * Clears all the changes that were registered in this delta.
	 */
	public void clear() {
		changes.clear();
	}
	
	private DELTA_TYPE convert(WatchEvent.Kind<?> kind) {
		if( kind == StandardWatchEventKinds.ENTRY_CREATE)
			return DELTA_TYPE.CREATED;
		else if( kind == StandardWatchEventKinds.ENTRY_MODIFY)
			return DELTA_TYPE.MODIFIED;
		else if( kind == StandardWatchEventKinds.ENTRY_DELETE)
			return DELTA_TYPE.DELETED;
		else
			return DELTA_TYPE.UNKNOWN;
	}

}
