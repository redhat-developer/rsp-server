/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

public class RSPLogger {
	public static final int LOG_ERROR = LogService.LOG_ERROR;
	/**
	 * A warning message (Value 2).
	 * 
	 * <p>
	 * This log entry indicates a bundle or service is still functioning but may
	 * experience problems in the future because of the warning condition.
	 */
	public static final int LOG_WARNING = LogService.LOG_WARNING;
	/**
	 * An informational message (Value 3).
	 * 
	 * <p>
	 * This log entry may be the result of any change in the bundle or service and
	 * does not indicate a problem.
	 */
	public static final int LOG_INFO = LogService.LOG_INFO;
	/**
	 * A debugging message (Value 4).
	 * 
	 * <p>
	 * This log entry is used for problem determination and may be irrelevant to
	 * anyone but the bundle developer.
	 */
	public static final int LOG_DEBUG = LogService.LOG_DEBUG;

	private interface IRSPLogger {
		public void log(int level, String message);

		public void log(int level, String message, Throwable exception);

		public void dispose();
	}

	private static IRSPLogger rspLogger;
	private static int logLevel = LogService.LOG_WARNING;

	static {
		setLogLevelFromFlags();
	}
	
	
	public static IRSPLogger getLogger() {
		if (rspLogger == null) {
			useSysout();
		}
		return rspLogger;
	}

	public static void setLogLevel(int level) {
		logLevel = level;
	}

	public static void useService() {
		if( rspLogger != null ) 
			rspLogger.dispose();
		rspLogger = new ServiceLogger();
	}

	private static class ServiceLogger implements IRSPLogger {
		private LogService log = null;
		private LogListener listener = null;
		private LogReaderService logReader;

		public ServiceLogger() {
			log = getLogService();
			if (log != null) {
				listener = new LogListener() {
					@Override
					public void logged(LogEntry entry) {
						if (entry.getLevel() <= logLevel) {
							System.out.println(formatMessage(entry.getLevel(), entry.getTime(), entry.getMessage()));
						}
					}

				};
				logReader = getService(LogReaderService.class);
				if (logReader != null) {
					logReader.addLogListener(listener);
				}
			}
		}

		@Override
		public void log(int level, String message) {
			log.log(level, message);
		}

		@Override
		public void log(int level, String message, Throwable exception) {
			log.log(level, message, exception);
		}

		@Override
		public void dispose() {
			if (logReader != null && listener != null) {
				logReader.removeLogListener(listener);
			}
		}

	}

	private static String formatMessage(int level, long timestamp, String message) {
		String m2 = new StringBuilder().append("[").append(level).append("] ")
				.append(timestamp).append(": ").append(message).toString();
		return m2;
	}
	
	public static void useSysout() {
		if( rspLogger != null ) 
			rspLogger.dispose();
		
		rspLogger = new IRSPLogger() {
			@Override
			public void log(int level, String message) {
				if (level <= logLevel) {
					System.out.println(formatMessage(level,  System.currentTimeMillis(), message));
				}
			}

			@Override
			public void log(int level, String message, Throwable exception) {
				if (level <= logLevel) {
					System.out.println(formatMessage(level,  System.currentTimeMillis(), message));
					exception.printStackTrace();
				}
			}

			@Override
			public void dispose() {
			}

		};
	}

	private static LogService getLogService() {
		BundleContext bc = ServerCoreActivator.getDefaultContext();
		if (bc == null)
			return null;
		ServiceReference ref = bc.getServiceReference(LogService.class.getName());
		if (ref != null) {
			LogService log = (LogService) bc.getService(ref);
			return log;
		}
		return null;
	}

	private static <T> T getService(Class<T> clazz) {
		BundleContext bc = ServerCoreActivator.getDefaultContext();
		if (bc == null)
			return null;
		ServiceReference<?> ref = bc.getServiceReference(clazz.getName());
		if (ref != null)
			return (T) bc.getService(ref);
		return null;
	}

	public static void log(int level, String message) {
		getLogger().log(level, message);
	}

	public static void log(int level, String message, Throwable exception) {
		getLogger().log(level, message, exception);
	}

	private static void setLogLevelFromFlags() {
		RSPLogger.setLogLevel(RSPFlags.getLogLevel());
	}
}
