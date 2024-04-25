/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.launch;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util.JavaUtils;

public class Java9LaunchArgUtil {

	private Java9LaunchArgUtil() {
		// prevent instantiation
	}
	
	public static String getJava9VMArgs(IVMInstall vm) {
		String suffix = "";
		if( vm == null )
			return suffix;

		int[] versionIDs = JavaUtils.getMajorMinorVersion(vm.getJavaVersion());
		if (versionIDs.length > 0 ) {
			if (versionIDs[0] >= 17) {
				suffix = " --add-exports=java.desktop/sun.awt=ALL-UNNAMED --add-exports=java.naming/com.sun.jndi.ldap=ALL-UNNAMED --add-exports=java.naming/com.sun.jndi.url.ldap=ALL-UNNAMED --add-exports=java.naming/com.sun.jndi.url.ldaps=ALL-UNNAMED --add-exports=jdk.naming.dns/com.sun.jndi.dns=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.management/javax.management=ALL-UNNAMED --add-opens=java.naming/javax.naming=ALL-UNNAMED";
			} else if (versionIDs[0] >= 9) {
				suffix = " --add-exports=java.base/sun.nio.ch=ALL-UNNAMED"
						+ " --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED"
						+ " --add-exports=jdk.unsupported/sun.reflect=ALL-UNNAMED"
						+ " --add-modules=java.se";
			}
		}
		return suffix;
	}
}
