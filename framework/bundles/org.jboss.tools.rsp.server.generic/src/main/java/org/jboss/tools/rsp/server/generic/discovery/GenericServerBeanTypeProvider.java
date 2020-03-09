/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.discovery;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class GenericServerBeanTypeProvider implements IServerBeanTypeProvider{
	private ServerBeanType[] serverBeanTypes;
	public GenericServerBeanTypeProvider(ServerBeanType[] allTypes) {
		this.serverBeanTypes = allTypes;
	}
	
	@Override
	public ServerBeanType[] getServerBeanTypes() {
		return serverBeanTypes;
	}	

}
