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
package org.jboss.tools.rsp.eclipse.debug.core;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.launching.utils.OSUtils;

public class ArgumentUtils {
	
	private ArgumentUtils() {
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
}
