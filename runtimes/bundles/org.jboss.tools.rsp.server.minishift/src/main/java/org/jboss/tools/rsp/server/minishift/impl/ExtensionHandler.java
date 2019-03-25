/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.impl;

import org.jboss.tools.rsp.server.minishift.discovery.MinishiftBeanTypeProvider;
import org.jboss.tools.rsp.server.minishift.download.MinishiftCdkDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.minishift.servertype.impl.MinishiftServerTypes;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class ExtensionHandler {

	private static final IServerType[] TYPES = {
			MinishiftServerTypes.MINISHIFT_1_12_SERVER_TYPE,
			MinishiftServerTypes.CDK_3X_SERVER_TYPE
	};

	private ExtensionHandler() {
		// inhibit instantionation
	}
	
	private static MinishiftBeanTypeProvider beanProvider = null;
	private static MinishiftCdkDownloadRuntimesProvider dlrtProvider = null;

	public static void addExtensions(IServerManagementModel model) {
		beanProvider = new MinishiftBeanTypeProvider();
		dlrtProvider = new MinishiftCdkDownloadRuntimesProvider(model);
		
		model.getServerBeanTypeManager().addTypeProvider(beanProvider);
		model.getServerModel().addServerTypes(TYPES);
		model.getDownloadRuntimeModel().addDownloadRuntimeProvider(dlrtProvider);
	}
	
	public static void removeExtensions(IServerManagementModel model) {
		model.getServerBeanTypeManager().removeTypeProvider(new MinishiftBeanTypeProvider());
		model.getServerModel().removeServerTypes(TYPES);
		model.getDownloadRuntimeModel().removeDownloadRuntimeProvider(dlrtProvider);
	}
}
