package org.jboss.tools.rsp.server.spi.jobs;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;

public interface IJobListener {
	
	/**
	 * A long-running job has been added
	 * @param job
	 */
	public void jobAdded(IJob job);
	
	/**
	 * A long-running job has been removed
	 * @param job
	 * @param status
	 */
	public void jobRemoved(IJob job, IStatus status);
	
	/**
	 * The progress on this job has been changed
	 * 
	 * @param job  The job
	 * @param work A double between 0 and 100 indicating progress
	 */
	public void progressChanged(IJob job, double work);
	
}
