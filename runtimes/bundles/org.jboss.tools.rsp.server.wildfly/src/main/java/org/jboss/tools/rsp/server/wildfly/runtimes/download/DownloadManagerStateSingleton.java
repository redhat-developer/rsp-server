/*************************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.rsp.server.wildfly.runtimes.download;

import java.util.HashMap;
import java.util.Map;

public class DownloadManagerStateSingleton   {

	private static DownloadManagerStateSingleton instance = new DownloadManagerStateSingleton();
	public static DownloadManagerStateSingleton getDefault() {
		return instance;
	}
	
	private Map<Long, DownloadManagerRequestState> map = new HashMap<>();
	public DownloadManagerRequestState getState(long requestId) {
		return map.get(requestId);
	}
	
	public void updateRequestState(long requestId, int workflowStep, Map<String,Object> data) {
		DownloadManagerRequestState existing = map.get(requestId);
		if( existing == null ) {
			existing = new DownloadManagerRequestState();
			map.put(requestId,  existing);
			existing.setData(new HashMap<String, Object>());
		}
		existing.setWorkflowStep(workflowStep);
		existing.getData().putAll(data);
	}
	
	static class DownloadManagerRequestState {
		private int workflowStep;
		private Map<String, Object> data;
		public int getWorkflowStep() {
			return workflowStep;
		}
		public void setWorkflowStep(int workflowStep) {
			this.workflowStep = workflowStep;
		}
		public Map<String, Object> getData() {
			return data;
		}
		public void setData(Map<String, Object> data) {
			this.data = data;
		}
		
	}

}
