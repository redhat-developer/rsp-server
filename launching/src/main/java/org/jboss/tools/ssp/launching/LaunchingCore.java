/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.launching;

import java.io.File;

public class LaunchingCore {
	private static LaunchingCore instance = new LaunchingCore();
	public static LaunchingCore getDefault() {
		return instance;
	}

	public static void log(Throwable t) {
		t.printStackTrace();
	}
	public static void log(String bind) {
		System.out.println(bind);
	}
	
	public static final String SYSPROP_DATA_LOCATION = "org.jboss.tools.ssp.data";
	public static final String SYSPROP_DATA_DEFAULT_LOCATION = ".org.jboss.tools.ssp.data";
	public static File getDataLocation() {
		String prop = System.getProperty(SYSPROP_DATA_LOCATION);
		File ret = null;
		if( prop != null ) {
			ret = new File(prop);
		}
		if( prop == null ) {
			String home = System.getProperty("user.home");
			File home2 = new File(home);
			ret = new File(home2, SYSPROP_DATA_DEFAULT_LOCATION);
		}
		if( ret != null && !ret.exists()) {
			ret.mkdirs();
		}
		return ret;
	}
}
