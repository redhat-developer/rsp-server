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
package org.jboss.tools.rsp.server.spi.runtimes;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.WorkflowPromptDetails;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.SubMonitor;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.launching.utils.IStatusRunnableWithProgress;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeRunner;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowConstants;
import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.server.spi.SPIActivator;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;
import org.jboss.tools.rsp.server.spi.client.MessageContextStore.MessageContext;
import org.jboss.tools.rsp.server.spi.jobs.IJob;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.IServerModel;
import org.jboss.tools.rsp.server.spi.util.WorkflowUtility;

/*
 * A class for simple download-runtimes that require only a license agreement
 */
public abstract class AbstractLicenseOnlyDownloadExecutor implements IDownloadRuntimeRunner {

	private DownloadRuntime dlrt;
	private IServerManagementModel model;
	public AbstractLicenseOnlyDownloadExecutor(DownloadRuntime dlrt, IServerManagementModel model) {
		this.dlrt = dlrt;
		this.model = model;
	}
	
	protected DownloadRuntime getRuntime() {
		return dlrt;
	}
	
	public static WorkflowResponse quickResponse(int sev, String msg, DownloadSingleRuntimeRequest req) {
		return quickResponse(sev, msg, req, null);
	}
	public static WorkflowResponse quickResponse(int sev, String msg, 
			DownloadSingleRuntimeRequest req, Throwable t) {
		return WorkflowUtility.quickResponse(sev, msg, req.getRequestId(), t);
	}
	
	@Override
	public WorkflowResponse execute(DownloadSingleRuntimeRequest req) {
		if( req == null || dlrt == null) {
			return quickResponse(IStatus.ERROR, "No runtime found for id=null", req);
		}
		
		if( req.getRequestId() == 0 ) {
			return licenseWorkflowResponse(req);
		}
		
		Map<String, Object> data = req.getData();
		Object d1 = data == null ? null : data.get(ServerManagementAPIConstants.WORKFLOW_LICENSE_SIGN_ID);
		boolean approved = Boolean.TRUE.equals(d1);
		if( !approved ) {
			return quickResponse(IStatus.CANCEL,  "License not approved", req);
		}
		return executeDownload(req);
	}
	
	/**
	 * Execute the download of a given download-runtime, and, once complete, 
	 * attempt to configure a server out of whatever was downloaded. 
	 * 
	 * @param req
	 * @return
	 */
	protected WorkflowResponse executeDownload(DownloadSingleRuntimeRequest req) {
		// Now find an installer for this file type (jar, zip, runnable binary, etc)
		String installationMethod = dlrt.getInstallationMethod();
		installationMethod = (installationMethod == null ? IRuntimeInstaller.EXTRACT_INSTALLER : installationMethod);
		final IRuntimeInstaller installer = model.getDownloadRuntimeModel().getRuntimeInstaller(installationMethod);
		if( installer == null ) {
			return quickResponse(IStatus.ERROR,  "No installer found for runtime "  + dlrt.getId(), req);
		}
		
		// Set up the folders where we'll store automatic downloads of runtimes
		File runtimes = getDownloadRuntimesRootFolder();
		File downloads = new File(runtimes, "downloads");
		File installations = new File(runtimes, "installations");
		if( !runtimes.exists())
			runtimes.mkdirs();
		if( !downloads.exists()) 
			downloads.mkdirs();
		if( !installations.exists())
			installations.mkdirs();
		
		// Kick off a download in a new thread
		String jobId = initiateDownloadAndCreateServer(req, dlrt, installer, installations, downloads);
		
		// License is approved. Send a response about it.
		WorkflowResponse rsp = quickResponse(IStatus.OK,  "Download In Progress", req);
		rsp.setJobId(jobId);
		return rsp;
	}
	
	private File getDownloadRuntimesRootFolder() {
		File f = model.getDataStoreModel().getDataLocation();
		return new File(f, "runtimes");
	}
	
	/*
	 * Initiate the download, and return the job id
	 */
	private String initiateDownloadAndCreateServer(DownloadSingleRuntimeRequest req, 
			DownloadRuntime dlrt, IRuntimeInstaller installer, File installations, File downloads) {
		String jobName = "Download runtime: " + dlrt.getName();
		final MessageContext<RSPClient> client = ClientThreadLocal.getStore().getContext();
		IStatusRunnableWithProgress task = new IStatusRunnableWithProgress() {
			
			@Override
			public IStatus run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String uniqueName = getUniqueInstallLocation(dlrt.getId(), installations);
				File uniqueLoc = new File(installations, uniqueName);
				
				
				final MessageContext<RSPClient> prev = ClientThreadLocal.getStore().getContext();
				ClientThreadLocal.getStore().setContext(client);
				// TODO, implement a progress monitor that can update progress
				SubMonitor sub = SubMonitor.convert(monitor, 100);
				
				TaskModel tm2 = createDownloadTaskModel(req);
				String sSize = dlrt.getSize();
				if(sSize != null && !sSize.isEmpty() && !sSize.equals(DownloadRuntime.SIZE_UNKNOWN)) {
					try {
						long l = Long.parseLong(sSize);
						tm2.putObject(IDownloadRuntimeWorkflowConstants.DL_RUNTIME_SIZE, l);
					} catch(NumberFormatException nfe) {
						// Ignore
					}
				}

				IStatus ret = installer.installRuntime(dlrt, uniqueLoc.getAbsolutePath(), downloads.getAbsolutePath(), 
						true, tm2, sub.split(90));
				if( !ret.isOK()) {
					return ret;
				}
				// Now it's downloaded, but, we should now maybe install it? Or add it as a server?
				String newHome = (String)tm2.getObject(IDownloadRuntimeWorkflowConstants.UNZIPPED_SERVER_HOME_DIRECTORY);
				IStatus complete = createServer(dlrt, newHome, tm2);
				ClientThreadLocal.getStore().setContext(prev);
				return complete;
			}
		};
		
