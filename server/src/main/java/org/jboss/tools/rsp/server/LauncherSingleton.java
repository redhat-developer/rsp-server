/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

/**
 * This application (for now) can be launched one of two ways:
 *   1) In an OSGi environment, or, 
 *   2) via a main class that instantiates a launcher object. 
 *   
 * This class exists for a few purposes. It's primary purpose is to hold a 
 * single launcher. Regardless of how the application is started, there should
 * not be more than one launcher running at a time. This class holds that reference. 
 * 
 * If launched via OSGi, the activator should instantiate the launcher and set it here.
 * If launched via a main class, that class should instantiate the launcher and set it here.
 * 
 */
public class LauncherSingleton {
	private static LauncherSingleton instance = new LauncherSingleton();
	public static LauncherSingleton getDefault() {
		return instance;
	}
	
	private ServerManagementServerLauncher launcher;
	public ServerManagementServerLauncher getLauncher() {
		return launcher;
	}
	
	public void setLauncher(ServerManagementServerLauncher launcher) {
		this.launcher = launcher;
	}
}
