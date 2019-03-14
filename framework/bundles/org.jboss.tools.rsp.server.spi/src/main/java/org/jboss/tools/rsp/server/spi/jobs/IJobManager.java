/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.jobs;

import java.util.List;

import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.eclipse.core.runtime.IRunnableWithProgress;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.launching.utils.IStatusRunnableWithProgress;

public interface IJobManager {
	/**
	 * Add a job listener to this model
	 * @param l
	 */
	public void addJobListener(IJobListener l);
	
	/**
	 * Remove a job listener from this model
	 * @param l
	 */
	public void removeJobListener(IJobListener l);
	
	/**
	 * Add a job to this model
	 * @param jobName a name for the job (not a unique id)
	 * @param runnable a runnable that accepts a progress monitor
	 * @return job the created job, or null if a job with the same id already exists
	 */
	public IJob scheduleJob(String jobName, IRunnableWithProgress runnable);

	/**
	 * Add a job to this model
	 * @param jobName a name for the job (not a unique id)
	 * @param runnable a runnable that accepts a progress monitor and returns an IStatus object
	 * @return job the created job, or null if a job with the same id already exists
	 */
	public IJob scheduleJob(String jobName, IStatusRunnableWithProgress runnable);

	/**
	 * The job work pctg has changed
	 */
	public void jobWorkChanged(IJob job);
	
	/**
	 * Cancel a given job.
	 * 
	 * @param job
	 */
	public void cancel(IJob job);
	
	/**
	 * Shut down this job manager
	 */
	public void shutdown();
	
	/**
	 * Get a list of jobs currently registered in the model
	 * @return
	 */
	public List<IJob> getJobs();
	
	/**
	 * Cancel a given job
	 * @param job
	 * @return
	 */
	public IStatus cancelJob(JobHandle job);
}
