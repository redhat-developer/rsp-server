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
