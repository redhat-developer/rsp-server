/*************************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.server.wildfly.runtimes.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractStacksDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;

public class EAPDownloadExecutor extends AbstractDownloadManagerExecutor {

	public EAPDownloadExecutor(DownloadRuntime dlrt, IServerManagementModel model) {
		super(dlrt, model);
	}

	@Override
	protected IStatus createServer(DownloadRuntime dlrt, String newHome, TaskModel tm) {
		// duplicate with the wildfly impl

		// The wtp-runtime id is used in stacks.yaml,
		String wtpRuntimeId = dlrt.getProperty(AbstractStacksDownloadRuntimesProvider.PROP_WTP_RUNTIME);

		// but rsp-server doesn't really have a server / runtime split.
		// So now we need to get the rsp-server server type id
		String serverType = IServerConstants.RUNTIME_TO_SERVER.get(wtpRuntimeId);

		// Now we have to somehow create this thing... ... ...
		Set<String> serverIds = getServerModel().getServers().keySet();
		String suggestedId = new File(newHome).getName();
		String chosenId = getUniqueServerId(suggestedId, serverIds);

		Map<String, Object> attributes = new HashMap<>();
		attributes.put(IJBossServerAttributes.SERVER_HOME, newHome);
		CreateServerResponse response = getServerModel().createServer(serverType, chosenId, attributes);
		return StatusConverter.convert(response.getStatus());
	}
}
