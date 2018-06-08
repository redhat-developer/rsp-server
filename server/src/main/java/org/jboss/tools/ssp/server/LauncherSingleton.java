/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server;

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
