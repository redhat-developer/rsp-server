package org.jboss.tools.rsp.server;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RSPFlagTest {
	private String originalLogLevel;
	private String originalServerPort;
	
	@Before
	public void before() {
		originalLogLevel = System.getProperty(RSPFlags.SYSPROP_LOG_LEVEL_FLAG);
		originalServerPort = System.getProperty(RSPFlags.SYSPROP_SERVER_PORT);
	}
	
	@After
	public void after() {
		if( originalLogLevel == null )
			System.clearProperty(RSPFlags.SYSPROP_LOG_LEVEL_FLAG);
		else
			System.setProperty(RSPFlags.SYSPROP_LOG_LEVEL_FLAG, originalLogLevel);
		
		if( originalServerPort == null ) 
			System.clearProperty(RSPFlags.SYSPROP_SERVER_PORT);
		else
			System.setProperty(RSPFlags.SYSPROP_SERVER_PORT, originalServerPort);
	}
	
	@Test
	public void testLogLevel() {
		System.clearProperty(RSPFlags.SYSPROP_LOG_LEVEL_FLAG);
		assertEquals(RSPLogger.LOG_WARNING, RSPFlags.getLogLevel());
		System.setProperty(RSPFlags.SYSPROP_LOG_LEVEL_FLAG, Integer.toString(RSPLogger.LOG_DEBUG));
		assertEquals(RSPLogger.LOG_DEBUG, RSPFlags.getLogLevel());
		System.setProperty(RSPFlags.SYSPROP_LOG_LEVEL_FLAG, Integer.toString(RSPLogger.LOG_ERROR));
		assertEquals(RSPLogger.LOG_ERROR, RSPFlags.getLogLevel());
	}
	
	@Test
	public void testServerPort() {
		System.clearProperty(RSPFlags.SYSPROP_SERVER_PORT);
		assertEquals(RSPFlags.DEFAULT_PORT, RSPFlags.getServerPort());
		System.setProperty(RSPFlags.SYSPROP_SERVER_PORT, Integer.toString(48897));
		assertEquals(48897, RSPFlags.getServerPort());
		System.setProperty(RSPFlags.SYSPROP_SERVER_PORT, Integer.toString(10305));
		assertEquals(10305, RSPFlags.getServerPort());
	}
	
}
