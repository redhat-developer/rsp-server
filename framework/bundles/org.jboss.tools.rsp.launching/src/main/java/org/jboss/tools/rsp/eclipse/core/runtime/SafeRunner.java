/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.rsp.eclipse.core.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the given ISafeRunnable in a protected mode: exceptions and certain
 * errors thrown in the runnable are logged and passed to the runnable's
 * exception handler.  Such exceptions are not rethrown by this method.
 * <p>
 * This class can be used without OSGi running.
 * </p>
 * @since org.eclipse.equinox.common 3.2
 */
public final class SafeRunner {
	private static final Logger LOG = LoggerFactory.getLogger(SafeRunner.class);

	/**
	 * Runs the given runnable in a protected mode.   Exceptions
	 * thrown in the runnable are logged and passed to the runnable's
	 * exception handler.  Such exceptions are not rethrown by this method.
	 * <p>
	 * In addition to catching all {@link Exception} types, this method also catches certain {@link Error} 
	 * types that typically result from programming errors in the code being executed. 
	 * Severe errors that are not generally safe to catch are not caught by this method.
	 * </p>
	 *
	 * @param code the runnable to run
	 */
	public static void run(ISafeRunnable code) {
		Assert.isNotNull(code);
		try {
			code.run();
		} catch (Exception e) {
			handleException(code, e);
		} catch (LinkageError e) {
			handleException(code, e);
		} catch (AssertionError e) {
			handleException(code, e);
		}
	}

	private static void log(Throwable t) {
		if( t == null || t.getMessage() == null )
			LOG.error("Unknown Error");
		else
			LOG.error(t.getMessage() , t);
	}
	
	private static void handleException(ISafeRunnable code, Throwable e) {
		log(e);
	}
}
