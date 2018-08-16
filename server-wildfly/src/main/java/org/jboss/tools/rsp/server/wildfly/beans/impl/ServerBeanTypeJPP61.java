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
package org.jboss.tools.rsp.server.wildfly.beans.impl;

import java.io.File;

/**
 * @since 3.0  Actually 2.4.101
 */
public class ServerBeanTypeJPP61 extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeJPP61() {
		super("JPP", "JBoss Portal Platform", AS7_MODULE_LAYERED_SERVER_MAIN);
	}

	protected String getServerTypeBaseName() {
		return "JBoss Portal";
	}
	
	@Override
	public boolean isServerRoot(File location) {
		if( "JPP".equalsIgnoreCase(getSlot(location))) {
			String v = getFullVersion(location, null);
			return v != null && v.startsWith("6.") && !v.startsWith("6.0");
		}
		return false;
	}
}
