/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.servicemgr.impl;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.server.spi.model.polling.PollingException;

public class OpenShiftNotReadyPollingException extends PollingException {

	public static final int OPENSHIFT_UNREACHABLE_CODE = 10001;
	private final transient IStatus stat;

	public OpenShiftNotReadyPollingException(IStatus status) {
		super(status.getMessage());
		this.stat = status;
	}

	public IStatus getStatus() {
		return stat;
	}
}