/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class JobProgress {
	private double percent;
	private JobHandle handle;

	public JobProgress() { 
		
	}
	public JobProgress( JobHandle handle, double percent) {
		this.handle = handle;
		this.percent = percent;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public JobHandle getHandle() {
		return handle;
	}

	public void setHandle(JobHandle handle) {
		this.handle = handle;
	}
}
