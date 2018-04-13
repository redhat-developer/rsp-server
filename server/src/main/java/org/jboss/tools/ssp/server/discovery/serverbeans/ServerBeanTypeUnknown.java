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
package org.jboss.tools.ssp.server.discovery.serverbeans;

import java.io.File;

import org.jboss.tools.ssp.server.spi.discovery.ServerBeanType;

public class ServerBeanTypeUnknown extends ServerBeanType {

	protected ServerBeanTypeUnknown() {
		super( UNKNOWN_STR, UNKNOWN_STR);
	}

	@Override
	public boolean isServerRoot(File location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getFullVersion(File root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUnderlyingTypeId(File root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerAdapterTypeId(String version) {
		// TODO Auto-generated method stub
		return null;
	}

}
