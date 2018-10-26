/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.servertype;

import java.util.List;

import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.launching.utils.StatusConverter;

public class CreateServerValidation {
	private IStatus status;
	private List<String> failedKeys;
	public CreateServerValidation() {
	}
	public CreateServerValidation(IStatus status, List<String> failedKeys) {
		this.status = status;
		this.failedKeys = failedKeys;
		
	}
	public IStatus getStatus() {
		return status;
	}
	public void setStatus(IStatus status) {
		this.status = status;
	}
	public List<String> getFailedKeys() {
		return failedKeys;
	}
	public void setFailedKeys(List<String> failedKeys) {
		this.failedKeys = failedKeys;
	}

	public CreateServerResponse toDao() {
		return new CreateServerResponse(StatusConverter.convert(status), failedKeys);
	}
}
