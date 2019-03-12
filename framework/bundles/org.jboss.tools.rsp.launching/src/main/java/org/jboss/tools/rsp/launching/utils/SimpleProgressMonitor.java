/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.launching.utils;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;

/**
 * Progress monitor for search
 *
 * @since 2.0
 */
public class SimpleProgressMonitor implements IProgressMonitor {
	private boolean started, done, canceled;

	private int totalWork = IProgressMonitor.UNKNOWN;

	private double currWork;

	/**
	 * Constructor.
	 */
	public SimpleProgressMonitor() {
		started = done = canceled = false;
	}

	@Override
	public void beginTask(String name, int totalWork) {
		this.totalWork = totalWork;
		this.started = true;
	}

	@Override
	public void done() {
		currWork = totalWork;
		this.done = true;
		this.started = true;
	}

	@Override
	public void setTaskName(String name) {
	}

	@Override
	public void subTask(String name) {
	}

	@Override
	public void worked(int work) {
		internalWorked(work);
	}

	@Override
	public void internalWorked(double work) {
		currWork += work;
		if (currWork > totalWork)
			currWork = totalWork;
		else if (currWork < 0)
			currWork = 0;
	}

	public double getPercentage() {
		if (done) {
			return 100;
		}
		if (totalWork == IProgressMonitor.UNKNOWN)
			return 0;
		if (currWork >= totalWork)
			return 100;
		return (int)(100 * currWork / totalWork);
	}

	/**
	 * Gets the isCancelled.
	 *
	 * @return Returns a boolean
	 */
	@Override
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Sets the isStarted.
	 */
	public void started() {
		this.started = true;
	}

	/**
	 * Gets the isStarted.
	 *
	 * @return Returns a boolean
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Gets the isDone.
	 *
	 * @return Returns a boolean
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Sets the isCanceled.
	 *
	 * @param canceled
	 *            The isCanceled to set
	 */
	@Override
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
