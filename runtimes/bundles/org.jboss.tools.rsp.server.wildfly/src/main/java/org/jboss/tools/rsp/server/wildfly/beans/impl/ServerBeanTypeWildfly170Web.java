/******************************************************************************* 
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.beans.impl;

import java.io.File;

public class ServerBeanTypeWildfly170Web extends JBossServerBeanType {
	public ServerBeanTypeWildfly170Web() {
		super(ID_WILDFLY_WEB, NAME_WILDFLY,
				AS7_MODULE_LAYERED_SERVER_MAIN);
	}
	
	protected String getServerTypeBaseName() {
		return getId();
	}

	@Override
	public String getFullVersion(File location, File systemFile) {
		return ServerBeanTypeWildfly90Web.getFullVersion(location, systemFile, "17.");
	}

	
	public boolean isServerRoot(File location) {
		return getFullVersion(location, null) != null;
	}
	
	public String getServerAdapterTypeId(String version) {	
		return IServerConstants.SERVER_WILDFLY_170;
	}
}
