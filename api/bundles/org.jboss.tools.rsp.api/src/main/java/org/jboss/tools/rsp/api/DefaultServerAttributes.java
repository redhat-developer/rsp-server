/*******************************************************************************
 * Copyright (c) 2019, 2024 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api;

/**
 * A list of server attributes common to all (or most) servers,
 * or any attributes that are treated as first-class citizens 
 * with support in the framework itself, rather than provided by or
 * for specific adapters.
 */
public interface DefaultServerAttributes {

	/*
	 * Standardized server property keys 
	 */
	public static final String SERVER_HOME_DIR = "server.home.dir";
	public static final String SERVER_HOME_FILE = "server.home.file";
	public static final String AUTOPUBLISH_ENABLEMENT = "server.autopublish.enabled";
	public static final String AUTOPUBLISH_INACTIVITY_LIMIT = "server.autopublish.inactivity.limit";
	public static final boolean AUTOPUBLISH_ENABLEMENT_DEFAULT = true;
	public static final int AUTOPUBLISH_INACTIVITY_LIMIT_DEFAULT = 1000;
	
	public static final String SERVER_TIMEOUT_STARTUP = "server.timeout.startup";
	public static final String SERVER_TIMEOUT_SHUTDOWN = "server.timeout.shutdown";
	public static final int DEFAULT_SERVER_TRANSITION_TIMEOUT = 2*60*1000;

	
	/*
	 * Deployment options 
	 */
	public static final String DEPLOYMENT_OPTION_OUTPUT_NAME = "deployment.output.name";
	public static final String DEPLOYMENT_OPTION_ASSEMBLY_FILE = "deployment.assembly.file";
	public static final String DEPLOYMENT_OPTION_ASSEMBLY_FILE_DEFAULT = "rsp.assembly.json";
	

}
