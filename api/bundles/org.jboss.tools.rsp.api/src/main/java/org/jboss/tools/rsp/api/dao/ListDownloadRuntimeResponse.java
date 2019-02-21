/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.List;

public class ListDownloadRuntimeResponse {
	private List<DownloadRuntimeDescription> runtimes;
	public ListDownloadRuntimeResponse() {
		super();
	}
	public List<DownloadRuntimeDescription> getRuntimes() {
		return runtimes;
	}
	public void setRuntimes(List<DownloadRuntimeDescription> runtimes) {
		this.runtimes = runtimes;
	}
}
