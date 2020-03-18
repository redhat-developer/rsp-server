/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.wildfly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.api.dao.JobProgress;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.itests.RSPCase;
import org.jboss.tools.rsp.itests.util.RSPServerUtility;
import org.jboss.tools.rsp.itests.util.WaitCondition;
import org.junit.Test;

public class WildflyJobsTest extends RSPCase {
	
	private static final String WILDFLY_RUNTIME_ID = "wildfly-1800finalruntime";
	private static final String JOB_NOT_FOUND = "Job not found: ";
	private static final String JOB_ID = "xxx-777";
	private static final String JOB_HANDLE_NULL = "Job handle cannot be null";
	private static final String WORKFLOW_LICENSE_URL = "workflow.license.url";
	private static final String WORKFLOW_LICENSE_SIGN = "workflow.license.sign";
	private static final String DOWNLOAD_IN_PROGRESS = "Download In Progress";
	
	@Test
	public void testGetJobsEmpty() throws InterruptedException, ExecutionException, TimeoutException {
		List<JobProgress> jobs = serverProxy.getJobs().get();
        assertTrue(jobs != null && jobs.isEmpty());
        jobs = serverProxy.getJobs().get();
        assertNotNull(jobs);
	}
	
	@Test
	public void testInvalidCancelJob() throws InterruptedException, ExecutionException, TimeoutException {
		JobHandle jobHandle = new JobHandle("foo.handle", JOB_ID);
		Status status = serverProxy.cancelJob(jobHandle).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, status.getSeverity());
		assertEquals(JOB_NOT_FOUND + JOB_ID, status.getMessage());
	}
	
	@Test
	public void testNullCancelJob() throws InterruptedException, ExecutionException, TimeoutException {
		Status status = serverProxy.cancelJob(null).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.ERROR, status.getSeverity());
		assertEquals(JOB_HANDLE_NULL, status.getMessage());
	}
	
	@Test
	public void testGetAndCancelJob() throws InterruptedException, ExecutionException, TimeoutException {
		// workaround
		ListDownloadRuntimeResponse listResponse = serverProxy.listDownloadableRuntimes().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertTrue(listResponse.getRuntimes().size() > 0);
		// prepare request for downloading specific runtime
		DownloadSingleRuntimeRequest request = new DownloadSingleRuntimeRequest();
		request.setDownloadRuntimeId(WILDFLY_RUNTIME_ID);
		WorkflowResponse response = serverProxy.downloadRuntime(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertNotNull(response);
		assertEquals(Status.INFO, response.getStatus().getSeverity());
		// update request, that we confirmed license agreement
		HashMap<String, Object> data = new HashMap<>();
		data.put(WORKFLOW_LICENSE_URL, "Continue...");
		data.put(WORKFLOW_LICENSE_SIGN, true);
		request.setRequestId(response.getRequestId());
		request.setData(data);
		// start downloading
		WorkflowResponse responseDownloading = serverProxy.downloadRuntime(request).get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.OK, responseDownloading.getStatus().getSeverity());
		assertEquals(DOWNLOAD_IN_PROGRESS, responseDownloading.getStatus().getMessage());
		// download job id
		String downloadJobId = responseDownloading.getJobId();
		assertNotNull(downloadJobId);
		// list all running jobs on the rsp server
		// actual job progress (from getJobs) could be 0.0 as downloading takes some time to start
		List<JobProgress> jobs = serverProxy.getJobs().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertTrue(jobs != null && jobs.size() > 0);
		// verify that it is our downloading job
		JobProgress jobProgress = jobs.stream().filter(job -> job.getHandle().getId().equals(downloadJobId)).findAny().orElse(null);
		double actualProgress = jobProgress.getPercent();
		assertTrue(actualProgress >= 0.0);
		// check that there is progress in bits downloading. wait for a while
		sleep(5000);
		jobs = serverProxy.getJobs().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		jobProgress = jobs.stream().filter(job -> job.getHandle().getId().equals(downloadJobId)).findAny().orElse(null);
		assertTrue(jobProgress.getPercent() > 0.0 && jobProgress.getPercent() != actualProgress);
		// Cancel downloading
		Status cancelStatus = serverProxy.cancelJob(jobProgress.getHandle()).get(SERVER_OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
		assertEquals(Status.OK, cancelStatus.getSeverity());
		RSPServerUtility.waitFor(5000, new WaitCondition() {
			
			@Override
			public boolean test() {
				List<JobProgress> jobs = new ArrayList<>();
				try {
					jobs = serverProxy.getJobs().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					e.printStackTrace();
				}
				JobProgress jobProgress = jobs.stream().filter(job -> job.getHandle().getId().equals(downloadJobId)).findAny().orElse(null);
				return jobProgress == null ? true : false;	
			}
		});
		jobs = serverProxy.getJobs().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		assertTrue("There should be no job active: " + jobs.stream().map(mapper -> mapper.getHandle().getName()).collect(Collectors.joining(", ")), jobs != null && jobs.size() == 0);
	}
	
}
