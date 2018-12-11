package org.jboss.tools.rsp.server.wildfly.runtimes.download;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeWorkflowExecutor;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;

public class LicenseOnlyDownloadExecutor implements IDownloadRuntimeWorkflowExecutor {

	private DownloadRuntime dlrt;
	public LicenseOnlyDownloadExecutor(DownloadRuntime dlrt) {
		this.dlrt = dlrt;
	}
	
	@Override
	public WorkflowResponse execute(DownloadSingleRuntimeRequest req) {
		if( req == null || dlrt == null) {
			WorkflowResponse resp = new WorkflowResponse();
			resp.setStatus(new Status(IStatus.ERROR, 
					Activator.BUNDLE_ID, "No runtime found for id=null"));
			if( req != null )
				resp.setRequestId(req.getRequestId());
			return resp;
		}
		
		if( req.getRequestId() == 0 ) {
			try {
				return responseForNewRequest();
			} catch(CoreException ce) {
				WorkflowResponse resp = new WorkflowResponse();
				resp.setStatus(StatusConverter.convert(ce.getStatus()));
				resp.setRequestId(req.getRequestId());
				return resp;
			}
		}
		
		Map<String, Object> data = req.getData();
		Object d1 = data == null ? null : data.get(ServerManagementAPIConstants.WORKFLOW_LICENSE_SIGN_ID);
		boolean approved = Boolean.TRUE.equals(d1);
		if( !approved ) {
			WorkflowResponse resp = new WorkflowResponse();
			resp.setStatus(new Status(IStatus.CANCEL, 
					Activator.BUNDLE_ID, "License not approved"));
			resp.setRequestId(req.getRequestId());
			return resp;
		}
		
		// License is approved. Send a response about it.
		WorkflowResponse resp = new WorkflowResponse();
		resp.setStatus(new Status(IStatus.OK, 
				Activator.BUNDLE_ID, "Download In Progress"));
		resp.setRequestId(req.getRequestId());
		
		// TODO actually kick off the download
		// wtf do i do here? lol.  
		// DownloadRuntimeOperationUtility.downloadAndUnzip(etc)
		return resp;
	}

	private WorkflowResponse responseForNewRequest() throws CoreException {
		// New request, return what we need
		long id = (long) ((Math.random() * ((100000 - 10) + 1)) + 10);
		WorkflowResponse resp = new WorkflowResponse();
		List<WorkflowResponseItem> items = new ArrayList<>();
		
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setId(ServerManagementAPIConstants.WORKFLOW_LICENSE_TEXT_ID);
		item1.setLabel("Please approve the following license:");
		item1.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_NONE);
		item1.setContent(dlrt.getLicense(new NullProgressMonitor()));

		WorkflowResponseItem item2 = new WorkflowResponseItem();
		item2.setId(ServerManagementAPIConstants.WORKFLOW_LICENSE_SIGN_ID);
		item2.setLabel("Do you agree to the license?");
		item2.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_BOOL);

		items.add(item1);
		items.add(item2);
		resp.setItems(items);
		resp.setRequestId(id);
		resp.setStatus(new Status(IStatus.INFO, Activator.BUNDLE_ID, "Please fill the requried information"));
		return resp;
	}

}
