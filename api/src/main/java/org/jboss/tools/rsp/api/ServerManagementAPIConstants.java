/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api;

public interface ServerManagementAPIConstants {

	public static final int STREAM_TYPE_SYSERR = 1;
	public static final int STREAM_TYPE_SYSOUT = 2;
	public static final int STREAM_TYPE_OTHER = 3;
	
	public static final String ATTR_TYPE_BOOL = "bool";
	public static final String ATTR_TYPE_INT = "int";
	public static final String ATTR_TYPE_STRING= "string";
	public static final String ATTR_TYPE_LIST = "list";
	public static final String ATTR_TYPE_MAP = "map";


	/**
	 * Server state constant (value 0) indicating that the
	 * server is in an unknown state.
	 */
	public static final int STATE_UNKNOWN = 0;

	/**
	 * Server state constant (value 1) indicating that the
	 * server is starting, but not yet ready to serve content.
	 */
	public static final int STATE_STARTING = 1;

	/**
	 * Server state constant (value 2) indicating that the
	 * server is ready to serve content.
	 */
	public static final int STATE_STARTED = 2;

	/**
	 * Server state constant (value 3) indicating that the
	 * server is shutting down.
	 */
	public static final int STATE_STOPPING = 3;

	/**
	 * Server state constant (value 4) indicating that the
	 * server is stopped.
	 */
	public static final int STATE_STOPPED = 4;

	/**
	 * Publish state constant (value 0) indicating that it's
	 * in an unknown state.
	 */
	public static final int PUBLISH_STATE_UNKNOWN = 0;

	/**
	 * Publish state constant (value 1) indicating that there
	 * is no publish required.
	 */
	public static final int PUBLISH_STATE_NONE = 1;

	/**
	 * Publish state constant (value 2) indicating that an
	 * incremental publish is required.
	 */
	public static final int PUBLISH_STATE_INCREMENTAL = 2;

	/**
	 * Publish state constant (value 3) indicating that a
	 * full publish is required.
	 */
	public static final int PUBLISH_STATE_FULL = 3;

	/**
	 * Publish kind constant (value 1) indicating an incremental publish request.
	 */
	public static final int PUBLISH_INCREMENTAL = 1;

	/**
	 * Publish kind constant (value 2) indicating a full publish request.
	 */
	public static final int PUBLISH_FULL = 2;

	/**
	 * Publish kind constant (value 3) indicating an automatic publish request.
	 */
	public static final int PUBLISH_AUTO = 3;

	/**
	 * Publish kind constant (value 4) indicating a publish clean request
	 */
	public static final int PUBLISH_CLEAN = 4;
	
	
	
	/*
	 * Debugging details: keys
	 */
	public static final String DEBUG_DETAILS_HOST = "debug.details.host";
	
	public static final String DEBUG_DETAILS_PORT = "debug.details.port";
	
	public static final String DEBUG_DETAILS_TYPE = "debug.details.type";
	
	/*
	 * Standardized server property keys 
	 */
	public static final String SERVER_HOME_DIR = "server.home.dir";

	public static final String SERVER_HOME_FILE = "server.home.file";

}
