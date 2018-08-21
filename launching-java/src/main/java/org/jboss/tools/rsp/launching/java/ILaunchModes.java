/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.launching.java;

public interface ILaunchModes {
	/**
	 * A launch mode indicating a simple run. 
	 */
	public static final String RUN = "run";
	
	/**
	 * A launch mode indicating a debug launch. 
	 * Debugging flags will be added to the launch command
	 * to ensure a client's debugger can connect.
	 */
	public static final String DEBUG = "debug";
	
	public static final String RUN_DESC = "A launch mode indicating a simple run.";
	public static final String DEBUG_DESC = "A launch mode indicating a debug launch, which can add the appropriate debugging flags or system properties as required.";
	
}
