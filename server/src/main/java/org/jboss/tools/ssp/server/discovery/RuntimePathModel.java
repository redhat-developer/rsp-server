package org.jboss.tools.ssp.server.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.tools.ssp.api.DiscoveryPath;

public class RuntimePathModel {
	public List<DiscoveryPath> paths;
	// TODO persistence? 
	public RuntimePathModel() {
		paths = new ArrayList<DiscoveryPath>();
	}
	
	public List<DiscoveryPath> getPaths() {
		return Collections.unmodifiableList(paths);
	}
	
	public void addPath(DiscoveryPath path) {
		if( !paths.contains(path)) {
			paths.add(path);
		}
	}
	
	public void removePath(DiscoveryPath path) {
		paths.remove(path);
	}
}
