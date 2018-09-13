/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jboss.tools.rsp.eclipse.jdt.launching;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents a particular installation of a VM. A VM instance holds all
 * parameters specific to a VM installation. Unlike VM types, VM instances can
 * be created and configured dynamically at run-time. This is typically done by
 * the user interactively in the UI.
 * <p>
 * A VM install is responsible for creating VM runners to launch a Java program
 * in a specific mode.
 * </p>
 * <p>
 * This interface is intended to be implemented by clients that contribute to
 * the <code>"org.eclipse.jdt.launching.vmInstallTypes"</code> extension point.
 * Rather than implementing this interface directly, it is strongly recommended
 * that clients subclass
 * {@link org.jboss.tools.rsp.eclipse.jdt.launching.AbstractVMInstall} to be
 * insulated from potential API additions. In 3.1, a new optional interface has
 * been added for implementors of this interface -
 * {@link org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall2}. The new
 * interface is implemented by
 * {@link org.jboss.tools.rsp.eclipse.jdt.launching.AbstractVMInstall}.
 * </p>
 * 
 * @see org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall2
 */
public interface IVMInstall {
	/**
	 * Returns a VM runner that runs this installed VM in the given mode.
	 *
	 * @param mode
	 *            the mode the VM should be launched in; one of the constants
	 *            declared in <code>org.eclipse.debug.core.ILaunchManager</code>
	 * @return a VMRunner for a given mode May return <code>null</code> if the given
	 *         mode is not supported by this VM.
	 * @see org.eclipse.debug.core.ILaunchManager
	 */
	IVMRunner getVMRunner(String mode);

	/**
	 * Returns the id for this VM. VM IDs are unique within the VMs of a given VM
	 * type. The VM id is not intended to be presented to users.
	 *
	 * @return the VM identifier. Must not return <code>null</code>.
	 */
	String getId();

	/**
	 * Returns the display name of this VM. The VM name is intended to be presented
	 * to users.
	 *
	 * @return the display name of this VM. May return <code>null</code>.
	 */
	String getName();

	/**
	 * Sets the display name of this VM. The VM name is intended to be presented to
	 * users.
	 *
	 * @param name
	 *            the display name of this VM
	 */
	void setName(String name);

	/**
	 * Returns the root directory of the install location of this VM.
	 *
	 * @return the root directory of this VM installation. May return
	 *         <code>null</code>.
	 */
	File getInstallLocation();

	/**
	 * Sets the root directory of the install location of this VM.
	 *
	 * @param installLocation
	 *            the root directory of this VM installation
	 */
	void setInstallLocation(File installLocation);

	/**
	 * Returns the VM type of this VM.
	 *
	 * @return the VM type that created this IVMInstall instance
	 */
	IVMInstallType getVMInstallType();

	/**
	 * Returns the library locations of this IVMInstall. Generally, clients should
	 * use <code>JavaRuntime.getLibraryLocations(IVMInstall)</code> to determine the
	 * libraries associated with this VM install.
	 *
	 * @see IVMInstall#setLibraryLocations(LibraryLocation[])
	 * @return The library locations of this IVMInstall. Returns <code>null</code>
	 *         to indicate that this VM install uses the default library locations
	 *         associated with this VM's install type.
	 * @since 2.0
	 */
	LibraryLocation[] getLibraryLocations();

	/**
	 * Sets the library locations of this IVMInstall.
	 * 
	 * @param locations
	 *            The <code>LibraryLocation</code>s to associate with this
	 *            IVMInstall. May be <code>null</code> to indicate that this VM
	 *            install uses the default library locations associated with this
	 *            VM's install type.
	 * @since 2.0
	 */
	void setLibraryLocations(LibraryLocation[] locations);

	/**
	 * Sets the Javadoc location associated with this VM install.
	 *
	 * @param url
	 *            a url pointing to the Javadoc location associated with this VM
	 *            install
	 * @since 2.0
	 */
	public void setJavadocLocation(URL url);

	/**
	 * Returns the Javadoc location associated with this VM install.
	 *
	 * @return a url pointing to the Javadoc location associated with this VM
	 *         install, or <code>null</code> if none
	 * @since 2.0
	 */
	public URL getJavadocLocation();

	/**
	 * Returns VM arguments to be used with this vm install whenever this VM is
	 * launched as they should be passed to the command line, or <code>null</code>
	 * if none.
	 *
	 * @return VM arguments to be used with this vm install whenever this VM is
	 *         launched as they should be passed to the command line, or
	 *         <code>null</code> if none
	 * @since 3.0
	 */
	public String[] getVMArguments();

	/**
	 * Sets VM arguments to be used with this vm install whenever this VM is
	 * launched, possibly <code>null</code>. This is equivalent to
	 * <code>setVMArgs(String)</code> with whitespace character delimited arguments.
	 *
	 * @param vmArgs
	 *            VM arguments to be used with this vm install whenever this VM is
	 *            launched, possibly <code>null</code>
	 * @since 3.0
	 * @deprecated if possible, clients should use setVMArgs(String) on
	 *             {@link IVMInstall2} when possible
	 */
	@Deprecated
	public void setVMArguments(String[] vmArgs);

	/**
	 * Returns VM arguments to be used with this vm install whenever this VM is
	 * launched as a raw string, or <code>null</code> if none.
	 *
	 * @return VM arguments to be used with this vm install whenever this VM is
	 *         launched as a raw string, or <code>null</code> if none
	 */
	public String getVMArgs();

	/**
	 * Sets VM arguments to be used with this vm install whenever this VM is
	 * launched as a raw string, possibly <code>null</code>.
	 *
	 * @param vmArgs
	 *            VM arguments to be used with this vm install whenever this VM is
	 *            launched as a raw string, possibly <code>null</code>
	 */
	public void setVMArgs(String vmArgs);

	/**
	 * Returns a string representing the <code>java.version</code> system property
	 * of this VM install, or <code>null</code> if unknown.
	 *
	 * @return a string representing the <code>java.version</code> system property
	 *         of this VM install, or <code>null</code> if unknown.
	 */
	public String getJavaVersion();

	/**
	 * Evaluates the specified system properties in this VM, returning the result as
	 * a map of property names to property values.
	 *
	 * @param properties
	 *            the property names to evaluate, for example
	 *            <code>{"user.home"}</code>
	 * @param monitor
	 *            progress monitor or <code>null</code>
	 * @return map of system property names to associated property values
	 * @throws CoreException
	 *             if an exception occurs evaluating the properties
	 * @since 3.2
	 */
	public Map<String, String> evaluateSystemProperties(String[] properties, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Get the registry that this vm install is registered in, or
	 * null if this install has not been registered in any model yet.
	 * 
	 * @return registry object this install has been registered in
	 */
	public IVMInstallRegistry getRegistry();

}
