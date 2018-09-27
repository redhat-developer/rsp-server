/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class LogLevelInitializer {
	
	public static void initLogLevel() {
		String sysprop = System.getProperty(LoggingActivator.SYSPROP_LOG_LEVEL_FLAG);
		if( sysprop != null ) {
			int i = Integer.parseInt(sysprop);
			setLogLevel(i);
		}
	}

	private static void setLogLevel(int level) {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if( level == LoggingActivator.LOG_ERROR ) 
			((ch.qos.logback.classic.Logger)root).setLevel(Level.ERROR);
		else if( level == LoggingActivator.LOG_WARNING ) 
			((ch.qos.logback.classic.Logger)root).setLevel(Level.WARN);
		else if( level == LoggingActivator.LOG_INFO ) 
			((ch.qos.logback.classic.Logger)root).setLevel(Level.INFO);
		else if( level == LoggingActivator.LOG_DEBUG ) 
			((ch.qos.logback.classic.Logger)root).setLevel(Level.DEBUG);
	}
}
