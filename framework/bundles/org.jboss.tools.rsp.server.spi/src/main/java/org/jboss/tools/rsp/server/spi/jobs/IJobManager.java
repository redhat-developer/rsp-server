package org.jboss.tools.rsp.server.spi.jobs;

import org.jboss.tools.rsp.eclipse.core.runtime.IRunnableWithProgress;
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
	 * @return job
	 */
	public IJob scheduleJob(String jobName, IRunnableWithProgress runnable);

	/**
	 * Add a job to this model
	 * @param jobName a name for the job (not a unique id)
	 * @param runnable a runnable that accepts a progress monitor and returns an IStatus object
	 * @return job
	 */
	public IJob scheduleJob(String jobName, IStatusRunnableWithProgress runnable);

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
}
