/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import org.jboss.tools.ssp.server.wildfly.beans.impl.IServerConstants;

public interface ServerTypeStringConstants {
	public static final String WF12_ID = IServerConstants.SERVER_WILDFLY_120;
	public static final String WF12_NAME = "WildFly 12.x";
	public static final String WF12_DESC = "A server adapter capable of discovering and controlling a WildFly 12.x runtime instance.";

	public static final String WF11_ID = IServerConstants.SERVER_WILDFLY_110;
	public static final String WF11_NAME = "WildFly 11.x";
	public static final String WF11_DESC = "A server adapter capable of discovering and controlling a WildFly 11.x runtime instance.";

	public static final String WF10_ID = IServerConstants.SERVER_WILDFLY_100;
	public static final String WF10_NAME = "WildFly 10.x";
	public static final String WF10_DESC = "A server adapter capable of discovering and controlling a WildFly 10.x runtime instance.";

	public static final String WF9_ID = IServerConstants.SERVER_WILDFLY_90;
	public static final String WF9_NAME = "WildFly 9.x";
	public static final String WF9_DESC = "A server adapter capable of discovering and controlling a WildFly 9.x runtime instance.";

	public static final String WF8_ID = IServerConstants.SERVER_WILDFLY_80;
	public static final String WF8_NAME = "WildFly 8.x";
	public static final String WF8_DESC = "A server adapter capable of discovering and controlling a WildFly 8.x runtime instance.";

	public static final String WF71_ID = IServerConstants.SERVER_AS_71;
	public static final String WF71_NAME = "JBoss AS 7.1";
	public static final String WF71_DESC = "A server adapter capable of discovering and controlling a JBoss 7.1 runtime instance.";

	public static final String WF70_ID = IServerConstants.SERVER_AS_70;
	public static final String WF70_NAME = "JBoss AS 7.0";
	public static final String WF70_DESC = "A server adapter capable of discovering and controlling a JBoss 7.0 runtime instance.";

	public static final String AS60_ID = IServerConstants.SERVER_AS_60;
	public static final String AS60_NAME = "JBoss AS 6.0";
	public static final String AS60_DESC = "A server adapter capable of discovering and controlling a JBoss 6.0 runtime instance.";

	public static final String AS51_ID = IServerConstants.SERVER_AS_51;
	public static final String AS51_NAME = "JBoss AS 5.1";
	public static final String AS51_DESC = "A server adapter capable of discovering and controlling a JBoss 5.1 runtime instance.";

	public static final String AS50_ID = IServerConstants.SERVER_AS_50;
	public static final String AS50_NAME = "JBoss AS 5.0";
	public static final String AS50_DESC = "A server adapter capable of discovering and controlling a JBoss 5.0 runtime instance.";

	public static final String AS42_ID = IServerConstants.SERVER_AS_42;
	public static final String AS42_NAME = "JBoss AS 4.2";
	public static final String AS42_DESC = "A server adapter capable of discovering and controlling a JBoss 4.2 runtime instance.";

	public static final String AS40_ID = IServerConstants.SERVER_AS_40;
	public static final String AS40_NAME = "JBoss AS 4.0";
	public static final String AS40_DESC = "A server adapter capable of discovering and controlling a JBoss 4.0 runtime instance.";

	public static final String AS32_ID = IServerConstants.SERVER_AS_32;
	public static final String AS32_NAME = "JBoss AS 3.2";
	public static final String AS32_DESC = "A server adapter capable of discovering and controlling a JBoss 3.2 runtime instance.";
}
