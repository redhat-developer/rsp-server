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
package org.jboss.tools.rsp.runtime.core.util;

import java.util.HashMap;
import java.util.Map;

public class DownloadRuntimeSessionCache   {

	private static DownloadRuntimeSessionCache instance = new DownloadRuntimeSessionCache();
	public static DownloadRuntimeSessionCache getDefault() {
		return instance;
	}
	
	private Map<Long, DownloadManagerSessionState> map = new HashMap<>();
	public DownloadManagerSessionState getState(long requestId) {
		return map.get(requestId);
	}
	
	public void updateRequestState(long requestId, int workflowStep, Map<String,Object> data) {
		DownloadManagerSessionState existing = map.get(requestId);
		if( existing == null ) {
			existing = new DownloadManagerSessionState();
			map.put(requestId,  existing);
			existing.setData(new HashMap<String, Object>());
		}
		existing.setWorkflowStep(workflowStep);
		existing.getData().putAll(data);
	}
	
	public static class DownloadManagerSessionState {
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
