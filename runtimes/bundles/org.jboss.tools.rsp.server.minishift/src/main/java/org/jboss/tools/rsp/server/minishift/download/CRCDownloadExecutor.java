/*************************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. and others.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.util.DownloadRuntimeSessionCache.DownloadManagerSessionState;
import org.jboss.tools.rsp.server.minishift.discovery.MinishiftDiscovery;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.minishift.servertype.impl.MinishiftServerTypes;
import org.jboss.tools.rsp.server.redhat.download.AbstractDownloadManagerExecutor;
import org.jboss.tools.rsp.server.redhat.download.stacks.AbstractStacksDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.spi.SPIActivator;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CRCDownloadExecutor extends AbstractDownloadManagerExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(CRCDownloadExecutor.class);

	protected static final int STEP_PULL_SECRET = 5;
	protected static final int STEP_DOWNLOAD = 6;

	public CRCDownloadExecutor(DownloadRuntime dlrt, IServerManagementModel model) {
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
		if( !binFile.setExecutable(true) ) {
			LOG.warn("Unable to set cdk binary to executable: " + binFile.getAbsolutePath());
		}
		attributes.put(ServerManagementAPIConstants.SERVER_HOME_FILE, binFile.getAbsolutePath());
		attributes.put(IMinishiftServerAttributes.CRC_IMAGE_PULL_SECRET, tm.getObject(IMinishiftServerAttributes.CRC_IMAGE_PULL_SECRET));
		
		CreateServerResponse response = getServerModel().createServer(serverType, chosenId, attributes);
		return StatusConverter.convert(response.getStatus());
	}
	
	@Override
	protected WorkflowResponse executeAdditionalSteps(DownloadSingleRuntimeRequest req) {
		// Superclass already handles license. If we get to here, 
		// we have already handled license and can move on
		// TODO delete this method
		return executeDownload(req);
	}
	private WorkflowResponse requestPullSecret(String prefix, long requestId) {
		requestId = ensureRequestId(requestId);
		WorkflowResponse resp = new WorkflowResponse();
		WorkflowResponseItem item1 = createWorkflowItem(
				IMinishiftServerAttributes.CRC_IMAGE_PULL_SECRET,
				prefix + "Pull Secret file: ",
				ServerManagementAPIConstants.ATTR_TYPE_STRING);

		List<WorkflowResponseItem> items = Arrays.asList(item1);
		resp.setItems(items);
		resp.setRequestId(requestId);
		Status s1 = new Status(IStatus.INFO, SPIActivator.BUNDLE_ID, "Please fill the requried information");
		resp.setStatus(StatusConverter.convert(s1));
		return resp;
	}
	
	protected WorkflowResponse handlePullSecret(DownloadSingleRuntimeRequest req) {
		Map<String, Object> data = req.getData();
		Object d1 = data == null ? null : data.get(ServerManagementAPIConstants.WORKFLOW_LICENSE_SIGN_ID);
		boolean approved = Boolean.TRUE.equals(d1);
		if (!approved) {
			return quickResponse(IStatus.CANCEL,  "License not approved", req);
		}
		
		return null;
	}

	@Override
	public WorkflowResponse execute(DownloadSingleRuntimeRequest req) {
		if( req == null || getRuntime() == null) {
			return quickResponse(IStatus.ERROR, "No runtime found for id=null", req);
		}

		if( req.getRequestId() == 0 ) {
			return licenseWorkflowResponse(req);
		}
		
		DownloadManagerSessionState state = SESSION_STATE.getState(req.getRequestId());
		if( state == null || state.getWorkflowStep() == STEP_LICENSE) {
			// License has been handled but not updated for next step
			WorkflowResponse response = handleLicense(req);
			if (response != null)
				return response;
			SESSION_STATE.updateRequestState(
					req.getRequestId(), STEP_PULL_SECRET, req.getData());
		}
		
		state = SESSION_STATE.getState(req.getRequestId());
		if( state.getWorkflowStep() == STEP_PULL_SECRET) {
			String pullSecFile = (String)req.getData().get(IMinishiftServerAttributes.CRC_IMAGE_PULL_SECRET);
			if( pullSecFile  == null ) {
				return requestPullSecret("", req.getRequestId());
			}
			if( pullSecFile == null || pullSecFile.isEmpty() || !(new File(pullSecFile).isFile())) {
				return requestPullSecret("Pull Secret file is invalid: ", req.getRequestId());
			}
		}
		
		SESSION_STATE.updateRequestState(
				req.getRequestId(), STEP_DOWNLOAD, req.getData());
		return executeDownload(req);
	}
	
	@Override
	protected TaskModel createDownloadTaskModel(DownloadSingleRuntimeRequest req) {
		TaskModel tm = new TaskModel();
		String key = IMinishiftServerAttributes.CRC_IMAGE_PULL_SECRET;
		tm.putObject(key, req.getData().get(key));
		return tm;
	}

}
