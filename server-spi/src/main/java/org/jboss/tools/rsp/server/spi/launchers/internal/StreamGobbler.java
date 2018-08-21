package org.jboss.tools.rsp.server.spi.launchers.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StreamGobbler extends Thread {

	private static final long MAX_WAIT_AFTER_TERMINATION = 5000;
	private static final long DELAY = 100;

	InputStream is;
	ArrayList<String> ret = new ArrayList<String>();
	private boolean canceled = false;
	private boolean complete = false;

	public StreamGobbler(InputStream is) {
		this.is = is;
	}

	private synchronized void add(String line) {
		ret.add(line);
	}

	private synchronized ArrayList<String> getList() {
		return ret;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while (!isCanceled() && (line = br.readLine()) != null)
				add(line);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		if (is != null) {
			try {
				is.close();
			} catch (IOException ioe) {
				// ignore
			}
		}
		setComplete();
	}

	private synchronized void setComplete() {
		complete = true;
	}

	private synchronized boolean isComplete() {
		return complete;
	}

	private synchronized void setCanceled() {
		canceled = true;
	}

	private synchronized boolean isCanceled() {
		return canceled;
	}

	public void cancel() {
		setCanceled();
		if (is != null) {
			try {
				is.close();
			} catch (IOException ioe) {
				// ignore
			}
		}
	}

	private void waitComplete(long delay, long maxwait) {
		long start = System.currentTimeMillis();
		long end = start + maxwait;
		while (!isComplete() && System.currentTimeMillis() < end) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ie) {

			}
		}
		if (!isComplete()) {
			cancel();
		}
	}

	/**
	 * Wait a maximum 5 seconds for the streams to finish reading whatever is in the
	 * pipeline
	 * 
	 * @return
	 */
	public List<String> getOutput() {
		waitComplete(DELAY, MAX_WAIT_AFTER_TERMINATION);
		return getList();
	}
}