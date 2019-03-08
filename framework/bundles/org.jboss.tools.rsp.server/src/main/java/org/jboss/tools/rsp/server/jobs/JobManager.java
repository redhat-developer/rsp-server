package org.jboss.tools.rsp.server.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.server.spi.jobs.IJob;
import org.jboss.tools.rsp.server.spi.jobs.IJobListener;
import org.jboss.tools.rsp.server.spi.jobs.IJobManager;

public class JobManager implements IJobManager {

	private ArrayList<IJobListener> listeners = new ArrayList<>();
	private HashMap<String, IJob> currentJobs = new HashMap<>();
	
	
	@Override
	public void addJobListener(IJobListener l) {
		listeners.add(l);
	}

	@Override
	public void removeJobListener(IJobListener l) {
		listeners.remove(l);
	}

	@Override
	public void addJob(IJob job) {
		currentJobs.put(job.getId(), job);
		ArrayList<IJobListener> tmp = new ArrayList<>(listeners);
		for( IJobListener l : tmp ) {
			l.jobAdded(job);
		}
	}

	@Override
	public void removeJob(IJob job, IStatus status) {
		currentJobs.remove(job.getId());
		ArrayList<IJobListener> tmp = new ArrayList<>(listeners);
		for( IJobListener l : tmp ) {
			l.jobRemoved(job, status);
		}
	}

	@Override
	public void progressChanged(IJob job, double work) {
		ArrayList<IJobListener> tmp = new ArrayList<>(listeners);
		for( IJobListener l : tmp ) {
			l.progressChanged(job, work);
		}
	}
	
	@Override
	public String generateJobId() {
		return UUID.randomUUID().toString();
	}

}
