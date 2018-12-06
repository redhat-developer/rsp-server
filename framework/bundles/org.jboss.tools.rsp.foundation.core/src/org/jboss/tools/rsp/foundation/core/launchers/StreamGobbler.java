/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.foundation.core.launchers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamGobbler extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(StreamGobbler.class);

	private static final long MAX_WAIT_AFTER_TERMINATION = 5000;
	private static final long DELAY = 100;

	private InputStream is;
	private List<String> ret = new ArrayList<>();
	private boolean canceled = false;
	private boolean complete = false;

	public StreamGobbler(InputStream is) {
		this.is = is;
	}

	private synchronized void add(String line) {
		ret.add(line);
	}

	private synchronized List<String> getList() {
		return ret;
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while (!isCanceled() && (line = br.readLine()) != null)
				add(line);
		} catch (IOException ioe) {
			LOG.error("Could not read input stream.", ioe);
		}

		close();
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
		close();
	}

	private void close() {
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