/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.debug.core.model;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

	private static final int MAX_WAIT_FOR_DEATH_ATTEMPTS = 10;
	private static final int TIME_TO_WAIT_FOR_THREAD_DEATH = 500; // ms

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
	 * This process's exit value
	 */
	private int fExitValue;

	/**
	 * The monitor which listens for this runtime process' system process
	 * to terminate.
	 */
	private ProcessMonitorThread fMonitor;

	/**
	 * The streams proxy for this process
	 */
	private IStreamsProxy fStreamsProxy;

	/**
	 * The name of the process
	 */
	private String fName;

	/**
	 * Whether this process has been terminated
	 */
	private boolean fTerminated;

	/**
	 * Table of client defined attributes
	 */
	private Map<String, String> fAttributes;

	/**
	 * Whether output from the process should be captured or swallowed
	 */
	private boolean fCaptureOutput = true;

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
		fProcess= process;
		fName= name;
		fTerminated= true;
		try {
			fExitValue = process.exitValue();
		} catch (IllegalThreadStateException e) {
			fTerminated= false;
		}

		String captureOutput = launch.getAttribute(DebugPluginConstants.ATTR_CAPTURE_OUTPUT);
		fCaptureOutput = !("false".equals(captureOutput)); //$NON-NLS-1$

		fStreamsProxy= createStreamsProxy();
		fMonitor = new ProcessMonitorThread(this);
		fMonitor.start();
		launch.addProcess(this);
		fireCreationEvent();
	}

	/**
	 * Initialize the attributes of this process to those in the given map.
	 *
	 * @param attributes attribute map or <code>null</code> if none
	 */
	private void initializeAttributes(Map<String, String> attributes) {
		if (attributes != null) {
			for (Entry<String, String> entry : attributes.entrySet()) {
				setAttribute(entry.getKey(), entry.getValue());
			}
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
			if (fStreamsProxy instanceof StreamsProxy) {
				((StreamsProxy)fStreamsProxy).kill();
			}
			Process process = getSystemProcess();
			if (process != null) {
			    process.destroy();
			}
			int attempts = 0;
			boolean interrupted = false;
			while (attempts < MAX_WAIT_FOR_DEATH_ATTEMPTS && !interrupted) {
				try {
				    process = getSystemProcess();
					if (process != null) {
						fExitValue = process.exitValue(); // throws exception if process not exited
					}
					return;
				} catch (IllegalThreadStateException ie) {
				}
				try {
					Thread.sleep(TIME_TO_WAIT_FOR_THREAD_DEATH);
				} catch (InterruptedException e) {
					interrupted = true;
					Thread.currentThread().interrupt();
				}
				attempts++;
			}
			// clean-up
			if (fMonitor != null) {
				fMonitor.killThread();
				fMonitor = null;
			}
			IStatus status = new Status(IStatus.ERROR, DebugPluginConstants.DEBUG_CORE_ID, DebugException.TARGET_REQUEST_FAILED, RuntimeProcess_terminate_failed, null);
			throw new DebugException(status);
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
			fTerminated= true;
			if (!running) {
			    fExitValue = exitValue;
			}
			fProcess= null;
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
		// Changed from DebugPlugin
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
		if (fAttributes == null) {
			fAttributes = new HashMap<String, String>(5);
		}
		Object origVal = fAttributes.get(key);
		if (origVal != null && origVal.equals(value)) {
			return; //nothing changed.
		}

		fAttributes.put(key, value);
		fireChangeEvent();
	}

	/**
	 * @see IProcess#getAttribute(String)
	 */
	@Override
	public String getAttribute(String key) {
		if (fAttributes == null) {
			return null;
		}
		return fAttributes.get(key);
	}
//
//	/* (non-Javadoc)
//	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
//	 */
//	@SuppressWarnings("unchecked")
//	@Override
//	public <T> T getAdapter(Class<T> adapter) {
//		if (adapter.equals(IProcess.class)) {
//			return (T) this;
//		}
//		if (adapter.equals(IDebugTarget.class)) {
//			ILaunch launch = getLaunch();
//			IDebugTarget[] targets = launch.getDebugTargets();
//			for (int i = 0; i < targets.length; i++) {
//				if (this.equals(targets[i].getProcess())) {
//					return (T) targets[i];
//				}
//			}
//			return null;
//		}
//		if (adapter.equals(ILaunch.class)) {
//			return (T) getLaunch();
//		}
//		//CONTEXTLAUNCHING
//		if(adapter.equals(ILaunchConfiguration.class)) {
//			return (T) getLaunch().getLaunchConfiguration();
//		}
//		return super.getAdapter(adapter);
//	}
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
	class ProcessMonitorThread extends Thread {

		/**
		 * Whether the thread has been told to exit.
		 */
		protected boolean fExit;
		/**
		 * The underlying <code>java.lang.Process</code> being monitored.
		 */
		protected Process fOSProcess;
		/**
		 * The <code>IProcess</code> which will be informed when this
		 * monitor detects that the underlying process has terminated.
		 */
		protected RuntimeProcess fRuntimeProcess;

		/**
		 * The <code>Thread</code> which is monitoring the underlying process.
		 */
		protected Thread fThread;

		/**
		 * A lock protecting access to <code>fThread</code>.
		 */
		private final Object fThreadLock = new Object();

		/**
		 * @see Thread#run()
		 */
		@Override
		public void run() {
			synchronized (fThreadLock) {
				if (fExit) {
					return;
				}
				fThread = Thread.currentThread();
			}
			while (fOSProcess != null) {
				try {
					fOSProcess.waitFor();
				} catch (InterruptedException ie) {
					// clear interrupted state
					// This is intentional
					Thread.interrupted();
				} finally {
					fOSProcess = null;
					fRuntimeProcess.terminated();
				}
			}
			fThread = null;
		}

		/**
		 * Creates a new process monitor and starts monitoring the process for
		 * termination.
		 *
		 * @param process process to monitor for termination
		 */
		public ProcessMonitorThread(RuntimeProcess process) {
			super(ProcessMonitorJob_0);
			setDaemon(true);
			fRuntimeProcess= process;
			fOSProcess= process.getSystemProcess();
		}

		/**
		 * Kills the monitoring thread.
		 *
		 * This method is to be useful for dealing with the error
		 * case of an underlying process which has not informed this
		 * monitor of its termination.
		 */
		protected void killThread() {
			synchronized (fThreadLock) {
				if (fThread == null) {
					fExit = true;
				} else {
					fThread.interrupt();
				}
			}
		}
	}
}
