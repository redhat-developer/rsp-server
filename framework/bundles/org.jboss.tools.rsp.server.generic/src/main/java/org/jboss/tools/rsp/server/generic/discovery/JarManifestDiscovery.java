/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.discovery;

import java.io.File;

import org.jboss.tools.rsp.server.generic.discovery.internal.ManifestUtility;

public class JarManifestDiscovery extends ExplodedManifestDiscovery {

	public JarManifestDiscovery(String id, String name, String serverAdapterTypeId, String nameFileString,
			boolean nameFileStringIsPattern, String nameKey, String requiredNamePrefix, String versionFileString,
			boolean versionFileStringIsPattern, String versionKey, String requiredVersionPrefix) {
		super(id, name, serverAdapterTypeId, nameFileString, nameFileStringIsPattern, nameKey, requiredNamePrefix,
				versionFileString, versionFileStringIsPattern, versionKey, requiredVersionPrefix);
	}
	@Override
	protected String getProperty(File f, String key) {
		return ManifestUtility.getManifestPropertyFromZip(f, key);
	}
}
