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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeConnectionFactory;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowConstants;
import org.jboss.tools.rsp.server.spi.SPIActivator;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractLicenseOnlyDownloadExecutor;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractStacksDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.runtimes.download.DownloadManagerStateSingleton.DownloadManagerRequestState;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadManagerDownloadExecutor extends AbstractLicenseOnlyDownloadExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(DownloadManagerDownloadExecutor.class);

	private static int STEP_CREDENTIALS = 1;
	private static int STEP_TC = 2;
	private static int STEP_LICENSE = 3;
	
	private static final String KEY_INTERNAL_CREDENTIAL_VALIDATION = "internal.credential.validation";
	
	public DownloadManagerDownloadExecutor(DownloadRuntime dlrt, IServerManagementModel model) {
		super(dlrt, model);
	}

	@Override
	public WorkflowResponse execute(DownloadSingleRuntimeRequest req) {
		if( req == null || getRuntime() == null) {
			return quickResponse(IStatus.ERROR, "No runtime found for id=null", req);
		}
		DownloadManagerRequestState state = null;
		if (req.getRequestId() != 0) {
			state = DownloadManagerStateSingleton.getDefault().getState(req.getRequestId());
		}

		if (req.getRequestId() == 0 || state == null) {
			WorkflowResponse ret = requestCredentials();
			DownloadManagerStateSingleton.getDefault().updateRequestState(
					ret.getRequestId(), STEP_CREDENTIALS, new HashMap<String, Object>());
			return ret;
		}
		
		if (state.getWorkflowStep() == STEP_CREDENTIALS) {
			WorkflowResponse response = handleCredentials(req);
			if (response != null)
				return response;
		}
		
		if (state.getWorkflowStep() == STEP_TC) {
			WorkflowResponse response = handleTC(req);
			if (response != null)
				return response;
		}

		if (state.getWorkflowStep() == STEP_LICENSE) {
			WorkflowResponse response = handleLicense(req);
			if (response != null)
				return response;
		}

		// Everything good
		return executeDownload(req);
	}

	private WorkflowResponse handleLicense(DownloadSingleRuntimeRequest req) {
		Map<String, Object> data = req.getData();
		Object d1 = data == null ? null : data.get(ServerManagementAPIConstants.WORKFLOW_LICENSE_SIGN_ID);
		boolean approved = Boolean.TRUE.equals(d1);
		if (!approved) {
			return quickResponse(IStatus.CANCEL,  "License not approved", req);
		}
		
		return null;
	}

	private WorkflowResponse handleTC(DownloadSingleRuntimeRequest req) {
		Map<String, Object> data = DownloadManagerStateSingleton.getDefault().getState(req.getRequestId()).getData();
		Object workflowStep = data.get(KEY_INTERNAL_CREDENTIAL_VALIDATION);
		if (workflowStep == null) {
			return quickResponse(IStatus.ERROR, "Workflow Error", req);
		}
		if (!(workflowStep instanceof Integer)) {
			return quickResponse(IStatus.ERROR, "Workflow Error", req);
		}
		if (((Integer) workflowStep).intValue() == DownloadManagerWorkflowUtility.WORKFLOW_FAILED) {
			String rtUrl = getRuntime().getUrl();
			String msg = NLS.bind("You have not yet signed the Terms and Conditions of the 0-dollar Subscription. Please go to {0} to accept and begin your download.", rtUrl);;
			return quickResponse(IStatus.ERROR, msg, req);
		}
		
		// We have valid credentials. 
		DownloadManagerStateSingleton.getDefault().updateRequestState(
				req.getRequestId(), STEP_LICENSE, req.getData());
		return licenseWorkflowResponse(req);
	}
	
	private WorkflowResponse handleCredentials(DownloadSingleRuntimeRequest req) {
		int existingStep = 	DownloadManagerStateSingleton.getDefault().getState(req.getRequestId()).getWorkflowStep();
		
		// Update model with new values from user
		DownloadManagerStateSingleton.getDefault().updateRequestState(
				req.getRequestId(), existingStep, req.getData());
		
		// We're in the handle-credential step. They should actually be sending me credentials
		if (req.getData().get(ServerManagementAPIConstants.WORKFLOW_USERNAME_ID) == null
				&& req.getData().get(ServerManagementAPIConstants.WORKFLOW_PASSWORD_ID) == null) {
			return quickResponse(IStatus.ERROR, "Canceled by user", req);
		}
		
		// we have a request id. This means they're at SOME step in the process. 
		String user = (String) DownloadManagerStateSingleton.getDefault().getState(req.getRequestId())
				.getData().get(ServerManagementAPIConstants.WORKFLOW_USERNAME_ID);
		String pass = (String) DownloadManagerStateSingleton.getDefault().getState(req.getRequestId())
				.getData().get(ServerManagementAPIConstants.WORKFLOW_PASSWORD_ID);
		if (user == null) {
			return requestCredentials("Username cannot be null. ", req.getRequestId());
		}
		if (pass == null) {
			return requestCredentials("Password cannot be null. ", req.getRequestId());
		}

		int credentialState = -1;
		try {
			credentialState = DownloadManagerWorkflowUtility.getWorkflowStatus(getRuntime(), user, pass);
		} catch (CoreException | IOException e) {
			return requestCredentials("Error while validating credentials: " + e.getMessage() + ". ", req.getRequestId());
		}
		
		boolean valid = isValidCredentials(credentialState);
		if (!valid) {
			WorkflowResponse retry = requestCredentials("Your credentials have failed. ", req.getRequestId());
			return retry;
		}
		// We have valid credentials. 
		DownloadManagerStateSingleton.getDefault().updateRequestState(
				req.getRequestId(), STEP_TC, req.getData());
		HashMap<String, Object> credentialStateData = new HashMap<>();
		credentialStateData.put(KEY_INTERNAL_CREDENTIAL_VALIDATION, credentialState);
		DownloadManagerStateSingleton.getDefault().updateRequestState(
				req.getRequestId(), STEP_TC, credentialStateData);
		return null;
	}
	
	private boolean isValidCredentials(int credentialState) {
		return credentialState == DownloadManagerWorkflowUtility.AUTHORIZED 
				|| credentialState == DownloadManagerWorkflowUtility.WORKFLOW_FAILED;
	}

	private WorkflowResponse requestCredentials() {
		return requestCredentials("", -1);
	}	

	private WorkflowResponse requestCredentials(String prefix, long requestId) {
		requestId = ensureRequestId(requestId);
		WorkflowResponse resp = new WorkflowResponse();
		WorkflowResponseItem item1 = createWorkflowItem(
				"downloadmanager.credentials.label", 
				prefix + "Please provide your Red Hat credentials:", 
				ServerManagementAPIConstants.ATTR_TYPE_NONE);

		WorkflowResponseItem item2 = createWorkflowItem(
				ServerManagementAPIConstants.WORKFLOW_USERNAME_ID,
				"Username: ",
				ServerManagementAPIConstants.ATTR_TYPE_STRING);

		WorkflowResponseItem item3 = createWorkflowItem(
				ServerManagementAPIConstants.WORKFLOW_PASSWORD_ID,
				"Password: ",
				ServerManagementAPIConstants.ATTR_TYPE_STRING);
		List<WorkflowResponseItem> items = Arrays.asList(item1, item2, item3);
		resp.setItems(items);
		resp.setRequestId(requestId);
		resp.setStatus(new Status(IStatus.INFO, SPIActivator.BUNDLE_ID, "Please fill the requried information"));
		return resp;
	}

	private long ensureRequestId(long requestId) {
		if (requestId == -1) {
			// New request, return what we need
			requestId = (long) ((Math.random() * ((100000 - 10) + 1)) + 10);
		}
		return requestId;
	}

	private WorkflowResponseItem createWorkflowItem(String id, String label, String responseType) {
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setId(id);
		item1.setLabel(label);
		item1.setResponseType(responseType);
		return item1;
	}
	
	@Override
	protected IStatus createServer(DownloadRuntime dlrt, String newHome) {
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
		
		Map<String,Object> attributes = new HashMap<>();
		attributes.put(IJBossServerAttributes.SERVER_HOME, newHome);
		CreateServerResponse response = getServerModel().createServer(serverType, chosenId, attributes);
		return StatusConverter.convert(response.getStatus());
	}

	@Override 
	protected TaskModel createDownloadTaskModel(DownloadSingleRuntimeRequest req) {
		TaskModel tm = new TaskModel();
		String user = (String) DownloadManagerStateSingleton.getDefault().getState(req.getRequestId())
				.getData().get(ServerManagementAPIConstants.WORKFLOW_USERNAME_ID);
		String pass = (String) DownloadManagerStateSingleton.getDefault().getState(req.getRequestId())
				.getData().get(ServerManagementAPIConstants.WORKFLOW_PASSWORD_ID);
		tm.putObject(IDownloadRuntimeWorkflowConstants.USERNAME_KEY, user);
		tm.putObject(IDownloadRuntimeWorkflowConstants.PASSWORD_KEY, pass);
		IDownloadRuntimeConnectionFactory fact = (url, username, password) -> {
			
				try {
					HttpURLConnection ret = DownloadManagerWorkflowUtility.getWorkflowConnection(url.toString(), username, password, "GET", true, true, 60*60*1000);
					return ret.getInputStream();
				} catch(IOException ioe) {
					LOG.error(MessageFormat.format("Could not get workflow connection for url {0}", url), ioe);
				}
				return null;
			};
		tm.putObject(IDownloadRuntimeWorkflowConstants.CONNECTION_FACTORY, fact);
		return tm;
	}
	
}
