package org.jboss.tools.rsp.server.spi.util;

import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.SPIActivator;

public class WorkflowUtility {

	public static WorkflowResponse quickResponse(int sev, String msg, long reqId) {
		return quickResponse(sev, msg, reqId, null);
	}
	public static WorkflowResponse quickResponse(int sev, String msg, long reqId, Throwable t) {
		WorkflowResponse resp = new WorkflowResponse();
		IStatus istat = new Status(sev, SPIActivator.BUNDLE_ID, msg, t);
		resp.setStatus(StatusConverter.convert(istat));
		if( reqId > 0 )
			resp.setRequestId(reqId);
		return resp;
		
	}
}
