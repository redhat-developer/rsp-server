/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.foundation.core.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.CancellationException;

class CallbackByteChannel implements ReadableByteChannel {

	public static interface ProgressCallBack {
		public void callback(CallbackByteChannel rbc, double progress) throws CancellationException;
	}
	
	private ProgressCallBack delegate;
	private long size;
	private ReadableByteChannel rbc;
	private long sizeRead;

	
	private Exception error = null;
	
	CallbackByteChannel(ReadableByteChannel rbc, long expectedSize, ProgressCallBack delegate) {
		this.delegate = delegate;
		this.size = expectedSize;
		this.rbc = rbc;
	}

	public void close() throws IOException {
		rbc.close();
	}

	public long getReadSoFar() {
		return sizeRead;
	}

	public boolean isOpen() {
		return rbc.isOpen();
	}

	public int read(ByteBuffer bb) throws IOException {
		int n;
		double progress;
		try {
			if ((n = rbc.read(bb)) > 0) {
				sizeRead += n;
				progress = size > 0 ? (double) sizeRead / (double) size * 100.0 : -1.0;
				try {
					delegate.callback(this, progress);
				} catch(CancellationException ce) {
					close();
					return n;
				}
			}
			return n;
		} catch(IOException ioe) {
			error = ioe;
			throw ioe;
		}
	}
	
	/**
	 * If this channel's read operations ended in an error, return 
	 * that error, or null.
	 * @return
	 */
	public Exception getError() {
		return error;
	}
}