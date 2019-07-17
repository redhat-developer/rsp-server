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
package org.jboss.tools.rsp.server.redhat.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
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
import org.jboss.tools.rsp.runtime.core.util.DownloadRuntimeSessionCache;
import org.jboss.tools.rsp.runtime.core.util.DownloadRuntimeSessionCache.DownloadManagerSessionState;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.server.redhat.credentials.RedHatAccessCredentials;
import org.jboss.tools.rsp.server.spi.SPIActivator;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractLicenseOnlyDownloadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDownloadManagerExecutor 
	extends AbstractLicenseOnlyDownloadExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractDownloadManagerExecutor.class);
	protected static final DownloadRuntimeSessionCache SESSION_STATE = new DownloadRuntimeSessionCache();
	
	
	protected static final int STEP_CREDENTIALS = 1;
	protected static final int STEP_TC = 2;
	protected static final int STEP_LICENSE = 3;
	
	protected static final String KEY_INTERNAL_CREDENTIAL_VALIDATION = "internal.credential.validation";
	
	public AbstractDownloadManagerExecutor(DownloadRuntime dlrt, IServerManagementModel model) {
		super(dlrt, model);
	}

	@Override
	public WorkflowResponse execute(DownloadSingleRuntimeRequest req) {
		try {
			return executeInternal(req);
		} catch(Exception t ) {
			LOG.error("Error handling download request", t);
			return null;
		}
	}
	
	private WorkflowResponse executeInternal(DownloadSingleRuntimeRequest req) {
		if( req == null || getRuntime() == null) {
			return quickResponse(IStatus.ERROR, "No runtime found for id=null", req);
		}
		DownloadManagerSessionState state = null;
		if (req.getRequestId() != 0) {
			state = SESSION_STATE.getState(req.getRequestId());
		}

		boolean nullState = (req.getRequestId() == 0 || state == null);
		// First step, see whether we need credentials
		if( nullState ) {
			if( requiresCredentials() ) {
				// We do. So ask the user for them.
				WorkflowResponse ret = requestCredentials();
				SESSION_STATE.updateRequestState(
						ret.getRequestId(), STEP_CREDENTIALS, new HashMap<String, Object>());
				return ret;
			} else {
				// The user hasn't been prompted for credentials, but 
				// they exist in the global model. Therefore, we create an 
				// on-the-fly request id for this request and update the 
				// state to indicate it has passed the request-credentials step
				long requestId = ensureRequestId(-1);
				SESSION_STATE.updateRequestState(
						requestId, STEP_CREDENTIALS, new HashMap<String, Object>());
				req.setRequestId(requestId);
				state = SESSION_STATE.getState(req.getRequestId());
			}
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

		// Everything good. Let subclasses override what else they want
		return executeAdditionalSteps(req);
	}
	

	protected boolean requiresCredentials() {
		return !isRedHatPasswordSet() || !isRedHatUsernameSet();
	}
	
	protected boolean isRedHatUsernameSet() {
		return getRedHatUsername()  == null ? false : true;
	}
	
	protected String getRedHatUsername() {
		ISecureStorageProvider storage = getServerModel().getSecureStorageProvider();
		if( storage != null ) {
			return RedHatAccessCredentials.getGlobalRedhatUser(storage);
		}
		return null;
	}
	
	protected boolean isRedHatPasswordSet() {
		return getRedHatPassword() == null ? false : true;
	}
	
	protected String getRedHatPassword() {
		ISecureStorageProvider storage = getServerModel().getSecureStorageProvider();
		if( storage != null ) {
			return RedHatAccessCredentials.getGlobalRedhatPassword(storage);
		}
		return null;
	}
	
	protected void setRedHatPassword(String password) {
		ISecureStorageProvider storage = getServerModel().getSecureStorageProvider();
		if( storage != null ) {
			RedHatAccessCredentials.setGlobalRedhatPassword(storage, password);
		}
	}

	protected void setRedHatUser(String username) {
		ISecureStorageProvider storage = getServerModel().getSecureStorageProvider();
		if( storage != null ) {
			RedHatAccessCredentials.setGlobalRedhatUser(storage, username);
		}
	}

	/*
	 * Subclasses to override if necessary
	 */
	protected WorkflowResponse executeAdditionalSteps(DownloadSingleRuntimeRequest req) {
		return executeDownload(req);
	}

	protected WorkflowResponse handleLicense(DownloadSingleRuntimeRequest req) {
		Map<String, Object> data = req.getData();
		Object d1 = data == null ? null : data.get(ServerManagementAPIConstants.WORKFLOW_LICENSE_SIGN_ID);
		boolean approved = Boolean.TRUE.equals(d1);
		if (!approved) {
			return quickResponse(IStatus.CANCEL,  "License not approved", req);
		}
		
		return null;
	}

	protected WorkflowResponse handleTC(DownloadSingleRuntimeRequest req) {
		Map<String, Object> data = SESSION_STATE.getState(req.getRequestId()).getData();
		Object workflowStep = data.get(KEY_INTERNAL_CREDENTIAL_VALIDATION);
		if (workflowStep == null) {
			return quickResponse(IStatus.ERROR, "Workflow Error", req);
		}
		if (!(workflowStep instanceof Integer)) {
			return quickResponse(IStatus.ERROR, "Workflow Error", req);
		}
		if (((Integer) workflowStep).intValue() == DownloadManagerWorkflowUtility.WORKFLOW_FAILED) {
			String rtUrl = getRuntime().getUrl();
			String msg = NLS.bind("You have not yet signed the Terms and Conditions of the 0-dollar Subscription. "
					+ "Please go to {0} to accept and begin your download.", rtUrl);
			return quickResponse(IStatus.ERROR, msg, req);
		}
		
		// We have valid credentials. 
		SESSION_STATE.updateRequestState(
				req.getRequestId(), STEP_LICENSE, req.getData());
		return licenseWorkflowResponse(req);
	}
	
	protected WorkflowResponse handleCredentials(DownloadSingleRuntimeRequest req) {
		setCredentialsInWorkflowOrStorage(req);
		
		int existingStep = 	SESSION_STATE.getState(req.getRequestId()).getWorkflowStep();
		
		// Update model with new values from user
		SESSION_STATE.updateRequestState(
				req.getRequestId(), existingStep, req.getData());
		
		String submittedUser = (String)req.getData().get(ServerManagementAPIConstants.WORKFLOW_USERNAME_ID);
		String submittedPassword = (String)req.getData().get(ServerManagementAPIConstants.WORKFLOW_PASSWORD_ID);
		
		
		// We're in the handle-credential step. They should actually be sending me credentials
		if (submittedUser == null || submittedPassword == null) {
			return quickResponse(IStatus.ERROR, "Canceled by user", req);
		}
		
		// we have a request id. This means they're at SOME step in the process. 
		String user = (String) SESSION_STATE.getState(req.getRequestId())
				.getData().get(ServerManagementAPIConstants.WORKFLOW_USERNAME_ID);
		String pass = (String) SESSION_STATE.getState(req.getRequestId())
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
		SESSION_STATE.updateRequestState(
				req.getRequestId(), STEP_TC, req.getData());
		HashMap<String, Object> credentialStateData = new HashMap<>();
		credentialStateData.put(KEY_INTERNAL_CREDENTIAL_VALIDATION, credentialState);
		SESSION_STATE.updateRequestState(
				req.getRequestId(), STEP_TC, credentialStateData);
		return null;
	}
	
	
	/*
	 * This odd method is used to handle the situation where either a user has no 
	 * global credentials saved and has set credentials on this request, or, the situation
	 * where the user has not included credentials in this request but they already exist
	 * in the global model. 
	 * 
	 * In the first case, the credentials used for this download are persisted
	 * in the global model for future use. 
	 * 
	 * In the second case, the credentials currently in secure storage will be 
	 * placed into this workflow to be used rather than prompt the user.
	 */
	private void setCredentialsInWorkflowOrStorage(DownloadSingleRuntimeRequest req) {
		Map<String, Object> map = req.getData();
		if( map == null ) {
			map = new HashMap<String,Object>();
			req.setData(map);
		}
		String submittedUser = (String)map.get(ServerManagementAPIConstants.WORKFLOW_USERNAME_ID);
		String submittedPassword = (String)map.get(ServerManagementAPIConstants.WORKFLOW_PASSWORD_ID);

		if( !requiresCredentials() && 
				(submittedUser == null || submittedPassword == null) ) {
			// Credentials were not required bc secure storage has them.
			// User put in no values for user/pass, so load the ones from secure storage
			if( submittedUser == null || submittedUser.isEmpty()) {
				req.getData().put(ServerManagementAPIConstants.WORKFLOW_USERNAME_ID,
						getRedHatUsername());
			}

			if( submittedPassword == null || submittedPassword.isEmpty()) {
				req.getData().put(ServerManagementAPIConstants.WORKFLOW_PASSWORD_ID,
						getRedHatPassword());
			}
		} else if( requiresCredentials() && 
				(submittedUser != null && submittedPassword != null
				&& !submittedUser.isEmpty() && !submittedPassword.isEmpty())) { 
			// Credentials were required, did not exist in secure storage
			// User inputted non-null values, so lets persist them to secure storage
			setRedHatUser(submittedUser);
			setRedHatPassword(submittedPassword);
		}
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
				ServerManagementAPIConstants.ATTR_TYPE_STRING, true);
		List<WorkflowResponseItem> items = Arrays.asList(item1, item2, item3);
		resp.setItems(items);
		resp.setRequestId(requestId);
		resp.setStatus(new Status(IStatus.INFO, SPIActivator.BUNDLE_ID, "Please fill the requried information"));
		return resp;
	}

	protected long ensureRequestId(long requestId) {
		if (requestId == -1) {
			// New request, return what we need
			requestId = (long) ((Math.random() * ((100000 - 10) + 1)) + 10);
		}
		return requestId;
	}

	@Override 
	protected TaskModel createDownloadTaskModel(DownloadSingleRuntimeRequest req) {
		TaskModel tm = new TaskModel();
		String user = (String) SESSION_STATE.getState(req.getRequestId())
				.getData().get(ServerManagementAPIConstants.WORKFLOW_USERNAME_ID);
		String pass = (String) SESSION_STATE.getState(req.getRequestId())
				.getData().get(ServerManagementAPIConstants.WORKFLOW_PASSWORD_ID);
		tm.putObject(IDownloadRuntimeWorkflowConstants.USERNAME_KEY, user);
		tm.putObject(IDownloadRuntimeWorkflowConstants.PASSWORD_KEY, pass);
		
		// Also add any other flags set via the state
		for( String k : SESSION_STATE.getState(req.getRequestId()).getData().keySet()) {
			tm.putObject(k, SESSION_STATE.getState(req.getRequestId()).getData().get(k));
		}
		
		IDownloadRuntimeConnectionFactory fact = new IDownloadRuntimeConnectionFactory() {
			
			@Override
			public int getContentLength(URL url, String user, String pass) {
				HttpURLConnection ret = null;
				try {
					ret = DownloadManagerWorkflowUtility.getWorkflowConnection(url.toString(), user, pass, "GET", true, true, 60*60*1000);
					return ret.getContentLength();
				} catch(IOException ioe) {
					LOG.error(MessageFormat.format("Could not get workflow connection for url {0}", url), ioe);
				}
				return -1;
			}
			
			@Override
			public InputStream createConnection(URL url, String user, String pass) {
				try {
					HttpURLConnection ret = DownloadManagerWorkflowUtility.getWorkflowConnection(url.toString(), user, pass, "GET", true, true, 60*60*1000);
					return ret.getInputStream();
				} catch(IOException ioe) {
					LOG.error(MessageFormat.format("Could not get workflow connection for url {0}", url), ioe);
				}
				return null;
			}
		};
		
		tm.putObject(IDownloadRuntimeWorkflowConstants.CONNECTION_FACTORY, fact);
		return tm;
	}

	@Override
	protected abstract IStatus createServer(DownloadRuntime dlrt, String newHome, TaskModel tm);
	
	

}
