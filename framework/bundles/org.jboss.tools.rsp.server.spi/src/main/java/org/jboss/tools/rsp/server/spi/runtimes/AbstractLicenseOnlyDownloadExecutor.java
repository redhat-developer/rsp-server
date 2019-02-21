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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeRunner;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowConstants;
import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.model.installer.RuntimesInstallerModel;
import org.jboss.tools.rsp.server.spi.SPIActivator;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.IServerModel;

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
	
	protected WorkflowResponse quickResponse(int sev, String msg, DownloadSingleRuntimeRequest req) {
		WorkflowResponse resp = new WorkflowResponse();
		resp.setStatus(new Status(sev,  SPIActivator.BUNDLE_ID, msg));
		if( req != null )
			resp.setRequestId(req.getRequestId());
		return resp;
		
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
	
	public WorkflowResponse executeDownload(DownloadSingleRuntimeRequest req) {
		// Now find an installer for this file type (jar, zip, runnable binary, etc)
		String installationMethod = dlrt.getInstallationMethod();
		installationMethod = (installationMethod == null ? IRuntimeInstaller.EXTRACT_INSTALLER : installationMethod);
		final IRuntimeInstaller installer = RuntimesInstallerModel.getDefault()
					.getRuntimeInstaller(installationMethod);
		if( installer == null ) {
			return quickResponse(IStatus.ERROR,  "No installer found for runtime "  + dlrt.getId(), req);
		}
		
		// Set up the folders where we'll store automatic downloads of runtimes
		File runtimes = new File(LaunchingCore.getDataLocation(), "runtimes");
		File downloads = new File(runtimes, "downloads");
		File installations = new File(runtimes, "installations");
		if( !runtimes.exists())
			runtimes.mkdirs();
		if( !downloads.exists()) 
			downloads.mkdirs();
		if( !installations.exists())
			installations.mkdirs();
		
		String uniqueName = getUniqueInstallLocation(dlrt.getId(), installations);
		File uniqueLoc = new File(installations, uniqueName);
		
		// Kick off a download in a new thread
		initiateDownload(req, dlrt, installer, uniqueLoc, downloads);
		
		// License is approved. Send a response about it.
		return quickResponse(IStatus.OK,  "Download In Progress", req);
	}
	
	private void initiateDownload(DownloadSingleRuntimeRequest req, DownloadRuntime dlrt, 
			IRuntimeInstaller installer, File uniqueLoc, File downloads) {
		new Thread("Download runtime: " + dlrt.getId()) {
			public void run() {
				TaskModel tm2 = createDownloadTaskModel(req);
				IStatus ret = installer.installRuntime(dlrt, uniqueLoc.getAbsolutePath(), downloads.getAbsolutePath(), 
						true, tm2, new NullProgressMonitor());
				String newHome = (String)tm2.getObject(IDownloadRuntimeWorkflowConstants.UNZIPPED_SERVER_HOME_DIRECTORY);
				if( ret.isOK()) {
					// Now it's downloaded, but, we should now maybe install it? Or add it as a server?
					createServer(dlrt, newHome);
				}
			}
		}.start();
	}
	
	protected TaskModel createDownloadTaskModel(DownloadSingleRuntimeRequest req) {
		return new TaskModel();
	}
	
	protected abstract void createServer(DownloadRuntime dlrt, String newHome);

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

		item1.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_NONE);
		try {
			item1.setContent(dlrt.getLicense(new NullProgressMonitor()));
		} catch(CoreException ce) {
			item1.setContent("Error loading license text.");
		}

		WorkflowResponseItem item1a = new WorkflowResponseItem();
		item1a.setId(ServerManagementAPIConstants.WORKFLOW_LICENSE_URL_ID);
		item1a.setLabel("License URL: ");

		item1a.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_NONE);
		item1a.setContent(dlrt.getLicenseURL());

		WorkflowResponseItem item2 = new WorkflowResponseItem();
		item2.setId(ServerManagementAPIConstants.WORKFLOW_LICENSE_SIGN_ID);
		item2.setLabel("Do you agree to the license?");
		item2.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_BOOL);

		items.add(item1);
		items.add(item1a);
		items.add(item2);
		resp.setItems(items);
		resp.setRequestId(id);
		resp.setStatus(new Status(IStatus.INFO, SPIActivator.BUNDLE_ID, "Please fill the requried information"));
		return resp;
	}

}