		IJob job = model.getJobManager().scheduleJob(jobName, task);
		return job.getId();
	}
	
	protected TaskModel createDownloadTaskModel(DownloadSingleRuntimeRequest req) {
		return new TaskModel();
	}
	
	protected abstract IStatus createServer(DownloadRuntime dlrt, String newHome, TaskModel tm);

	protected IServerModel getServerModel() {
		return model.getServerModel();
	}
	
	protected String getUniqueServerId(String desired, Set<String> existing) {
		String ret = desired;
		int count = 1;
		while(existing.contains(ret)) {
			ret = desired + "_" + count++;
		}
		return ret;
	}
	
	private String getUniqueInstallLocation(String dlrtType, File installations) {
		String ret = dlrtType;
		int count = 1;
		while(new File(installations, ret).exists()) {
			ret = dlrtType + "_" + count++;
		}
		return ret;
	}
	
	protected long getOrGenerateRequestId(DownloadSingleRuntimeRequest request) {
		if( request != null && request.getRequestId() > 0 )
			return request.getRequestId();
		return (long) ((Math.random() * ((100000 - 10) + 1)) + 10);
	}
	protected WorkflowResponse licenseWorkflowResponse(DownloadSingleRuntimeRequest request) {
		// New request, return what we need
		long id = getOrGenerateRequestId(request);
		WorkflowResponse resp = new WorkflowResponse();
		List<WorkflowResponseItem> items = new ArrayList<>();
		
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setId(ServerManagementAPIConstants.WORKFLOW_LICENSE_TEXT_ID);
		item1.setLabel("Please approve the following license:");
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_LARGE);
		try {
			item1.setContent(dlrt.getLicense(new NullProgressMonitor()));
		} catch(CoreException ce) {
			item1.setContent("Error loading license text.");
		}
		WorkflowPromptDetails det = new WorkflowPromptDetails();
		item1.setPrompt(det);
		det.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_NONE);
		

		WorkflowResponseItem item1a = new WorkflowResponseItem();
		item1a.setId(ServerManagementAPIConstants.WORKFLOW_LICENSE_URL_ID);
		item1a.setLabel("License URL: ");
		item1a.setContent(dlrt.getLicenseURL());
		HashMap<String,String> props = new HashMap<String,String>();
		props.put(ServerManagementAPIConstants.WORKFLOW_ITEM_STRING_PROPERTY_LINK_URL, dlrt.getLicenseURL());
		item1a.setProperties(props);
		det = new WorkflowPromptDetails();
		item1a.setPrompt(det);
		det.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_NONE);

		WorkflowResponseItem item2 = new WorkflowResponseItem();
		item2.setId(ServerManagementAPIConstants.WORKFLOW_LICENSE_SIGN_ID);
		item2.setLabel("Do you agree to the license?");
		det = new WorkflowPromptDetails();
		item2.setPrompt(det);
		det.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_BOOL);

		items.add(item1);
		items.add(item1a);
		items.add(item2);
		resp.setItems(items);
		resp.setRequestId(id);
		resp.setStatus(statusDao(IStatus.INFO, SPIActivator.BUNDLE_ID, "Please fill the requried information"));
		return resp;
	}

	protected org.jboss.tools.rsp.api.dao.Status statusDao(int sev, String bundle, String msg) {
		return new org.jboss.tools.rsp.api.dao.Status(sev, bundle, msg);
	}

	protected WorkflowResponse convertAttributes(String serverTypeId, DownloadSingleRuntimeRequest req, List<String> ignored) {
		List<WorkflowResponseItem> items = new ArrayList<>();
		Attributes reqAttrs = getServerModel().getRequiredAttributes(getServerModel().getIServerType(serverTypeId));
		for( String k : reqAttrs.getAttributes().keySet()) {
			if( !ignored.contains(k)) {
				Attribute v = reqAttrs.getAttributes().get(k);
				items.add(createWorkflowItem(k, v.getDescription(), v.getType(), v.isSecret()));
			}
		}

		Attributes optAttrs = getServerModel().getOptionalAttributes(getServerModel().getIServerType(serverTypeId));
		for( String k : optAttrs.getAttributes().keySet()) {
			if( !ignored.contains(k)) {
				Attribute v = optAttrs.getAttributes().get(k);
				WorkflowResponseItem item = createWorkflowItem(k, v.getDescription(), v.getType(), v.isSecret()); 
				items.add(item);
			}
		}
		
		WorkflowResponse resp = new WorkflowResponse();
		resp.setItems(items);
		resp.setRequestId(req.getRequestId());
		resp.setStatus(statusDao(IStatus.INFO, SPIActivator.BUNDLE_ID, "Please fill the requried information"));
		return resp;
	}
	
	protected WorkflowResponseItem createWorkflowItem(String id, String label, String responseType) {
		return createWorkflowItem(id, label, responseType, false);
	}

	protected WorkflowResponseItem createWorkflowItem(String id, String label, 
			String responseType, boolean secret) {
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setId(id);
		item1.setLabel(label);
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL);
		WorkflowPromptDetails prompt = new WorkflowPromptDetails();
		item1.setPrompt(prompt);
		prompt.setResponseType(responseType);
		prompt.setResponseSecret(secret);
		return item1;
	}


}
