/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.ssp.eclipse.jdt.launching;

import java.util.Map;

import org.jboss.tools.ssp.eclipse.core.runtime.CoreException;
import org.jboss.tools.ssp.eclipse.core.runtime.IProgressMonitor;

/**
 * Optional extensions that may be implemented by an
 * {@link org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstall}, providing access to
 * a JRE's system properties.
 * <p>
 * Clients that implement {@link org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstall} may additionally
 * implement this interface. However, it is strongly recommended that clients subclass
 * {@link org.jboss.tools.ssp.eclipse.jdt.launching.AbstractVMInstall} instead, which already implements
 * this interface, and will insulate clients from additional API additions in the future.
 * </p>
 * @since 3.2
 */
public interface IVMInstall3 {

	/**
	 * Evaluates the specified system properties in this VM, returning the result
	 * as a map of property names to property values.
	 *
	 * @param properties the property names to evaluate, for example <code>{"user.home"}</code>
	 * @param monitor progress monitor or <code>null</code>
	 * @return map of system property names to associated property values
	 * @throws CoreException if an exception occurs evaluating the properties
	 * @since 3.2
	 */
	public Map<String, String> evaluateSystemProperties(String[] properties, IProgressMonitor monitor) throws CoreException;
}
