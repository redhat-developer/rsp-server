/*******************************************************************************
 * Copyright (c) 2013 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.foundation.core.plugin;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;

/**
 * This interface is useful for logging messages to your plugin's log. 
 * This interface reserves the right to add additional methods 
 * that begin with "log". 
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPluginLog {
	
	/**
	 * Log an error in the error log
	 * @param message
	 * @param t
	 */
	public void logError(String message, Throwable t);

	/**
	 * Log an error in the error log
	 * @param message
	 */
	public void logError(String message);

	/**
	 * Log an error in the error log
	 * @param t
	 */
	public void logError(Throwable t);

	/**
	 * Log a warning in the error log
	 * @param message
	 * @param t
	 */
	public void logWarning(String message, Throwable t);	

	/**
	 * Log a warning in the error log
	 * @param message
	 */
	public void logWarning(String message);	

	/**
	 * Log a warning in the error log
	 * @param t
	 */
	public void logWarning(Throwable t);
	
	/**
	 * Log an info-level status event in the error log
	 * @param message
	 * @param t
	 */
	public void logInfo(String message, Throwable t);	
	
	/**
	 * Log an info-level status event in the error log
	 * @param message
	 */
	public void logInfo(String message);
	
	/**
	 * Log a throwable. If the throwable is null, log the message
	 * instead at INFO level. If the Throwable is not null, convert it into a form
	 * that maintains the most information possible, at IStatus.ERROR severity, and then log it. 
	 * 
	 * @param code
	 * @param message
	 * @param t
	 */
	public void logMessage(int code, String message, Throwable t);
	
	/**
	 * Log a status object
	 * @param status
	 */
	public void logStatus(IStatus status);
}
