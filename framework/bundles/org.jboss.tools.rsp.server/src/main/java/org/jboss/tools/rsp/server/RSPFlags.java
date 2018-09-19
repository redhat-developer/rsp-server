/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

public class RSPFlags {
	public static final String SYSPROP_LOG_LEVEL_FLAG = "rsp.log.level";
	public static final String SYSPROP_SERVER_PORT = "rsp.server.port";
	
	public static final int DEFAULT_PORT = 27511;
	
	public static int getLogLevel() {
		return getIntSysprop(SYSPROP_LOG_LEVEL_FLAG, RSPLogger.LOG_WARNING);
	}

	public static int getServerPort() {
		return getIntSysprop(SYSPROP_SERVER_PORT, DEFAULT_PORT);
	}

	public static int getIntSysprop(String key, int def) {
		int logLevel = def;
		String logLevelTmp = System.getProperty(key);
		if( logLevelTmp != null ) {
			try {
				logLevel = Integer.parseInt(logLevelTmp);
			} catch(NumberFormatException nfe) {
				// ignore
			}
		}
		return logLevel;
	}
}
