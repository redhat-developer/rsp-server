/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class JobRemoved {
	private Status status;
	private JobHandle handle;

	public JobRemoved() {
		
	}
	public JobRemoved( JobHandle handle, Status status) {
		this.handle = handle;
		this.status = status;
	}

	public JobHandle getHandle() {
		return handle;
	}

	public void setHandle(JobHandle handle) {
		this.handle = handle;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
