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

public class ServerBeanTypeSOA6 extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeSOA6() {
		super("SOA", "JBoss Enterprise SOA Platform", AS7_MODULE_LAYERED_SERVER_MAIN);
	}
	public String getServerAdapterTypeId(String version) {
		return IServerConstants.SERVER_EAP_61;
	}
	@Override
	public boolean isServerRoot(File location) {
		return "soa".equals(getSlot(location)) && super.isServerRoot(location);
	}
}
