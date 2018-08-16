/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.discovery.serverbeans;

import java.util.ArrayList;
import java.util.Arrays;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeManager;
import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class ServerBeanTypeManager implements IServerBeanTypeManager {
	private ArrayList<IServerBeanTypeProvider> typeProviders;
	
	public ServerBeanTypeManager() {
		typeProviders = new ArrayList<IServerBeanTypeProvider>();
	}
	
	public void addTypeProvider(IServerBeanTypeProvider provider) {
		typeProviders.add(provider);
	}
	
	public ServerBeanType[] getAllRegisteredTypes() {
		ArrayList<ServerBeanType> ret = new ArrayList<ServerBeanType>();
		for( IServerBeanTypeProvider prov : typeProviders) {
			ret.addAll(Arrays.asList(prov.getServerBeanTypes()));
		}
		return (ServerBeanType[]) ret.toArray(new ServerBeanType[ret.size()]);
	}
}
