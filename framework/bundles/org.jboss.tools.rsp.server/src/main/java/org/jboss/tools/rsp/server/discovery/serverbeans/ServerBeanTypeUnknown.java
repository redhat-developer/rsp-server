/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.discovery.serverbeans;

import java.io.File;

import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class ServerBeanTypeUnknown extends ServerBeanType {

	protected ServerBeanTypeUnknown() {
		super( UNKNOWN_STR, UNKNOWN_STR);
	}

	@Override
	public boolean isServerRoot(File location) {
		return false;
	}

	@Override
	public String getFullVersion(File root) {
		return null;
	}

	@Override
	public String getUnderlyingTypeId(File root) {
		return null;
	}

	@Override
	public String getServerAdapterTypeId(String version) {
		return null;
	}

}
