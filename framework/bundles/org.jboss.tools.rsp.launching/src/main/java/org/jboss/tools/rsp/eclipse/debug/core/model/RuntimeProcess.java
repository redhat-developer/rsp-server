/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.debug.core.model;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugEvent;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.DebugPluginConstants;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.internal.core.NullStreamsProxy;
import org.jboss.tools.rsp.eclipse.debug.internal.core.StreamsProxy;
import org.jboss.tools.rsp.launching.RuntimeProcessEventManager;


/**
 * Standard implementation of an <code>IProcess</code> that wrappers a system
 * process (<code>java.lang.Process</code>).
 * <p>
 * Clients may subclass this class. Clients that need to replace the implementation
 * of a streams proxy associated with an <code>IProcess</code> should subclass this
 * class. Generally clients should not instantiate this class directly, but should
 * instead call <code>DebugPlugin.newProcess(...)</code>, which can delegate to an
 * <code>IProcessFactory</code> if one is referenced by the associated launch configuration.
 * </p>
 * @see org.jboss.tools.rsp.eclipse.debug.core.model.IProcess
 * @see org.jboss.tools.rsp.eclipse.debug.core.IProcessFactory
 * @since 3.0
 */
public class RuntimeProcess implements IProcess {

	private static final int TERMINATION_TIMEOUT = 5000; // ms

	private static final String RuntimeProcess_Exit_value_not_available_until_process_terminates__1="Exit value not available until process terminates.";
	private static final String ProcessMonitorJob_0="Process monitor";
	private static final String RuntimeProcess_terminate_failed="Terminate failed";

	/**
	 * The launch this process is contained in
	 */
	private ILaunch fLaunch;

	/**
	 * The system process represented by this <code>IProcess</code>
	 */
	private Process fProcess;

	/**
	 * This process's exit value.
	 *
	 * synchronized by this
	 */
	private int fExitValue;

	/**
	 * The monitor which listens for this runtime process' system process
	 * to terminate.
	 */
	private final ProcessMonitorThread fMonitor;

	/**
	 * The streams proxy for this process
	 */
	private final IStreamsProxy fStreamsProxy;

	/**
	 * The name of the process
	 */
	private final String fName;

	/**
	 * Whether this process has been terminated
	 */
	private boolean fTerminated;

	/**
	 * Table of client defined attributes
	 */
	private final Map<String, String> fAttributes = new ConcurrentHashMap<>();

	/**
	 * Whether output from the process should be captured or swallowed
	 */
	private final boolean fCaptureOutput;

	/**
	 * Constructs a RuntimeProcess on the given system process
	 * with the given name, adding this process to the given
	 * launch.
	 *
	 * @param launch the parent launch of this process
	 * @param process underlying system process
	 * @param name the label used for this process
	 * @param attributes map of attributes used to initialize the attributes
	 *   of this process, or <code>null</code> if none
	 */
	public RuntimeProcess(ILaunch launch, Process process, String name, Map<String, String> attributes) {
		setLaunch(launch);
		initializeAttributes(attributes);
		fProcess = process;
		fName = name;
		fTerminated = true;
		try {
			fExitValue = process.exitValue();
		} catch (IllegalThreadStateException e) {
			fTerminated = false;
		}

		String captureOutput = launch.getAttribute(DebugPluginConstants.ATTR_CAPTURE_OUTPUT);
		fCaptureOutput = !("false".equals(captureOutput)); //$NON-NLS-1$

		fStreamsProxy = createStreamsProxy();
		fMonitor = new ProcessMonitorThread();
		// Process must be added to launch before starting the monitor thread,
		// otherwise the process may terminate and generate notifications before
		// they can properly be processed.
		launch.addProcess(this);
		fMonitor.start();
		fireCreationEvent();
	}

	/**
	 * Initialize the attributes of this process to those in the given map.
	 *
	 * @param attributes attribute map or <code>null</code> if none
	 */
	private void initializeAttributes(Map<String, String> attributes) {
		if (attributes != null) {
			attributes.forEach(this::setAttribute);
		}
	}

	/**
	 * @see ITerminate#canTerminate()
	 */
	@Override
	public synchronized boolean canTerminate() {
		return !fTerminated;
	}

	/**
	 * @see IProcess#getLabel()
	 */
	@Override
	public String getLabel() {
		return fName;
	}

	/**
	 * Sets the launch this process is contained in
	 *
	 * @param launch the launch this process is contained in
	 */
	protected void setLaunch(ILaunch launch) {
		fLaunch = launch;
	}

	/**
	 * @see IProcess#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/**
	 * Returns the underlying system process associated with this process.
	 *
	 * @return system process
	 */
	protected Process getSystemProcess() {
		return fProcess;
	}

	/**
	 * @see ITerminate#isTerminated()
	 */
	@Override
	public synchronized boolean isTerminated() {
		return fTerminated;
	}

