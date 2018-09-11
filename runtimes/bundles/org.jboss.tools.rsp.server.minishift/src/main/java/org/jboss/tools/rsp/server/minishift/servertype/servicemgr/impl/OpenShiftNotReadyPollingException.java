package org.jboss.tools.rsp.server.minishift.servertype.servicemgr.impl;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.server.spi.model.polling.PollingException;

public class OpenShiftNotReadyPollingException extends PollingException {
	public static final int OPENSHIFT_UNREACHABLE_CODE = 10001;
	private transient IStatus stat;

	public OpenShiftNotReadyPollingException(IStatus status) {
		super(status.getMessage());
		this.stat = status;
	}

	public IStatus getStatus() {
		return stat;
	}
}