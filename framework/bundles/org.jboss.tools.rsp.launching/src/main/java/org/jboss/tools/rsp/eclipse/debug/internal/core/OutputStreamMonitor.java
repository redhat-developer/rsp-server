/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.debug.internal.core;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.tools.rsp.eclipse.core.runtime.ISafeRunnable;
import org.jboss.tools.rsp.eclipse.core.runtime.ListenerList;
import org.jboss.tools.rsp.eclipse.core.runtime.SafeRunner;
import org.jboss.tools.rsp.eclipse.debug.core.IStreamListener;
import org.jboss.tools.rsp.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors the output stream of a system process and notifies
 * listeners of additions to the stream.
 *
 * The output stream monitor reads system out (or err) via
 * and input stream.
 */
public class OutputStreamMonitor implements IFlushableStreamMonitor {
	private static final Logger LOG = LoggerFactory.getLogger(OutputStreamMonitor.class);

	
	/**
	 * The stream being monitored (connected system out or err).
	 */
	private InputStream fStream;

	/**
	 * A collection of listeners
	 */
	private ListenerList<IStreamListener> fListeners = new ListenerList<>();

	/**
	 * Whether content is being buffered
	 */
	private boolean fBuffered = true;

	/**
	 * The local copy of the stream contents
	 */
	private StringBuffer fContents;

	/**
	 * The thread which reads from the stream
	 */
	private Thread fThread;

	/**
	 * The size of the read buffer
	 */
	private static final int BUFFER_SIZE= 8192;

	/**
	 * Whether or not this monitor has been killed.
	 * When the monitor is killed, it stops reading
	 * from the stream immediately.
	 */
	private boolean fKilled= false;

    private long lastSleep;

	private String fEncoding;

	/**
	 * Creates an output stream monitor on the
	 * given stream (connected to system out or err).
	 *
	 * @param stream input stream to read from
	 * @param encoding stream encoding or <code>null</code> for system default
	 */
	public OutputStreamMonitor(InputStream stream, String encoding) {
        fStream = new BufferedInputStream(stream, 8192);
        fEncoding = encoding;
		fContents= new StringBuffer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStreamMonitor#addListener(org.eclipse.debug.core.IStreamListener)
	 */
	@Override
	public synchronized void addListener(IStreamListener listener) {
		fListeners.add(listener);
	}

	/**
	 * Causes the monitor to close all
	 * communications between it and the
	 * underlying stream by waiting for the thread to terminate.
	 */
	protected void close() {
		if (fThread != null) {
			Thread thread= fThread;
			fThread= null;
			try {
				thread.join();
			} catch (InterruptedException ie) {
				// Intentionally not rethrown or interrupt flag set. 
				// Copied from org.eclipse
			}
			fListeners = new ListenerList<>();
		}
	}

	/**
	 * Notifies the listeners that text has
	 * been appended to the stream.
	 * @param text the text that was appended to the stream
	 */
	private void fireStreamAppended(String text) {
		getNotifier().notifyAppend(text);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStreamMonitor#getContents()
	 */
	@Override
	public synchronized String getContents() {
		return fContents.toString();
	}

	/**
	 * Continually reads from the stream.
	 * <p>
	 * This method, along with the <code>startReading</code>
	 * method is used to allow <code>OutputStreamMonitor</code>
	 * to implement <code>Runnable</code> without publicly
	 * exposing a <code>run</code> method.
	 */
	private void read() {
        lastSleep = System.currentTimeMillis();
        long currentTime = lastSleep;
		byte[] bytes= new byte[BUFFER_SIZE];
		int read = 0;
		while (read >= 0) {
			try {
				if (fKilled) {
					break;
				}
				read= fStream.read(bytes);
				if (read > 0) {
					String text;
					if (fEncoding != null) {
						text = new String(bytes, 0, read, fEncoding);
					} else {
						text = new String(bytes, 0, read);
					}
					synchronized (this) {
						if (isBuffered()) {
							fContents.append(text);
						}
						fireStreamAppended(text);
					}
				}
			} catch (IOException ioe) {
				if (!fKilled) {
					log(ioe);
				}
				return;
			} catch (NullPointerException e) {
				// killing the stream monitor while reading can cause an NPE
				// when reading from the stream
				if (!fKilled && fThread != null) {
					log(e);
				}
				return;
			}

            currentTime = System.currentTimeMillis();
            if (currentTime - lastSleep > 1000) {
                lastSleep = currentTime;
                try {
                    Thread.sleep(1); // just give up CPU to maintain UI responsiveness.
                } catch (InterruptedException e) {
    				// Intentionally not rethrown or interrupt flag set. 
    				// Copied from org.eclipse
                }
            }
		}
		try {
			fStream.close();
		} catch (IOException e) {
			log(e);
		}
	}

	protected void kill() {
		fKilled= true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStreamMonitor#removeListener(org.eclipse.debug.core.IStreamListener)
	 */
	@Override
	public synchronized void removeListener(IStreamListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Starts a thread which reads from the stream
	 */
	protected void startMonitoring() {
		if (fThread == null) {
			fThread= new Thread(new Runnable() {
				@Override
				public void run() {
					read();
				}
			}, "Output Stream Monitor"); // DebugCoreMessages.OutputStreamMonitor_label);
            fThread.setDaemon(true);
            fThread.setPriority(Thread.MIN_PRIORITY);
			fThread.start();
		}
	}

	/**
	 * @see org.jboss.tools.rsp.eclipse.debug.core.model.IFlushableStreamMonitor#setBuffered(boolean)
	 */
	@Override
	public synchronized void setBuffered(boolean buffer) {
		fBuffered = buffer;
	}

	/**
	 * @see org.jboss.tools.rsp.eclipse.debug.core.model.IFlushableStreamMonitor#flushContents()
	 */
	@Override
	public synchronized void flushContents() {
		fContents.setLength(0);
	}

	/**
	 * @see IFlushableStreamMonitor#isBuffered()
	 */
	@Override
	public synchronized boolean isBuffered() {
		return fBuffered;
	}

	private ContentNotifier getNotifier() {
		return new ContentNotifier();
	}

	class ContentNotifier implements ISafeRunnable {

		private IStreamListener fListener;
		private String fText;

		/**
		 * @see org.jboss.tools.rsp.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		@Override
		public void handleException(Throwable exception) {
			log(exception);
		}

		/**
		 * @see org.jboss.tools.rsp.eclipse.core.runtime.ISafeRunnable#run()
		 */
		@Override
		public void run() throws Exception {
			fListener.streamAppended(fText, OutputStreamMonitor.this);
		}

		public void notifyAppend(String text) {
			if (text == null) {
				return;
			}
			fText = text;
			for (IStreamListener iStreamListener : fListeners) {
				fListener = iStreamListener;
				SafeRunner.run(this);
			}
			fListener = null;
			fText = null;
		}
	}
	
	private void log(Throwable t) {
		String msg = (t == null || t.getMessage() == null ? "Unknown Error" : t.getMessage());
		LOG.error(msg, t);
	}
}
