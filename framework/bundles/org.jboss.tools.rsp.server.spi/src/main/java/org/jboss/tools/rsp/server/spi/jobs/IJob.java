/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.jobs;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;

public interface IJob {
	
	/**
	 * Get a name for this job
	 * @return
	 */
	public String getName();
	
	/**
	 * Get the id of this job
	 * @return
	 */
	public String getId();
	
	/**
	 * Return a double between 0 and 100 indicating the progress thus far,
	 * or -1 if unknown
	 * @return
	 */
	public double getProgress();
	
	/**
	 * Cancel the current job
	 * @return 
	 */
	public IStatus cancel();
}
