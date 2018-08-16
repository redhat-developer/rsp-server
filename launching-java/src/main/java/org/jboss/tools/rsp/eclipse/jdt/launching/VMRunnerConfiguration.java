/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jboss.tools.rsp.eclipse.jdt.launching;


import java.util.Map;

/**
 * Holder for various arguments passed to a VM runner.
 * Mandatory parameters are passed in the constructor; optional arguments, via setters.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @noextend This class is not intended to be sub-classed by clients.
 */
public class VMRunnerConfiguration {
	public static final String vmRunnerConfig_assert_classNotNull="classToLaunch cannot be null";
	public static final String vmRunnerConfig_assert_classPathNotNull="classPath cannot be null";
	public static final String vmRunnerConfig_assert_programArgsNotNull="args cannot be null";
	public static final String vmRunnerConfig_assert_vmArgsNotNull="args cannot be null";
	private String fClassToLaunch;
	private String[] fVMArgs;
	private String[] fProgramArgs;
	private String[] fEnvironment;
	private String[] fClassPath;
	private String[] fBootClassPath;
	private String[] fModulepath;
	private String fModuleDescription;
	private String fWorkingDirectory;
	private String fOverrideDependencies;
	private Map<String, Object> fVMSpecificAttributesMap;
	private boolean fResume = true;

	private static final String[] fgEmpty= new String[0];

	/**
	 * Creates a new configuration for launching a VM to run the given main class
	 * using the given class path.
	 *
	 * @param classToLaunch The fully qualified name of the class to launch. May not be null.
	 * @param classPath 	The classpath. May not be null.
	 */
	public VMRunnerConfiguration(String classToLaunch, String[] classPath) {
		if (classToLaunch == null) {
			throw new IllegalArgumentException(vmRunnerConfig_assert_classNotNull);
		}
		if (classPath == null) {
			throw new IllegalArgumentException(vmRunnerConfig_assert_classPathNotNull);
		}
		fClassToLaunch= classToLaunch;
		fClassPath= classPath;
	}

	/**
	 * Sets the <code>Map</code> that contains String name/value pairs that represent
	 * VM-specific attributes.
	 *
	 * @param map the <code>Map</code> of VM-specific attributes.
	 * @since 2.0
	 */
	public void setVMSpecificAttributesMap(Map<String, Object> map) {
		fVMSpecificAttributesMap = map;
	}

	/**
	 * Sets the custom VM arguments. These arguments will be appended to the list of
	 * VM arguments that a VM runner uses when launching a VM. Typically, these VM arguments
	 * are set by the user.
	 * These arguments will not be interpreted by a VM runner, the client is responsible for
	 * passing arguments compatible with a particular VM runner.
	 *
	 * @param args the list of VM arguments
	 */
	public void setVMArguments(String[] args) {
		if (args == null) {
			throw new IllegalArgumentException(vmRunnerConfig_assert_vmArgsNotNull);
		}
		fVMArgs= args;
	}

	/**
	 * Sets the custom program arguments. These arguments will be appended to the list of
	 * program arguments that a VM runner uses when launching a VM (in general: none).
	 * Typically, these VM arguments are set by the user.
	 * These arguments will not be interpreted by a VM runner, the client is responsible for
	 * passing arguments compatible with a particular VM runner.
	 *
	 * @param args the list of arguments
	 */
	public void setProgramArguments(String[] args) {
		if (args == null) {
			throw new IllegalArgumentException(vmRunnerConfig_assert_programArgsNotNull);
		}
		fProgramArgs= args;
	}

	/**
	 * Sets the environment for the Java program. The Java VM will be
	 * launched in the given environment.
	 *
	 * @param environment the environment for the Java program specified as an array
	 *  of strings, each element specifying an environment variable setting in the
	 *  format <i>name</i>=<i>value</i>
	 * @since 3.0
	 */
	public void setEnvironment(String[] environment) {
		fEnvironment= environment;
	}

	/**
	 * Sets the boot classpath. Note that the boot classpath will be passed to the
	 * VM "as is". This means it has to be complete. Interpretation of the boot class path
	 * is up to the VM runner this object is passed to.
	 * <p>
	 * In release 3.0, support has been added for appending and prepending the
	 * boot classpath. Generally an <code>IVMRunner</code> should use the prepend,
	 * main, and append boot classpaths provided. However, in the case that an
	 * <code>IVMRunner</code> does not support these options, a complete bootpath
	 * should also be specified.
	 * </p>
	 * @param bootClassPath The boot classpath. An empty array indicates an empty
	 *  bootpath and <code>null</code> indicates a default bootpath.
	 */
	public void setBootClassPath(String[] bootClassPath) {
		fBootClassPath= bootClassPath;
	}

	/**
	 * Returns the <code>Map</code> that contains String name/value pairs that represent
	 * VM-specific attributes.
	 *
	 * @return The <code>Map</code> of VM-specific attributes or <code>null</code>.
	 * @since 2.0
	 */
	public Map<String, Object> getVMSpecificAttributesMap() {
		return fVMSpecificAttributesMap;
	}

