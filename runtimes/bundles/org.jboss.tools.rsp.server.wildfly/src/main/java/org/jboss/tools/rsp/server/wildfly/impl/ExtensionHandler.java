/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.impl;

import java.util.Arrays;
import java.util.List;

import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.eclipse.core.runtime.MultiStatus;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.ServerModelListenerAdapter;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.runtimes.download.DownloadRuntimesProvider;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerTypes;

public class ExtensionHandler {

	private static final IServerType[] TYPES = {
			WildFlyServerTypes.WF17_SERVER_TYPE,
			WildFlyServerTypes.WF16_SERVER_TYPE,
			WildFlyServerTypes.WF15_SERVER_TYPE,
			WildFlyServerTypes.WF14_SERVER_TYPE,
			WildFlyServerTypes.WF13_SERVER_TYPE,
			WildFlyServerTypes.WF12_SERVER_TYPE,
			WildFlyServerTypes.WF11_SERVER_TYPE,
			WildFlyServerTypes.WF10_SERVER_TYPE,
			WildFlyServerTypes.WF9_SERVER_TYPE,
			WildFlyServerTypes.WF8_SERVER_TYPE,
			WildFlyServerTypes.WF71_SERVER_TYPE,
			WildFlyServerTypes.WF7_SERVER_TYPE,
			WildFlyServerTypes.AS6_SERVER_TYPE,
			WildFlyServerTypes.AS51_SERVER_TYPE,
			WildFlyServerTypes.AS5_SERVER_TYPE,
			WildFlyServerTypes.AS42_SERVER_TYPE,
			WildFlyServerTypes.AS4_SERVER_TYPE,
			WildFlyServerTypes.AS32_SERVER_TYPE,
			
			WildFlyServerTypes.EAP43_SERVER_TYPE,
			WildFlyServerTypes.EAP50_SERVER_TYPE,
			WildFlyServerTypes.EAP60_SERVER_TYPE,
			WildFlyServerTypes.EAP61_SERVER_TYPE,
			WildFlyServerTypes.EAP70_SERVER_TYPE,
			WildFlyServerTypes.EAP71_SERVER_TYPE,
			WildFlyServerTypes.EAP72_SERVER_TYPE,
	};

	private ExtensionHandler() {
		// inhibit instantiation
	}

	private static JBossServerBeanTypeProvider beanProvider = null;
	private static DownloadRuntimesProvider dlrtProvider = null;
	private static PostWildFlyCreationListener postWflyListener = null;
	public static void addExtensions(IServerManagementModel model) {
		beanProvider = new JBossServerBeanTypeProvider();
		dlrtProvider = new DownloadRuntimesProvider(model);
		postWflyListener = new PostWildFlyCreationListener(model);
		model.getServerBeanTypeManager().addTypeProvider(beanProvider);
		model.getServerModel().addServerTypes(TYPES);
		model.getDownloadRuntimeModel().addDownloadRuntimeProvider(dlrtProvider);
		model.getServerModel().addServerModelListener(postWflyListener);
	}
	
	public static void removeExtensions(IServerManagementModel model) {
		model.getServerBeanTypeManager().removeTypeProvider(beanProvider);
		model.getServerModel().removeServerTypes(TYPES);
		model.getDownloadRuntimeModel().removeDownloadRuntimeProvider(dlrtProvider);
		model.getServerModel().removeServerModelListener(postWflyListener);
	}
	
	private static class PostWildFlyCreationListener extends ServerModelListenerAdapter {

		private IServerManagementModel model;

		public PostWildFlyCreationListener(IServerManagementModel model) {
			this.model = model;
		}

		@Override
		public void serverAdded(ServerHandle server) {
			String typeId = server.getType().getId();
			List<String> allJbossWfly = Arrays.asList(IServerConstants.ALL_JBOSS_SERVERS);
			if( allJbossWfly.contains(typeId)) {
				IServer iserver = model.getServerModel().getServer(server.getId());
				AbstractJBossServerDelegate del = (AbstractJBossServerDelegate)iserver.getDelegate();
				if( del != null ) {
					MultiStatus ms = new MultiStatus(ServerCoreActivator.BUNDLE_ID, 0, 
							"Empty Status", null);
					del.updateDependentAttributes(iserver, ms);
				}
			}
		}
	}
}