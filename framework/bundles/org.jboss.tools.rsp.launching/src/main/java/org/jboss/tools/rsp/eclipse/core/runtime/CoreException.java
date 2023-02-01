/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.core.runtime;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * A checked exception representing a failure.
 * <p>
 * Core exceptions contain a status object describing the cause of the exception.
 * </p><p>
 * This class can be used without OSGi running.
 * </p>
 * @see IStatus
 */
public class CoreException extends Exception {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/** Status object. */
	private final IStatus status;

	/**
	 * Creates a new exception with the given status object.  The message
	 * of the given status is used as the exception message.
	 *
	 * @param status the status object to be associated with this exception
	 */
	public CoreException(IStatus status) {
		super(status.getMessage());
		this.status = status;
	}

	/**
	  * Returns the cause of this exception, or <code>null</code> if none.
	  * 
	  * @return the cause for this exception
	  * @since 3.4
	  */
	@Override
	public synchronized Throwable getCause() {
		return status.getException();
	}

	/**
	 * Returns the status object for this exception.
	 * <p>
	 *   <b>IMPORTANT:</b><br>
	 *   The result must NOT be used for logging, error reporting, or as a method 
	 *   return value, since that code pattern hides the original stack trace. Instead, 
	 *   create a new {@link Status} with your plug-in ID and this 
	 *   <code>CoreException</code>, and use that new status for error reporting
	 *   or as a method return value. For example, instead of:
	 *   <pre>
	 *      yourPlugin.getLog().log(exception.getStatus());
	 *   </pre>
	 *   Use:
	 *   <pre>
	 *      IStatus result = new Status(exception.getStatus().getSeverity(), pluginId, message, exception);
	 *      yourPlugin.getLog().log(result);
	 *   </pre>
	 * </p>
	 *
	 * @return a status object
	 */
	public final IStatus getStatus() {
		return status;
	}

	/**
	 * Prints a stack trace out for the exception, and
	 * any nested exception that it may have embedded in
	 * its Status object.
	 */
	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	/**
	 * Prints a stack trace out for the exception, and
	 * any nested exception that it may have embedded in
	 * its Status object.
	 * 
	 * @param output the stream to write to
	 */
	@Override
	public void printStackTrace(final PrintStream output) {
		synchronized (output) {
			super.printStackTrace(output);
			printChildren(status, output);
		}
	}

	/**
	 * Prints a stack trace out for the exception, and
	 * any nested exception that it may have embedded in
	 * its Status object.
	 * 
	 * @param output the stream to write to
	 */
	@Override
	public void printStackTrace(final PrintWriter output) {
		synchronized (output) {
			super.printStackTrace(output);
			printChildren(status, output);
		}
	}
	

	static public void printChildren(IStatus status, PrintStream output) {
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0)
			return;
		for (int i = 0; i < children.length; i++) {
			output.println("Contains: " + children[i].getMessage()); //$NON-NLS-1$
			Throwable exception = children[i].getException();
			if (exception != null)
				exception.printStackTrace(output);
			printChildren(children[i], output);
		}
	}

	static public void printChildren(IStatus status, PrintWriter output) {
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0)
			return;
		for (int i = 0; i < children.length; i++) {
			output.println("Contains: " + children[i].getMessage()); //$NON-NLS-1$
			output.flush(); // call to synchronize output
			Throwable exception = children[i].getException();
			if (exception != null)
				exception.printStackTrace(output);
			printChildren(children[i], output);
		}
	}

}
