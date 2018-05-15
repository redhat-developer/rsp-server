package org.jboss.tools.ssp.eclipse.debug.core;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.ssp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.ssp.launching.util.OSUtils;

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

	private static String[] parseArgumentsWindows(String args, boolean split) {
		// see http://msdn.microsoft.com/en-us/library/a1y7w461.aspx
		List<String> result = new ArrayList<String>();

		final int DEFAULT= 0;
		final int ARG= 1;
		final int IN_DOUBLE_QUOTE= 2;

		int state= DEFAULT;
		int backslashes= 0;
		StringBuffer buf= new StringBuffer();
		int len= args.length();
		for (int i= 0; i < len; i++) {
			char ch= args.charAt(i);
			if (ch == '\\') {
				backslashes++;
				continue;
			} else if (backslashes != 0) {
				if (ch == '"') {
					for (; backslashes >= 2; backslashes-= 2) {
						buf.append('\\');
						if (split) {
							buf.append('\\');
						}
					}
					if (backslashes == 1) {
						if (state == DEFAULT) {
							state= ARG;
						}
						if (split) {
							buf.append('\\');
						}
						buf.append('"');
						backslashes= 0;
						continue;
					} // else fall through to switch
				} else {
					// false alarm, treat passed backslashes literally...
					if (state == DEFAULT) {
						state= ARG;
					}
					for (; backslashes > 0; backslashes--) {
						buf.append('\\');
					}
					// fall through to switch
				}
			}
			if (Character.isWhitespace(ch)) {
				if (state == DEFAULT) {
					// skip
					continue;
				} else if (state == ARG) {
					state= DEFAULT;
					result.add(buf.toString());
					buf.setLength(0);
					continue;
				}
			}
			switch (state) {
				case DEFAULT:
				case ARG:
					if (ch == '"') {
						state= IN_DOUBLE_QUOTE;
						if (split) {
							buf.append(ch);
						}
					} else {
						state= ARG;
						buf.append(ch);
					}
					break;

				case IN_DOUBLE_QUOTE:
					if (ch == '"') {
						if (i + 1 < len && args.charAt(i + 1) == '"') {
							/* Undocumented feature in Windows:
							 * Two consecutive double quotes inside a double-quoted argument are interpreted as
							 * a single double quote.
							 */
							buf.append('"');
							i++;
							if (split) {
								buf.append(ch);
							}
						} else if (buf.length() == 0) {
							// empty string on Windows platform. Account for bug in constructor of JDK's java.lang.ProcessImpl.
							result.add("\"\""); //$NON-NLS-1$
							state= DEFAULT;
						} else {
							state= ARG;
							if (split) {
								buf.append(ch);
							}
						}
					} else {
						buf.append(ch);
					}
					break;

				default:
					throw new IllegalStateException();
			}
		}
		if (buf.length() > 0 || state != DEFAULT) {
			result.add(buf.toString());
		}

		return result.toArray(new String[result.size()]);
	}

	private static String[] parseArgumentsImpl(String args, boolean split) {
		// man sh, see topic QUOTING
		List<String> result = new ArrayList<String>();

		final int DEFAULT= 0;
		final int ARG= 1;
		final int IN_DOUBLE_QUOTE= 2;
		final int IN_SINGLE_QUOTE= 3;

		int state= DEFAULT;
		StringBuffer buf= new StringBuffer();
		int len= args.length();
		for (int i= 0; i < len; i++) {
			char ch= args.charAt(i);
			if (Character.isWhitespace(ch)) {
				if (state == DEFAULT) {
					// skip
					continue;
				} else if (state == ARG) {
					state= DEFAULT;
					result.add(buf.toString());
					buf.setLength(0);
					continue;
				}
			}
			switch (state) {
				case DEFAULT:
				case ARG:
					if (ch == '"') {
						if (split) {
							buf.append(ch);
						}
						state= IN_DOUBLE_QUOTE;
					} else if (ch == '\'') {
						if (split) {
							buf.append(ch);
						}
						state= IN_SINGLE_QUOTE;
					} else if (ch == '\\' && i + 1 < len) {
						if (split) {
							buf.append(ch);
						}
						state= ARG;
						ch= args.charAt(++i);
						buf.append(ch);
					} else {
						state= ARG;
						buf.append(ch);
					}
					break;

				case IN_DOUBLE_QUOTE:
					if (ch == '"') {
						if (split) {
							buf.append(ch);
						}
						state= ARG;
					} else if (ch == '\\' && i + 1 < len &&
							(args.charAt(i + 1) == '\\' || args.charAt(i + 1) == '"')) {
						if (split) {
							buf.append(ch);
						}
						ch= args.charAt(++i);
						buf.append(ch);
					} else {
						buf.append(ch);
					}
					break;

				case IN_SINGLE_QUOTE:
					if (ch == '\'') {
						if (split) {
							buf.append(ch);
						}
						state= ARG;
					} else {
						buf.append(ch);
					}
					break;

				default:
					throw new IllegalStateException();
			}
		}
		if (buf.length() > 0 || state != DEFAULT) {
			result.add(buf.toString());
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Parses the given command line into separate arguments that can be passed
	 * to <code>DebugPlugin.exec(String[], File)</code>. Embedded quotes and
	 * backslashes are interpreted, i.e. the resulting arguments are in the form
	 * that will be passed to an invoked process.
	 * <p>
	 * The reverse operation is {@link #renderArguments(String[], int[])}.
	 * </p>
	 *
	 * @param args command line arguments as a single string
	 * @return individual arguments
	 * @see #renderArguments(String[], int[])
	 * @since 3.1
	 */
	public static String[] parseArguments(String args) {
		if (args == null) {
			return new String[0];
		}

		if (OSUtils.isWindows()) {
			return parseArgumentsWindows(args, false);
		}

		return parseArgumentsImpl(args, false);
	}

	/**
	 * Splits the given command line into separate arguments that can be
	 * concatenated with a space as joiner. Embedded quotes and backslashes are
	 * kept as is (i.e. not interpreted).
	 * <p>
	 * Use this method to avoid e.g. losing quotes around an argument like
	 * <code>"${env_var:A}"</code>, which may later be substituted by a string
	 * that contains spaces.
	 * </p>
	 *
	 * @param args command line arguments as a single string
	 * @return individual arguments in original form
	 * @since 3.10
	 */
	public static String[] splitArguments(String args) {
		if (args == null) {
			return new String[0];
		}

		if (OSUtils.isWindows()) {
			return parseArgumentsWindows(args, true);
		}

		return parseArgumentsImpl(args, true);
	}

	/**
	 * Renders the given array of argument strings into a single command line.
	 * <p>
	 * If an argument contains whitespace, it it quoted. Contained quotes or
	 * backslashes will be escaped.
	 * </p>
	 * <p>
	 * If <code>segments</code> is not <code>null</code>, the array is filled
	 * with the offsets of the start positions of arguments 1 to
	 * <code>arguments.length - 1</code>, as rendered in the resulting string.
	 * </p>
	 *
	 * @param arguments the command line arguments
	 * @param segments an array of size <code>arguments.length - 1</code> or
	 *            <code>null</code>
	 * @return the command line
	 * @see #parseArguments(String)
	 * @since 3.8
	 */
	public static String renderArguments(String[] arguments, int[] segments) {
		boolean isWin32= OSUtils.isWindows();
		StringBuffer buf = new StringBuffer();
		int count = arguments.length;
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				buf.append(' ');
			}

			boolean containsSpace = false;
			char[] characters = arguments[i].toCharArray();
			for (int j = 0; j < characters.length; j++) {
				char ch = characters[j];
				if (ch == ' ' || ch == '\t') {
					containsSpace = true;
					buf.append('"');
					break;
				}
			}

			int backslashes = 0;
			for (int j = 0; j < characters.length; j++) {
				char ch = characters[j];
				if (ch == '"') {
					if (isWin32) {
						if (j == 0 && characters.length == 2 && characters[1] == '"') {
							// empty string on windows platform, see bug 130767. Bug in constructor of JDK's java.lang.ProcessImpl.
							buf.append("\"\""); //$NON-NLS-1$
							break;
						}
						if (backslashes > 0) {
							// Feature in Windows: need to double-escape backslashes in front of double quote.
							for (; backslashes > 0; backslashes--) {
								buf.append('\\');
							}
						}
					}
					buf.append('\\');
				} else if (ch == '\\') {
					if (isWin32) {
						backslashes++;
					} else {
						buf.append('\\');
					}
				}
				buf.append(ch);
			}
			if (containsSpace) {
				buf.append('"');
			} else if (characters.length == 0) {
				buf.append("\"\""); //$NON-NLS-1$
			}

			if (segments != null && i < count - 1) {
				segments[i] = buf.length() + 1;
			}
		}
		return buf.toString();
	}
}
