package org.jboss.tools.rsp.server.model.internal.publishing;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableResourceDelta;

public class DeployableDelta implements IDeployableResourceDelta {
	
	private DeployableReference reference;

	private Map<Path, Integer> changes;
	public DeployableDelta(DeployableReference reference) {
		this.reference = reference;
		this.changes  = new HashMap<Path, Integer>();
	}
	
	/*
	 * Get a map of relative paths and their change type
	 */
	@Override
	public Map<Path, Integer> getResourceDeltaMap() {
		return new HashMap<Path, Integer>(changes);
	}

	public void registerChange(FileWatcherEvent event) {
		Path changedFile = event.getPath();
		Path referenceBase = new File(reference.getPath()).toPath();
		Path relative = referenceBase.relativize(changedFile);
		
		int currentChangeConverted = convert(event.getKind());
		
		if( changes.get(relative) == null ) {
			changes.put(relative, currentChangeConverted);
		} else {
			// Ok, this file has already been changed... ugh
			int existingChange = changes.get(relative);
			if( existingChange == DELETED) {
				if( currentChangeConverted != DELETED ) {
					changes.put(relative, MODIFIED);
				}
			} else if( existingChange == CREATED ) {
				if( currentChangeConverted == DELETED ) {
					changes.remove(relative);
				}
			} else if( existingChange == MODIFIED ) {
				if( currentChangeConverted == DELETED ) {
					changes.put(relative, DELETED);
				}
			}
		}
	}
	
	public void clearDelta() {
		changes.clear();
	}
	
	private int convert(WatchEvent.Kind<?> kind) {
		if( kind == StandardWatchEventKinds.ENTRY_CREATE)
			return CREATED;
		if( kind == StandardWatchEventKinds.ENTRY_MODIFY)
			return MODIFIED;
		if( kind == StandardWatchEventKinds.ENTRY_DELETE)
			return DELETED;
		return -1;
	}

}
