package org.jboss.tools.ssp.launching.util;

public final class OSUtils {
	private static String OS = System.getProperty("os.name").toLowerCase();

	public static String getOsName() {
		if (OS == null) {
			OS = System.getProperty("os.name");
		}
		return OS;
	}

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}

	public static boolean isUnknown() {
		return !isWindows() && !isMac() && !isUnix() && !isSolaris();
	}
}