package org.jboss.tools.rsp.server.minishift.discovery;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.minishift.discovery.MinishiftVersionLoader.MinishiftVersions;

public class MinishiftVersionUtil {
	private static final String MINISHIFT = "Minishift";
	private static final String CDK = "CDK";
	
	private MinishiftVersionUtil() {
		// Intentionally blank 
	}
	
	public static boolean matchesAny(MinishiftVersions versions) {
		if( matchesCDK30(versions) == null || matchesCDK32(versions) == null 
				|| matchesMinishift17(versions) == null )
			return true;
		return false;
	}
	
	
	public static String matchesCDK32(MinishiftVersions versions) {
		String cdkVers = versions.getCDKVersion();
		if (cdkVers != null) {
			if (matchesCDK32(cdkVers)) {
				return null;
			}
			return notCompatible(CDK, cdkVers);
		}
		return cannotDetermine(CDK);
	}
	public static boolean matchesCDK32(String version) {
		return version.startsWith("3.") && !(matchesCDK3(version));
	}
	public static boolean matchesCDK3(String version) {
		return (version.startsWith("3.0.") || version.startsWith("3.1."));
	}

	public static String matchesCDK30(MinishiftVersions versions) {
		String cdkVers = versions.getCDKVersion();
		if (cdkVers == null) {
			return cannotDetermine(CDK);
		}
		if (matchesCDK3(cdkVers)) {
			return null;
		}
		return notCompatible(CDK, cdkVers);
	}
	public static String matchesMinishift17(MinishiftVersions versions) {
		if( versions.getCDKVersion() != null ) {
			return notCompatible(CDK, versions.getCDKVersion());
		}

		String msVers = versions.getMinishiftVersion();
		if (msVers != null) {
			if (matchesMinishift17OrGreater(msVers)) {
				return null;
			}
			return notCompatible(MINISHIFT, msVers);
		}
		return cannotDetermine(MINISHIFT);
	}
	private static String cannotDetermine(String type) {
		return NLS.bind("Cannot determine {0} version.", type);
	}
	
	private static String notCompatible(String type, String vers) {
		return NLS.bind("{0} version {1} is not compatible with this server adapter.", type, vers);
	}
	

	public static boolean matchesMinishift17OrGreater(String version) {
		if (version.contains("+")) {
			String prefix = version.substring(0, version.indexOf("+"));
			String[] segments = prefix.split("\\.");
			if ("1".equals(segments[0]) && Integer.parseInt(segments[1]) >= 7) {
				return true;
			}
		}
		return false;
	}
}