	/**
	 * @see ITerminate#terminate()
	 */
	@Override
	public void terminate() throws DebugException {
		if (!isTerminated()) {
			try {
				Process process = getSystemProcess();
				if (process == null) {
					return;
				}

				List<ProcessHandle> descendants = Collections.emptyList();
				try {
					descendants = process.descendants().collect(Collectors.toList());
				} catch (UnsupportedOperationException e) {
					// JVM may not support descendants()
				}

				process.destroy();
				descendants.forEach(ProcessHandle::destroy);

				try {
					long waitStart = System.currentTimeMillis();
					if (process.waitFor(TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS)) {
						int exitValue = process.exitValue();
						synchronized (this) {
							fExitValue = exitValue;
							fTerminated = true;
						}
						if (waitFor(descendants, waitStart)) {
							return;
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			} finally {
				if (fStreamsProxy instanceof StreamsProxy) {
					((StreamsProxy) fStreamsProxy).kill();
				}
			}

			// clean-up
			fMonitor.killThread();
			IStatus status = new Status(IStatus.ERROR, DebugPluginConstants.DEBUG_CORE_ID, DebugException.TARGET_REQUEST_FAILED, RuntimeProcess_terminate_failed, null);
			throw new DebugException(status);
		}
	}

	/**
	 * Awaits the termination of the processes of the given ProcessHandles.
	 */
	private boolean waitFor(List<ProcessHandle> descendants, long waitStart) throws InterruptedException {
		try {
			for (ProcessHandle handle : descendants) {
				long remainingTime = TERMINATION_TIMEOUT - (System.currentTimeMillis() - waitStart);
				handle.onExit().get(remainingTime, TimeUnit.MILLISECONDS);
			}
			return true;
		} catch (ExecutionException e) {
			throw new IllegalStateException(e.getCause());
		} catch (TimeoutException e) {
			return false;
		}
	}

	/**
	 * Notification that the system process associated with this process
	 * has terminated.
	 */
	protected void terminated() {
		if (fStreamsProxy instanceof StreamsProxy) {
			((StreamsProxy)fStreamsProxy).close();
		}

		// Avoid calling IProcess.exitValue() inside a sync section (Bug 311813).
		int exitValue = -1;
		boolean running = false;
		try {
			exitValue = fProcess.exitValue();
		} catch (IllegalThreadStateException ie) {
			running = true;
		}

		synchronized (this) {
			fTerminated = true;
			if (!running) {
				fExitValue = exitValue;
			}
			fProcess = null;
		}
		fireTerminateEvent();
	}

	/**
	 * @see IProcess#getStreamsProxy()
	 */
	@Override
	public IStreamsProxy getStreamsProxy() {
		if (!fCaptureOutput) {
			return null;
		}
		return fStreamsProxy;
	}

	/**
	 * Creates and returns the streams proxy associated with this process.
	 *
	 * @return streams proxy
	 */
	protected IStreamsProxy createStreamsProxy() {
		if (!fCaptureOutput) {
			return new NullStreamsProxy(getSystemProcess());
		}
		String encoding = getLaunch().getAttribute(DebugPluginConstants.ATTR_CONSOLE_ENCODING);
		return new StreamsProxy(getSystemProcess(), encoding);
	}

	/**
	 * Fires a creation event.
	 */
	protected void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/**
	 * Fires the given debug event.
	 *
	 * @param event debug event to fire
	 */
	protected void fireEvent(DebugEvent event) {
		RuntimeProcessEventManager.getDefault()
			.fireDebugEventSet(new DebugEvent[]{event});
	}

	/**
	 * Fires a terminate event.
	 */
	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/**
	 * Fires a change event.
	 */
	protected void fireChangeEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
	}

	/**
	 * @see IProcess#setAttribute(String, String)
	 */
	@Override
	public void setAttribute(String key, String value) {
		Objects.requireNonNull(key);
		if (value == null) {
			if (fAttributes.remove(key) != null) {
				fireChangeEvent();
			}
		} else {
			String origVal = fAttributes.put(key, value);
			if (!Objects.equals(origVal, value)) {
				fireChangeEvent();
			}
		}
	}

	/**
	 * @see IProcess#getAttribute(String)
	 */
	@Override
	public String getAttribute(String key) {
		return fAttributes.get(key);
	}

	/**
	 * @see IProcess#getExitValue()
	 */
	@Override
	public synchronized int getExitValue() throws DebugException {
		if (isTerminated()) {
			return fExitValue;
		}
		throw new DebugException(new Status(IStatus.ERROR, DebugPluginConstants.DEBUG_CORE_ID, DebugException.TARGET_REQUEST_FAILED, RuntimeProcess_Exit_value_not_available_until_process_terminates__1, null));
	}

	/**
	 * Monitors a system process, waiting for it to terminate, and
	 * then notifies the associated runtime process.
	 */
	private class ProcessMonitorThread extends Thread {

		/**
		 * Whether the thread has been told to exit.
		 */
		private volatile boolean fExit;

		/**
		 * @see Thread#run()
		 */
		@Override
		public void run() {
			Process fOSProcess = RuntimeProcess.this.getSystemProcess();
			if (!fExit && fOSProcess != null) {
				try {
					fOSProcess.waitFor();
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				} finally {
					RuntimeProcess.this.terminated();
				}
			}
		}

		/**
		 * Creates a new process monitor and starts monitoring the process for
		 * termination.
		 */
		private ProcessMonitorThread() {
			super(ProcessMonitorJob_0);
			setDaemon(true);
		}

		/**
		 * Kills the monitoring thread.
		 *
		 * This method is to be useful for dealing with the error
		 * case of an underlying process which has not informed this
		 * monitor of its termination.
		 */
		private void killThread() {
			fExit = true;
			this.interrupt();
		}
	}
}
