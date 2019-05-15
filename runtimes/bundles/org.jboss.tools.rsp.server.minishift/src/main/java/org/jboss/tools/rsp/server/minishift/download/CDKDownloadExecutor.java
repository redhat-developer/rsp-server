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
package org.jboss.tools.rsp.server.minishift.download;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowConstants;
import org.jboss.tools.rsp.runtime.core.util.DownloadRuntimeSessionCache.DownloadManagerSessionState;
import org.jboss.tools.rsp.server.minishift.discovery.MinishiftDiscovery;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.minishift.servertype.impl.MinishiftServerTypes;
import org.jboss.tools.rsp.server.redhat.download.AbstractDownloadManagerExecutor;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractStacksDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class CDKDownloadExecutor extends AbstractDownloadManagerExecutor {

	protected static final int STEP_ATTRIBUTES = 4;
	protected static final int STEP_DOWNLOAD = 5;

	public CDKDownloadExecutor(DownloadRuntime dlrt, IServerManagementModel model) {
		super(dlrt, model);
	}

	@Override
	protected IStatus createServer(DownloadRuntime dlrt, String newHome, TaskModel tm) {
		// The wtp-runtime id is used in stacks.yaml, 
		String wtpRuntimeId = dlrt.getProperty(AbstractStacksDownloadRuntimesProvider.PROP_WTP_RUNTIME);
		
		// but rsp-server doesn't really have a server / runtime split. 
		// So now we need to get the rsp-server server type id
		String serverType = MinishiftServerTypes.RUNTIME_TO_SERVER.get(wtpRuntimeId);
		
		// Now we have to somehow create this thing... ... ... 
		Set<String> serverIds = getServerModel().getServers().keySet();
		String suggestedId = new File(newHome).getName();
		String chosenId = getUniqueServerId(suggestedId, serverIds);
		
		Map<String,Object> attributes = new HashMap<>();
		File binFile = new MinishiftDiscovery().getMinishiftBinaryFromFolder(new File(newHome), false);
		if( binFile == null ) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Unable to locate minishift binary");
		}
		binFile.setExecutable(true);
		attributes.put(ServerManagementAPIConstants.SERVER_HOME_FILE, binFile.getAbsolutePath());
		
		// A list of keys we should copy over IF the user contributed them
		List<String> serverKeys = new ArrayList<>();
		serverKeys.addAll(getServerModel().getRequiredAttributes(getServerModel().getIServerType(serverType)).getAttributes().keySet());
		serverKeys.addAll(getServerModel().getOptionalAttributes(getServerModel().getIServerType(serverType)).getAttributes().keySet());
		serverKeys.remove(ServerManagementAPIConstants.SERVER_HOME_FILE);
		
		for( String k1 : serverKeys ) {
			if( tm.getObject(k1) != null && !(tm.getObject(k1) instanceof String && ((String)tm.getObject(k1)).isEmpty())) {
				attributes.put(k1, tm.getObject(k1));
			}
		}
		
		attributes.put(IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, 
				(String)tm.getObject(IDownloadRuntimeWorkflowConstants.USERNAME_KEY));
		attributes.put(IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD, 
				(String)tm.getObject(IDownloadRuntimeWorkflowConstants.PASSWORD_KEY));
		
		CreateServerResponse response = getServerModel().createServer(serverType, chosenId, attributes);
		return StatusConverter.convert(response.getStatus());
	}

	@Override
	protected WorkflowResponse handleLicense(DownloadSingleRuntimeRequest req) {
		WorkflowResponse r = super.handleLicense(req);
		if( r == null ) {
			SESSION_STATE.updateRequestState(
					req.getRequestId(), STEP_ATTRIBUTES, req.getData());
		}
		return r;

	}
	
	@Override
	protected WorkflowResponse executeAdditionalSteps(DownloadSingleRuntimeRequest req) {
		DownloadManagerSessionState state = null;
		if (req.getRequestId() != 0) {
			state = SESSION_STATE.getState(req.getRequestId());
		}
		if (state.getWorkflowStep() == STEP_ATTRIBUTES) {
			String wtpRuntimeId = getRuntime().getProperty(AbstractStacksDownloadRuntimesProvider.PROP_WTP_RUNTIME);
			String serverType = MinishiftServerTypes.RUNTIME_TO_SERVER.get(wtpRuntimeId);
			List<String> list = Arrays.asList(
					IMinishiftServerAttributes.MINISHIFT_BINARY,
					IMinishiftServerAttributes.MINISHIFT_REG_USERNAME, 
					IMinishiftServerAttributes.MINISHIFT_REG_PASSWORD
			);
			WorkflowResponse resp = convertAttributes(serverType, req, list);
			SESSION_STATE.updateRequestState(
					req.getRequestId(), STEP_DOWNLOAD, req.getData());
			return resp;
		}
		
		SESSION_STATE.updateRequestState(
				req.getRequestId(), STEP_DOWNLOAD, req.getData());
		return executeDownload(req);
	}
}
