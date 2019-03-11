package org.jboss.tools.rsp.server.spi.jobs;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IRunnableWithProgress;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.utils.IStatusRunnableWithProgress;
import org.jboss.tools.rsp.server.spi.SPIActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleJob implements IJob {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleJob.class);

	private String name;
	private String id;
	private IRunnableWithProgress runnable;
	private IStatusRunnableWithProgress statusRunnable;
	private IProgressMonitor monitor = null;
	
	public SimpleJob(String name, String id, IRunnableWithProgress runnable) {
		this.name = name;
		this.id = id;
		this.runnable = runnable;
	}

	public SimpleJob(String name, String id, IStatusRunnableWithProgress statusRunnable) {
		this.name = name;
		this.id = id;
		this.statusRunnable = statusRunnable;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public double getProgress() {
		return -1;
	}
	
	@Override
	public synchronized IProgressMonitor getProgressMonitor() {
		if( monitor == null ) {
			monitor = new NullProgressMonitor(); // TODO make a real impl
		}
		return monitor;
	}
	
	public IStatus run() {
		if( runnable != null ) {
			try {
				runnable.run(getProgressMonitor());
				return Status.OK_STATUS;
			} catch(Exception e) {
				LOG.error(e.getMessage(), e);
				return new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, e.getMessage());
			}
		} else if( statusRunnable != null ) {
			try {
				return statusRunnable.run(getProgressMonitor());
			} catch(Exception e) {
				LOG.error(e.getMessage(), e);
				return new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, e.getMessage());
			}
		}
		// Should not happen
		return new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, "No runnable found");
	}
}
