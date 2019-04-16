package org.jboss.tools.rsp.server.spi.servertype;

import java.nio.file.Path;
import java.util.Map;

public interface IDeployableResourceDelta {

	public static final int CREATED = 1;
	public static final int MODIFIED = 2;
	public static final int DELETED = 3;
	
	/**
	 * Get a map of changed resources within this deployable.
	 * The key is a relative path to the deployment's source directory.
	 * The value is one of CREATED, MODIFIED, or DELETED. 
	 * @return
	 */
	public Map<Path, Integer> getResourceDeltaMap();

}
