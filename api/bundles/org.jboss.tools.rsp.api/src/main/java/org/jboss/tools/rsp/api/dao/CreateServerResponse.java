/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.ArrayList;
import java.util.List;

public class CreateServerResponse {
	private Status status;
	private List<String> invalidKeys;

	public CreateServerResponse() {
	}

	public CreateServerResponse(Status status, List<String> invalidKeys) {
		this.status = status;
		this.invalidKeys = (invalidKeys == null ? new ArrayList<String>() : invalidKeys);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<String> getInvalidKeys() {
		return invalidKeys;
	}

	public void setInvalidKeys(List<String> invalidKeys) {
		this.invalidKeys = invalidKeys;
	}
}
