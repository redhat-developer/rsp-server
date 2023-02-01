/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.jobs;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IRunnableWithProgress;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.utils.IStatusRunnableWithProgress;
import org.jboss.tools.rsp.launching.utils.SimpleProgressMonitor;
import org.jboss.tools.rsp.server.spi.SPIActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleJob implements IJob {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleJob.class);

	private String name;
	private String id;
	private IRunnableWithProgress runnable;
	private IStatusRunnableWithProgress statusRunnable;
	private JobProgressMonitor monitor = null;
	private IJobManager manager;
	
	public SimpleJob(String name, String id, IRunnableWithProgress runnable, IJobManager manager) {
		this(name, id, manager);
		this.runnable = runnable;
	}

	public SimpleJob(String name, String id, IStatusRunnableWithProgress statusRunnable, IJobManager manager) {
		this(name, id, manager);
		this.statusRunnable = statusRunnable;
	}

	private SimpleJob(String name, String id, IJobManager manager) {
		this.name = name;
		this.id = id;
		this.manager = manager;
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
		return ((JobProgressMonitor)getProgressMonitor()).getPercentage();
	}
	
	public synchronized IProgressMonitor getProgressMonitor() {
		if( monitor == null ) {
			monitor = new JobProgressMonitor(); 
		}
		return monitor;
	}
	
	private class JobProgressMonitor extends SimpleProgressMonitor {
		public void worked(int work) {
			double d = getPercentage();
			super.worked(work);
			double d2 = getPercentage();
			// Don't fire events if the old and new pctg is exactly the same
			if( d2 > d )
				manager.jobWorkChanged(SimpleJob.this);
		}
		@Override
		public void done() {
			super.done();
			manager.jobWorkChanged(SimpleJob.this);
		}
	}
	
	public IStatus run() {
		if( runnable != null ) {
			try {
				runnable.run(getProgressMonitor());
				return Status.OK_STATUS;
			} catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
				LOG.error(ie.getMessage(), ie);
				return new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, ie.getMessage());
			} catch(Exception e) {
				LOG.error(e.getMessage(), e);
				return new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, e.getMessage());
			}
		} else if( statusRunnable != null ) {
			try {
				return statusRunnable.run(getProgressMonitor());
			} catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
				LOG.error(ie.getMessage(), ie);
				return new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, ie.getMessage());
			} catch(Exception e) {
				LOG.error(e.getMessage(), e);
				return new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, e.getMessage());
			}
		}
		// Should not happen
		return new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, "No runnable found");
	}

	@Override
	public IStatus cancel() {
		IProgressMonitor monitor = getProgressMonitor();
		if( monitor == null )
			return new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, "Unable to cancel job");
		monitor.setCanceled(true);
		return Status.OK_STATUS;
	}
}
