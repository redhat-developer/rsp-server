/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class LogLevelInitializer {
	private static final Logger LOG = LoggerFactory.getLogger(LogLevelInitializer.class);
	public static void initLogLevel() {
		String sysprop = System.getProperty(LoggingConstants.SYSPROP_LOG_LEVEL_FLAG);
		if( sysprop != null ) {
			try {
				int i = Integer.parseInt(sysprop);
				setLogLevel(i);
			} catch(NumberFormatException nfe) {
				LOG.error("Unable to read system property for log level (" + LoggingConstants.SYSPROP_LOG_LEVEL_FLAG + ")", nfe);
			}
		}
	}

	private static void setLogLevel(int level) {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if( level == LoggingConstants.LOG_ERROR ) 
			((ch.qos.logback.classic.Logger)root).setLevel(Level.ERROR);
		else if( level == LoggingConstants.LOG_WARNING ) 
			((ch.qos.logback.classic.Logger)root).setLevel(Level.WARN);
		else if( level == LoggingConstants.LOG_INFO ) 
			((ch.qos.logback.classic.Logger)root).setLevel(Level.INFO);
		else if( level == LoggingConstants.LOG_DEBUG ) 
			((ch.qos.logback.classic.Logger)root).setLevel(Level.DEBUG);
	}
}
