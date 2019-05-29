/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.server.wildfly.test.defects;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeManager;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.IServerModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;

public abstract class AbstractMockServerTest {

	
	protected IServer createServer(String type) {
		IServer s = mock(IServer.class);
		IServerType st = mock(IServerType.class);
		doReturn(st).when(s).getServerType();
		doReturn(type).when(st).getId();
		doReturn(".").when(s).getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);

		IServerManagementModel managementModel = mockServerManagementModel();
		doReturn(managementModel).when(s).getServerManagementModel();
	
		IServerModel sm = mockServerModel();
		doReturn(sm).when(managementModel).getServerModel();
		return s;
	}

	protected IServerManagementModel mockServerManagementModel() {
		IServerManagementModel managementModel = mock(IServerManagementModel.class);		
		IServerBeanTypeManager beanTypeManager = mockServerBeanTypeManager();
		doReturn(beanTypeManager).when(managementModel).getServerBeanTypeManager();
		return managementModel;
	}

	protected IServerBeanTypeManager mockServerBeanTypeManager() {
		IServerBeanTypeManager beanTypeManager = mock(IServerBeanTypeManager.class);
		doReturn(new ServerBeanType[] {}).when(beanTypeManager).getAllRegisteredTypes();
		return beanTypeManager;
	}

	protected IServerModel mockServerModel() {
		IServerModel model = mock(IServerModel.class);
		return model;
	}

}
