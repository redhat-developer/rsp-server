package org.jboss.tools.rsp.server.spi.jobs;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;

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
	 * @param job
	 */
	public void addJob(IJob job);
	
	/**
	 * Remove a job from this model. 
	 * The status object should indicate the reason for removal
	 *    (ie, complete (OK), errored, or canceled
	 *    
	 * @param job
	 * @param status
	 */
	public void removeJob(IJob job, IStatus status);
	
	/**
	 * Create a job ID for an extender to use to register a custom job
	 * @return
	 */
	public String generateJobId();

	/**
	 * Update the job manager with the progress of a given job
	 * and fire the events to all listeners
	 * 
	 * @param job
	 * @param work
	 */
	void progressChanged(IJob job, double work);
}
