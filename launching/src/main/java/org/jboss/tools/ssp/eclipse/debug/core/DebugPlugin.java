package org.jboss.tools.ssp.eclipse.debug.core;

import org.jboss.tools.ssp.eclipse.debug.core.model.IProcess;

public class DebugPlugin {
	/**
	 * Unique identifier constant (value <code>"org.eclipse.debug.core"</code>)
	 * for the Debug Core plug-in.
	 */
	private static final String PI_DEBUG_CORE = "org.eclipse.debug.core"; //$NON-NLS-1$


	/**
	 * The launch attribute that designates whether or not it's associated
	 * launch should capture output. Value is a string representing a boolean -
	 * <code>true</code> or <code>false</code>. When unspecified, the default
	 * value is considered <code>true</code>.
	 *
	 * @since 3.1
	 */
	public static final String ATTR_CAPTURE_OUTPUT = PI_DEBUG_CORE + ".capture_output"; //$NON-NLS-1$

	/**
	 * The launch attribute that stores the time stamp of when a launch configuration was
	 * launched. Value is {@link Long#toString(long)} of {@link System#currentTimeMillis()}.
	 *
	 * @since 3.6
	 */
	public static final String ATTR_LAUNCH_TIMESTAMP = PI_DEBUG_CORE + ".launch.timestamp";  //$NON-NLS-1$


    /**
     * This launch attribute designates the encoding to be used by the console
     * associated with the launch.
     * <p>
     * For release 3.3, the system encoding is used when unspecified. Since 3.4,
     * the inherited encoding is used when unspecified. See {@link ILaunchManager} for a
     * description in <code>getEncoding(ILaunchConfiguration)</code>.
     * </p>
     * <p>
     * Value of this constant is the same as the value of the old
     * <code>IDebugUIConstants.ATTR_CONSOLE_ENCODING</code> constant for backward
     * compatibility.
     * </p>
     * @since 3.3
     */
	public static final String ATTR_CONSOLE_ENCODING = "org.eclipse.debug.ui.ATTR_CONSOLE_ENCODING"; //$NON-NLS-1$
	
	/**
	 * Attribute key for the environment used when an {@link IProcess} was run
	 * @see IProcess
	 * @since 3.8
	 */
	public static final String ATTR_ENVIRONMENT = PI_DEBUG_CORE + ".ATTR_ENVIRONMENT"; //$NON-NLS-1$

	/**
	 * Attribute key for the path of the working directory for an {@link IProcess}
	 *
	 * @see IProcess
	 * @since 3.8
	 */
	public static final String ATTR_WORKING_DIRECTORY = PI_DEBUG_CORE + ".ATTR_WORKING_DIRECTORY"; //$NON-NLS-1$

	/**
	 * Attribute key for path of the executable that launched an {@link IProcess}
	 *
	 * @see IProcess
	 * @since 3.8
	 */
	public static final String ATTR_PATH = PI_DEBUG_CORE + ".ATTR_PATH"; //$NON-NLS-1$

	/**
	 * Convenience method which returns the unique identifier of this plug-in.
	 *
	 * @return debug plug-in identifier
	 */
	public static String getUniqueIdentifier() {
		return PI_DEBUG_CORE;
	}
}
