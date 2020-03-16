/******************************************************************************* 
 * Copyright (c) 2019 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.Wildfly100DefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.Wildfly110DefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.Wildfly80DefaultLaunchArguments;

public class WildFlyExtendedProperties {
	private WildFlyExtendedProperties() {
		// Do nothing
	}
	
	private static final String HTTP_REMOTING_JMX_OLD = "service:jmx:http-remoting-jmx";
	private static final String HTTP_REMOTING_JMX_NEW = "service:jmx:remote+http";
	public static class Wildfly80ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly80ExtendedProperties(IServer server) {
			super("8.x", "1.7", "1.8", HTTP_REMOTING_JMX_OLD, new Wildfly80DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly90ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly90ExtendedProperties(IServer server) {
			super("9.x", "1.7", "1.8", HTTP_REMOTING_JMX_NEW, new Wildfly80DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly100ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly100ExtendedProperties(IServer server) {
			super("10.x", "1.8", "1.8", HTTP_REMOTING_JMX_NEW, new Wildfly100DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly110ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly110ExtendedProperties(IServer server) {
			super("11.0", "1.8", "9.", HTTP_REMOTING_JMX_NEW, new Wildfly100DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly120ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly120ExtendedProperties(IServer server) {
			super("12.0", "1.8", "10.", HTTP_REMOTING_JMX_NEW, new Wildfly100DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly130ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly130ExtendedProperties(IServer server) {
			super("13.0", "1.8", "10.", HTTP_REMOTING_JMX_NEW, new Wildfly110DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly140ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly140ExtendedProperties(IServer server) {
			super("14", "1.8", "10.", HTTP_REMOTING_JMX_NEW, new Wildfly110DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly150ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly150ExtendedProperties(IServer server) {
			super("15.0", "1.8", "11.", HTTP_REMOTING_JMX_NEW, new Wildfly110DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly160ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly160ExtendedProperties(IServer server) {
			super("16.0", "1.8", "12.", HTTP_REMOTING_JMX_NEW, new Wildfly110DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly170ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly170ExtendedProperties(IServer server) {
			super("17.0", "1.8", "13.", HTTP_REMOTING_JMX_NEW, new Wildfly110DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly180ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly180ExtendedProperties(IServer server) {
			super("18.0", "1.8", "13.", HTTP_REMOTING_JMX_NEW, new Wildfly110DefaultLaunchArguments(server), server);
		}
	}
	public static class Wildfly190ExtendedProperties extends AbstractWildflyExtendedProperties {
		public Wildfly190ExtendedProperties(IServer server) {
			super("19.0", "1.8", "13.", HTTP_REMOTING_JMX_NEW, new Wildfly110DefaultLaunchArguments(server), server);
		}
	}
	// NEW_SERVER_ADAPTER 
}
