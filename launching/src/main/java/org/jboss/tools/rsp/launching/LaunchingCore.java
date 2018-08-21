/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching;

import java.io.File;

public class LaunchingCore {

	public static final String SYSPROP_DATA_LOCATION = "org.jboss.tools.rsp.data";
	public static final String SYSPROP_DATA_DEFAULT_LOCATION = ".org.jboss.tools.rsp.data";
	private static final String SYSPROP_USER_HOME = "user.home";

	private static final LaunchingCore instance = new LaunchingCore();
	
	private LaunchingCore() {
	}

	public static LaunchingCore getDefault() {
		return instance;
	}

	public static void log(Throwable t) {
		t.printStackTrace();
	}

	public static void log(String bind) {
		System.out.println(bind);
	}
	
	public static File getDataLocation() {
		File data = null;
		String prop = System.getProperty(SYSPROP_DATA_LOCATION);

		if (prop != null) {
			data = new File(prop);
		} else {
			File home = new File(System.getProperty(SYSPROP_USER_HOME));
			data = new File(home, SYSPROP_DATA_DEFAULT_LOCATION);
		}

		if (data != null && !data.exists()) {
			data.mkdirs();
		}

		return data;
	}
}
