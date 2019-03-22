package org.jboss.tools.rsp.server.wildfly.servertype.launch;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;

public class Java9LaunchArgUtil {

	public static String getJava9VMArgs(IVMInstall vm) {
		String suffix = "";
		if( vm == null )
			return suffix;
		
		int[] versionIDs = getMajorMinor(vm.getJavaVersion());
		if (versionIDs.length > 0 && versionIDs[0] >= 9) {
			suffix = " --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED --add-exports=jdk.unsupported/sun.reflect=ALL-UNNAMED --add-modules=java.se";
		}
		return suffix;
	}
	
	/**
	 * Accept a non-null string as per the following document:
	 * https://blogs.oracle.com/java-platform-group/a-new-jdk-9-version-string-
	 * scheme
	 * 
	 * Strings may or may not have all segments, however, the returned array will be
	 * of length 2.
	 * 
	 * @param version
	 * 
	 * @return a integer array of length 2 representing the major+minor version of
	 * the string
	 */
	public static int[] getMajorMinor(String version) {

		if (version == null)
			return new int[] { -1, -1 };

		int pos = version.indexOf('.');
		if (pos == -1) {
			return new int[] { Integer.parseInt(version), 0 };
		}
		String[] split = version.split("\\.");
		if (split != null && split.length > 1) {
			try {
				return new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]) };
			} catch (NumberFormatException nfe) {
			}
		}
		return new int[] { -1, -1 };
	}
}
