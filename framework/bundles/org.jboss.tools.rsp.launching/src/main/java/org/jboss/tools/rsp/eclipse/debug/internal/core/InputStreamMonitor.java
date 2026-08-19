/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.debug.internal.core;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes to the input stream of a system process,
 * queueing output if the stream is blocked.
 *
 * The input stream monitor writes to system in via
 * an output stream.
 */
public class InputStreamMonitor {
	private static final Logger LOG = LoggerFactory.getLogger(InputStreamMonitor.class);

	/**
	 * The stream which is being written to (connected to system in).
	 */
	private final OutputStream fStream;
	/**
	 * The queue of output.
	 */
	private final Queue<byte[]> fQueue;
	/**
	 * The thread which writes to the stream.
	 */
	private volatile Thread fThread;
	/**
	 * A lock for ensuring that writes to the queue are contiguous
	 */
	private final Object fLock;

	/**
	 * Whether the underlying output stream has been closed
	 */
	private volatile boolean fClosed = false;

	/**
	 * The encoding of the input stream.
	 */
	private final String fEncoding;

	/**
	 * Creates an input stream monitor which writes to system in via the given output stream.
	 *
	 * @param stream output stream
	 */
	public InputStreamMonitor(OutputStream stream) {
		this(stream, null);
	}

	/**
	 * Creates an input stream monitor which writes to system in via the given output stream.
	 *
	 * @param stream output stream
	 * @param encoding stream encoding or <code>null</code> for system default
	 */
	public InputStreamMonitor(OutputStream stream, String encoding) {
		fStream = stream;
		fQueue = new LinkedTransferQueue<>();
		fLock = new Object();
		fEncoding = encoding;
	}

	/**
	 * Appends the given text to the stream, or
	 * queues the text to be written at a later time
	 * if the stream is blocked.
	 *
	 * @param text text to append
	 */
	public void write(String text) {
		if (fClosed) {
			return;
		}
		try {
			byte[] data = fEncoding != null ? text.getBytes(fEncoding) : text.getBytes();
			synchronized (fLock) {
				fQueue.offer(data);
				fLock.notifyAll();
			}
		} catch (IOException e) {
			log(e);
		}
	}

	/**
	 * Starts a thread which writes the stream.
	 */
	public synchronized void startMonitoring() {
		if (fThread == null) {
			fThread = new Thread((Runnable) this::writeLoop, "Input Stream Monitor");
			fThread.setDaemon(true);
			fThread.start();
		}
	}

	/**
	 * Close all communications between this
	 * monitor and the underlying stream.
	 */
	public void close() {
		Thread thread;
		synchronized (this) {
			thread = fThread;
			fThread = null;
		}
		if (thread != null) {
			thread.interrupt();
		}
	}

	/**
	 * Continuously writes to the stream.
	 */
	private void writeLoop() {
		try {
			try {
				while (fThread != null) {
					writeNext();
				}
			} finally {
				if (!fClosed) {
					fClosed = true;
					fStream.close();
				}
			}
		} catch (IOException e) {
			fQueue.clear();
			log(e);
		}
	}

	/**
	 * Write the text in the queue to the stream.
	 */
	private void writeNext() throws IOException {
		while (!fQueue.isEmpty() && !fClosed) {
			byte[] data = fQueue.poll();
			fStream.write(data);
			fStream.flush();
		}
		try {
			synchronized (fLock) {
				while (fQueue.isEmpty() && !fClosed && fThread != null) {
					fLock.wait();
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Closes the output stream attached to the standard input stream of this
	 * monitor's process.
	 *
	 * @exception IOException if an exception occurs closing the input stream
	 */
	public void closeInputStream() throws IOException {
		if (!fClosed) {
			fClosed = true;
			fStream.close();
		} else {
			throw new IOException();
		}
	}

	private void log(Throwable t) {
		String msg = (t == null || t.getMessage() == null ? "Unknown Error" : t.getMessage());
		LOG.error(msg, t);
	}
}

