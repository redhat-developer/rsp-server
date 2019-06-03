/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class UpdateServerResponse {
	private ServerHandle handle;
	private CreateServerResponse validation;
	public UpdateServerResponse() {
		validation = new CreateServerResponse();
	}
	public ServerHandle getHandle() {
		return handle;
	}
	public void setHandle(ServerHandle handle) {
		this.handle = handle;
	}
	public CreateServerResponse getValidation() {
		return validation;
	}
	public void setValidation(CreateServerResponse validation) {
		this.validation = validation;
	}

}