	/**
	 * Returns the name of the class to launch.
	 *
	 * @return The fully qualified name of the class to launch. Will not be <code>null</code>.
	 */
	public String getClassToLaunch() {
		return fClassToLaunch;
	}

	/**
	 * Returns the classpath.
	 *
	 * @return the classpath
	 */
	public String[] getClassPath() {
		return fClassPath;
	}

	/**
	 * Returns the boot classpath. An empty array indicates an empty
	 * bootpath and <code>null</code> indicates a default bootpath.
	 * <p>
	 * In 3.0, support has been added for prepending and appending to the
	 * boot classpath. The new attributes are stored in the VM specific
	 * attributes map using the following keys defined in
	 * <code>IJavaLaunchConfigurationConstants</code>:
	 * <ul>
	 * <li>ATTR_BOOTPATH_PREPEND</li>
	 * <li>ATTR_BOOTPATH_APPEND</li>
	 * <li>ATTR_BOOTPATH</li>
	 * </ul>
	 * </p>
	 * @return The boot classpath. An empty array indicates an empty
	 *  bootpath and <code>null</code> indicates a default bootpath.
	 * @see #setBootClassPath(String[])
	 * @see IJavaLaunchConfigurationConstants
	 */
	public String[] getBootClassPath() {
		return fBootClassPath;
	}

	/**
	 * Returns the arguments to the VM itself.
	 *
	 * @return The VM arguments. Default is an empty array. Will not be <code>null</code>.
	 * @see #setVMArguments(String[])
	 */
	public String[] getVMArguments() {
		if (fVMArgs == null) {
			return fgEmpty;
		}
		return fVMArgs;
	}

	/**
	 * Returns the arguments to the Java program.
	 *
	 * @return The Java program arguments. Default is an empty array. Will not be <code>null</code>.
	 * @see #setProgramArguments(String[])
	 */
	public String[] getProgramArguments() {
		if (fProgramArgs == null) {
			return fgEmpty;
		}
		return fProgramArgs;
	}

	/**
	 * Returns the environment for the Java program or <code>null</code>
	 *
	 * @return The Java program environment. Default is <code>null</code>
	 * @since 3.0
	 */
	public String[] getEnvironment() {
		return fEnvironment;
	}

	/**
	 * Sets the working directory for a launched VM.
	 *
	 * @param path the absolute path to the working directory
	 *  to be used by a launched VM, or <code>null</code> if
	 *  the default working directory is to be inherited from the
	 *  current process
	 * @since 2.0
	 */
	public void setWorkingDirectory(String path) {
		fWorkingDirectory = path;
	}

	/**
	 * Returns the working directory of a launched VM.
	 *
	 * @return the absolute path to the working directory
	 *  of a launched VM, or <code>null</code> if the working
	 *  directory is inherited from the current process
	 * @since 2.0
	 */
	public String getWorkingDirectory() {
		return fWorkingDirectory;
	}

	/**
	 * Sets whether the VM is resumed on startup when launched in
	 * debug mode. Has no effect when not in debug mode.
	 *
	 * @param resume whether to resume the VM on startup
	 * @since 3.0
	 */
	public void setResumeOnStartup(boolean resume) {
		fResume = resume;
	}

	/**
	 * Returns whether the VM is resumed on startup when launched
	 * in debug mode. Has no effect when no in debug mode. Default
	 * value is <code>true</code> for backwards compatibility.
	 *
	 * @return whether to resume the VM on startup
	 * @since 3.0
	 */
	public boolean isResumeOnStartup() {
		return fResume;
	}

	/**
	 * Sets the modulepath.
	 *
	 * @param modulepath
	 *            modulepath
	 * @since 3.10
	 */
	public void setModulepath(String[] modulepath) {
		this.fModulepath = modulepath;
	}

	/**
	 * Returns the Modulepath.
	 *
	 * @return the modulepath
	 * @since 3.10
	 */
	public String[] getModulepath() {
		return this.fModulepath;
	}

	/**
	 * Sets the fModuleDescription.
	 *
	 * @param fModuleDescription
	 *            fModuleDescription
	 * @since 3.10
	 */
	public void setModuleDescription(String fModuleDescription) {
		this.fModuleDescription = fModuleDescription;
	}

	/**
	 * Returns the ModuleDescription.
	 *
	 * @return the ModuleDescription
	 * @since 3.10
	 */
	public String getModuleDescription() {
		return this.fModuleDescription;
	}

	/**
	 * Gets the fOverrideDependencies.
	 * 
	 * @return the fOverrideDependencies
	 * @since 3.10
	 */
	public String getOverrideDependencies() {
		return fOverrideDependencies;
	}

	/**
	 * Sets the fOverrideDependencies.
	 * 
	 * @param fOverrideDependencies
	 *            the fOverrideDependencies to set
	 * @since 3.10
	 */
	public void setOverrideDependencies(String fOverrideDependencies) {
		this.fOverrideDependencies = fOverrideDependencies;
	}
}