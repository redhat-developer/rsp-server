/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.discovery;

import java.io.File;
import java.util.HashMap;

import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.server.minishift.discovery.MinishiftVersionLoader.MinishiftVersions;
import org.jboss.tools.rsp.server.minishift.servertype.impl.MinishiftServerTypes;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class MinishiftBeanType extends ServerBeanType {
	private HashMap<String,MinishiftVersions> versionsCache = new HashMap<>();
	
	
	protected MinishiftBeanType() {
		super("MINISHIFT", "Minishift 1.12+");
	}

	public MinishiftBeanType(String id, String name) {
		super(id, name);
	}

	@Override
	public boolean isServerRoot(File location) {
		if( location.isFile()) {
			return getFullVersion(location) != null;
		}
		MinishiftDiscovery disc = new MinishiftDiscovery();
		if( disc.folderContainsMinishiftBinary(location)) {
			File bin = disc.getMinishiftBinaryFromFolder(location);
			return getFullVersion(bin) != null;
		}
		return false;
	}

	@Override
	public String getFullVersion(File root) {
		return getFullVersion(findOrLoad(root));
	}

	protected String getFullVersion(MinishiftVersions props) {
		if( props != null ) {
			if( isSupported(props) && props.getMinishiftVersion() != null ) {
				return props.getMinishiftVersion();
			}
		}
		return null;
	}

	private MinishiftVersions findOrLoad(File root) {
		String path = root.getAbsolutePath();
		MinishiftVersions props = null;
		if( versionsCache.get(path) != null ) {
			props = versionsCache.get(path);
		} else {
			MinishiftDiscovery disc = new MinishiftDiscovery();
			if(disc.isMinishiftBinaryFile(root)) {
				props = MinishiftVersionLoader.getVersionProperties(root.getAbsolutePath());
				if( props != null ) {
					versionsCache.put(path, props);
				}
			}
		}
		return props;
	}

	@Override
	public String getUnderlyingTypeId(File root) {
		MinishiftVersions props = findOrLoad(root);
		if( props != null ) {
			if( props.getCDKVersion() != null ) {
				return "CDK";
			}
		}
		return "MINISHIFT";
	}

	@Override
	public String getServerAdapterTypeId(String version) {
		return MinishiftServerTypes.MINISHIFT_1_12_ID;
	}
	
	// TODO if the user put a folder here, find the right path
	public ServerBean createServerBean(File rootLocation) {
		if( rootLocation.isDirectory()) {
			MinishiftDiscovery disc = new MinishiftDiscovery();
			if( disc.folderContainsMinishiftBinary(rootLocation)) {
				rootLocation = disc.getMinishiftBinaryFromFolder(rootLocation);
			}
		}
		MinishiftVersions props = findOrLoad(rootLocation);
		String version = getFullVersion(props);
		if( version != null ) {
			ServerBean server = new ServerBean(
					rootLocation.getPath(), getServerBeanName(rootLocation),
					getId(), getUnderlyingTypeId(rootLocation), version, 
					getMajorMinorVersion(version), getServerAdapterTypeId(version));
			return server;
		}
		return null;
	}
	
	protected boolean isSupported(MinishiftVersions vers) {
		return vers.getCDKVersion() == null;
	}
}