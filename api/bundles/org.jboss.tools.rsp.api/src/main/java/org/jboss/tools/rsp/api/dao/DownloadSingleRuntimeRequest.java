/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.Map;

public class DownloadSingleRuntimeRequest {
	// null if it's a new request, or, a unique id referencing 
	// an existing request that required more workflow
	private long requestId;
	
	// A string representing the download runtime id
	private String downloadRuntimeId;
	
	private Map<String, Object> data;

	public DownloadSingleRuntimeRequest() {
		super();
	}
	
	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public String getDownloadRuntimeId() {
		return downloadRuntimeId;
	}

	public void setDownloadRuntimeId(String downloadRuntimeId) {
		this.downloadRuntimeId = downloadRuntimeId;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}
}
