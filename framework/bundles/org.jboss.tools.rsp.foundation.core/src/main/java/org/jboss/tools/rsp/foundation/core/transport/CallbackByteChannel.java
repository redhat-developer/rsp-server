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
	}
}