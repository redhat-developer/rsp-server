/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.discovery;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class MinishiftBeanTypeProvider implements IServerBeanTypeProvider {

	ServerBeanType minishift12PlusType = new MinishiftBeanType();
	ServerBeanType cdk3xType = new CDKBeanType();
	
	@Override
	public ServerBeanType[] getServerBeanTypes() {
		return new ServerBeanType[] {minishift12PlusType, cdk3xType};
	}
}
