package org.jboss.tools.rsp.server.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.utils.IStatusRunnableWithProgress;
import org.jboss.tools.rsp.server.spi.jobs.IJob;
import org.jboss.tools.rsp.server.spi.jobs.IJobListener;
import org.junit.Test;

public class JobManagerTest {
	private static class JobListenerAdapter implements IJobListener { 
		@Override
		public void jobAdded(IJob job) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void jobRemoved(IJob job, IStatus status) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void progressChanged(IJob job, double work) {
			// TODO Auto-generated method stub
			
		}
	}
	

	@Test
	public void testBasicManagerFunctionsOkStatus() {
		testBasicManagerFunctions(Status.OK_STATUS);
	}
	@Test
	public void testBasicManagerFunctionsCancelStatus() {
		testBasicManagerFunctions(Status.CANCEL_STATUS);
	}
	
	@Test
	public void testManagerFunctionsExceptionJob() {
		IStatusRunnableWithProgress srwp = new IStatusRunnableWithProgress() {
			@Override
			public IStatus run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				throw new InvocationTargetException(new Exception("Something Broke nested"), "Something Broke");
			}
		};
		IStatus exp = new Status(IStatus.ERROR, "test", "Something Broke");
		testBasicManagerFunctions(exp, srwp);
	}

	@Test
	public void testManagerFunctionsCanceledJob() {
		IStatus completionStatus = Status.CANCEL_STATUS;
		
		JobManager jm = new JobManager();
		CountDownLatch[] removedSignal1 = new CountDownLatch[1];
		CountDownLatch[] removedSignal2 = new CountDownLatch[1];
		CountDownLatch[] removedSignal3 = new CountDownLatch[1];
		removedSignal1[0] = new CountDownLatch(1);
		removedSignal2[0] = new CountDownLatch(1);
		removedSignal3[0] = new CountDownLatch(1);
		final IStatus[] jobRemovedCause = new IStatus[1];
		final IJob[] addedJobs = new IJob[1];
		jm.addJobListener(new JobListenerAdapter() {
			@Override
			public void jobAdded(IJob job) {
				addedJobs[0] = job;
			}

			@Override
			public void jobRemoved(IJob job, IStatus status) {
				try {
					removedSignal2[0].await();
				} catch(InterruptedException ie) {}
				jobRemovedCause[0] = status;
				removedSignal3[0].countDown();

			}
		});
		
		
		IStatusRunnableWithProgress runnable = new IStatusRunnableWithProgress() {
			@Override
			public IStatus run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					removedSignal1[0].await();
				} catch(InterruptedException ie) {}
				removedSignal2[0].countDown();
				if( monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				throw new InvocationTargetException(new Exception("Expected Cancel nested"), "Expected Cancel");
			}
		};

		
		IJob scheduled = jm.scheduleJob("Test1", runnable);
		assertNotNull(scheduled);
		assertNotNull(addedJobs[0]);
		assertEquals(scheduled, addedJobs[0]);
		
		// cancel the job
		jm.cancel(scheduled);
		
		// countdown once
		removedSignal1[0].countDown();
		try {
			removedSignal3[0].await();
		} catch(InterruptedException ie) {}
		
		assertNotNull(jobRemovedCause[0]);
		assertEquals(jobRemovedCause[0].getSeverity(), completionStatus.getSeverity());
		assertEquals(jobRemovedCause[0].getMessage(), completionStatus.getMessage());
		
		
	}

	
	public void testBasicManagerFunctions(final IStatus completionStatus) {
		IStatusRunnableWithProgress srwp = new IStatusRunnableWithProgress() {
			@Override
			public IStatus run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				return completionStatus;
			}
		};
		testBasicManagerFunctions(completionStatus, srwp);
	}
	
	public void testBasicManagerFunctions(final IStatus completionStatus, IStatusRunnableWithProgress runnable) {

		JobManager jm = new JobManager();
		CountDownLatch[] removedSignal1 = new CountDownLatch[1];
		CountDownLatch[] removedSignal2 = new CountDownLatch[1];
		removedSignal1[0] = new CountDownLatch(1);
		removedSignal2[0] = new CountDownLatch(1);
		final IStatus[] jobRemovedCause = new IStatus[1];
		final IJob[] addedJobs = new IJob[1];
		jm.addJobListener(new JobListenerAdapter() {
			@Override
			public void jobAdded(IJob job) {
				addedJobs[0] = job;
			}

			@Override
			public void jobRemoved(IJob job, IStatus status) {
				try {
					removedSignal1[0].await();
				} catch(InterruptedException ie) {}
				jobRemovedCause[0] = status;
				removedSignal2[0].countDown();
			}
		});
		
		
		IJob scheduled = jm.scheduleJob("Test1", runnable);
		assertNotNull(scheduled);
		assertNotNull(addedJobs[0]);
		assertEquals(scheduled, addedJobs[0]);
		
		// countdown once
		removedSignal1[0].countDown();
		try {
			removedSignal2[0].await();
		} catch(InterruptedException ie) {}
		
		assertNotNull(jobRemovedCause[0]);
		assertEquals(jobRemovedCause[0].getSeverity(), completionStatus.getSeverity());
		assertEquals(jobRemovedCause[0].getMessage(), completionStatus.getMessage());
		
	}
}
