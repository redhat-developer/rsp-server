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
		if (versionIDs.length > 0 && versionIDs[0] >= 9) {
			suffix = " --add-exports=java.base/sun.nio.ch=ALL-UNNAMED"
					+ " --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED"
					+ " --add-exports=jdk.unsupported/sun.reflect=ALL-UNNAMED"
					+ " --add-modules=java.se";
		}
		return suffix;
	}
}
