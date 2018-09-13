/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class Status {

	/** Status severity constant (value 0) indicating this status represents the nominal case.
	 * This constant is also used as the status code representing the nominal case.
	 * @see #getSeverity()
	 * @see #isOK()
	 * @see Status#OK_STATUS
	 */
	public static final int OK = 0;

	/** Status type severity (bit mask, value 1) indicating this status is informational only.
	 * @see #getSeverity()
	 * @see #matches(int)
	 */
	public static final int INFO = 0x01;

	/** Status type severity (bit mask, value 2) indicating this status represents a warning.
	 * @see #getSeverity()
	 * @see #matches(int)
	 */
	public static final int WARNING = 0x02;

	/** Status type severity (bit mask, value 4) indicating this status represents an error.
	 * @see #getSeverity()
	 * @see #matches(int)
	 */
	public static final int ERROR = 0x04;

	/** Status type severity (bit mask, value 8) indicating this status represents a
	 * cancelation
	 * @see #getSeverity()
	 * @see #matches(int)
	 * @see Status#CANCEL_STATUS
	 * @since 3.0
	 */
	public static final int CANCEL = 0x08;
	
	
	/**
	 * The severity. One of
	 * <ul>
	 * <li><code>CANCEL</code></li>
	 * <li><code>ERROR</code></li>
	 * <li><code>WARNING</code></li>
	 * <li><code>INFO</code></li>
	 * <li>or <code>OK</code> (0)</li>
	 * </ul>
	 */
	private int severity = OK;

	/** Unique identifier of plug-in.
	 */
	private String pluginId;

	/** Plug-in-specific status code.
	 */
	private int code;

	/** Message, localized to the current locale.
	 */
	private String message;

	/** 
	 * A more detailed technical output of the failing, 
	 * such as a stacktrace
	 */
	private String trace = null;

	public Status() {
	}
	
	/**
	 * Creates a new status object.  The created status has no children.
	 *
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 * @param pluginId the unique identifier of the relevant plug-in
	 * @param code the plug-in-specific status code, or <code>OK</code>
	 * @param message a human-readable message, localized to the
	 *    current locale
	 * @param exception a low-level exception, or <code>null</code> if not
	 *    applicable 
	 */
	public Status(int severity, String pluginId, int code, String message, String trace) {
		setSeverity(severity);
		setPlugin(pluginId);
		setCode(code);
		setMessage(message);
		setTrace(trace);
	}

	/**
	 * Simplified constructor of a new status object; assumes that code is <code>OK</code>.
	 * The created status has no children.
	 *
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 * @param pluginId the unique identifier of the relevant plug-in
	 * @param message a human-readable message, localized to the
	 *    current locale
	 * @param exception a low-level exception, or <code>null</code> if not
	 *    applicable
	 *     
	 * @since org.eclipse.equinox.common 3.3
	 */
	public Status(int severity, String pluginId, String message, String trace) {
		setSeverity(severity);
		setPlugin(pluginId);
		setMessage(message);
		setTrace(trace);
		setCode(OK);
	}

	/**
	 * Simplified constructor of a new status object; assumes that code is <code>OK</code> and
	 * exception is <code>null</code>. The created status has no children.
	 *
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 * @param pluginId the unique identifier of the relevant plug-in
	 * @param message a human-readable message, localized to the
	 *    current locale
	 *    
	 * @since org.eclipse.equinox.common 3.3
	 */
	public Status(int severity, String pluginId, String message) {
		setSeverity(severity);
		setPlugin(pluginId);
		setMessage(message);
		setCode(OK);
		setTrace(null);
	}

	public int getCode() {
		return code;
	}

	public String getTrace() {
		return trace;
	}

	public String getMessage() {
		return message;
	}

	public String getPlugin() {
		return pluginId;
	}

	public int getSeverity() {
		return severity;
	}

	public boolean isOK() {
		return severity == OK;
	}

	public boolean matches(int severityMask) {
		return (severity & severityMask) != 0;
	}

	/**
	 * Sets the status code.
	 *
	 * @param code the plug-in-specific status code, or <code>OK</code>
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * Sets the exception.
	 *
	 * @param exception a low-level exception, or <code>null</code> if not
	 *    applicable 
	 */
	public void setTrace(String trace) {
		this.trace = trace;
	}

	/**
	 * Sets the message. If null is passed, message is set to an empty
	 * string.
	 *
	 * @param message a human-readable message, localized to the
	 *    current locale
	 */
	public void setMessage(String message) {
		if (message == null)
			this.message = ""; //$NON-NLS-1$
		else
			this.message = message;
	}

	/**
	 * Sets the plug-in id.
	 *
	 * @param pluginId the unique identifier of the relevant plug-in
	 */
	public void setPlugin(String pluginId) {
		this.pluginId = pluginId;
	}

	/**
	 * Sets the severity.
	 *
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 */
	public void setSeverity(int severity) {
		this.severity = severity;
	}

	/**
	 * Returns a string representation of the status, suitable 
	 * for debugging purposes only.
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Status "); //$NON-NLS-1$
		if (severity == OK) {
			buf.append("OK"); //$NON-NLS-1$
		} else if (severity == ERROR) {
			buf.append("ERROR"); //$NON-NLS-1$
		} else if (severity == WARNING) {
			buf.append("WARNING"); //$NON-NLS-1$
		} else if (severity == INFO) {
			buf.append("INFO"); //$NON-NLS-1$
		} else if (severity == CANCEL) {
			buf.append("CANCEL"); //$NON-NLS-1$
		} else {
			buf.append("severity="); //$NON-NLS-1$
			buf.append(severity);
		}
		buf.append(": "); //$NON-NLS-1$
		buf.append(pluginId);
		buf.append(" code="); //$NON-NLS-1$
		buf.append(code);
		buf.append(' ');
		buf.append(message);
		buf.append(' ');
		buf.append(trace);
		return buf.toString();
	}
	
}
