/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import org.jboss.tools.rsp.server.wildfly.servertype.BaseJBossServerType;

public class WildFlyServerTypes implements ServerTypeStringConstants{
	// NEW_SERVER_ADAPTER
	public static final BaseJBossServerType WF24_SERVER_TYPE = 
			new WildFlyServerType(WF24_ID, WF24_NAME, WF24_DESC);
	public static final BaseJBossServerType WF23_SERVER_TYPE = 
			new WildFlyServerType(WF23_ID, WF23_NAME, WF23_DESC);
	public static final BaseJBossServerType WF22_SERVER_TYPE = 
			new WildFlyServerType(WF22_ID, WF22_NAME, WF22_DESC);
	public static final BaseJBossServerType WF21_SERVER_TYPE = 
			new WildFlyServerType(WF21_ID, WF21_NAME, WF21_DESC);
	public static final BaseJBossServerType WF20_SERVER_TYPE = 
			new WildFlyServerType(WF20_ID, WF20_NAME, WF20_DESC);
	public static final BaseJBossServerType WF19_SERVER_TYPE = 
			new WildFlyServerType(WF19_ID, WF19_NAME, WF19_DESC);
	public static final BaseJBossServerType WF18_SERVER_TYPE = 
			new WildFlyServerType(WF18_ID, WF18_NAME, WF18_DESC);
	public static final BaseJBossServerType WF17_SERVER_TYPE = 
			new WildFlyServerType(WF17_ID, WF17_NAME, WF17_DESC);
	public static final BaseJBossServerType WF16_SERVER_TYPE = 
			new WildFlyServerType(WF16_ID, WF16_NAME, WF16_DESC);
	public static final BaseJBossServerType WF15_SERVER_TYPE = 
			new WildFlyServerType(WF15_ID, WF15_NAME, WF15_DESC);
	public static final BaseJBossServerType WF14_SERVER_TYPE = 
			new WildFlyServerType(WF14_ID, WF14_NAME, WF14_DESC);
	public static final BaseJBossServerType WF13_SERVER_TYPE = 
			new WildFlyServerType(WF13_ID, WF13_NAME, WF13_DESC);
	public static final BaseJBossServerType WF12_SERVER_TYPE = 
			new WildFlyServerType(WF12_ID, WF12_NAME, WF12_DESC);
	public static final BaseJBossServerType WF11_SERVER_TYPE = 
			new WildFlyServerType(WF11_ID, WF11_NAME, WF11_DESC);
	public static final BaseJBossServerType WF10_SERVER_TYPE = 
			new WildFlyServerType(WF10_ID, WF10_NAME, WF10_DESC);
	public static final BaseJBossServerType WF9_SERVER_TYPE = 
			new WildFlyServerType(WF9_ID, WF9_NAME, WF9_DESC);
	public static final BaseJBossServerType WF8_SERVER_TYPE = 
			new WildFlyServerType(WF8_ID, WF8_NAME, WF8_DESC);
	public static final BaseJBossServerType WF71_SERVER_TYPE = 
			new WildFlyServerType(WF71_ID, WF71_NAME, WF71_DESC);
	public static final BaseJBossServerType WF7_SERVER_TYPE = 
			new WildFlyServerType(WF70_ID, WF70_NAME, WF70_DESC);
	
	public static final BaseJBossServerType AS6_SERVER_TYPE = 
			new JBossASServerType(AS60_ID, AS60_NAME, AS60_DESC);
	public static final BaseJBossServerType AS51_SERVER_TYPE = 
			new JBossASServerType(AS51_ID, AS51_NAME, AS51_DESC);
	public static final BaseJBossServerType AS5_SERVER_TYPE = 
			new JBossASServerType(AS50_ID, AS50_NAME, AS50_DESC);
	public static final BaseJBossServerType AS42_SERVER_TYPE = 
			new JBossASServerType(AS42_ID, AS42_NAME, AS42_DESC);
	public static final BaseJBossServerType AS4_SERVER_TYPE = 
			new JBossASServerType(AS40_ID, AS40_NAME, AS40_DESC);
	public static final BaseJBossServerType AS32_SERVER_TYPE = 
			new JBossASServerType(AS32_ID, AS32_NAME, AS32_DESC);
	
	
	public static final BaseJBossServerType EAP43_SERVER_TYPE = 
			new JBossASServerType(EAP43_ID, EAP43_NAME, EAP43_DESC);
	public static final BaseJBossServerType EAP50_SERVER_TYPE = 
			new JBossASServerType(EAP50_ID, EAP50_NAME, EAP50_DESC);

	public static final BaseJBossServerType EAP60_SERVER_TYPE = 
			new WildFlyServerType(EAP60_ID, EAP60_NAME, EAP60_DESC);
	public static final BaseJBossServerType EAP61_SERVER_TYPE = 
			new WildFlyServerType(EAP61_ID, EAP61_NAME, EAP61_DESC);
	public static final BaseJBossServerType EAP70_SERVER_TYPE = 
			new WildFlyServerType(EAP70_ID, EAP70_NAME, EAP70_DESC);
	public static final BaseJBossServerType EAP71_SERVER_TYPE = 
			new WildFlyServerType(EAP71_ID, EAP71_NAME, EAP71_DESC);
	public static final BaseJBossServerType EAP72_SERVER_TYPE = 
			new WildFlyServerType(EAP72_ID, EAP72_NAME, EAP72_DESC);
	public static final BaseJBossServerType EAP73_SERVER_TYPE = 
			new WildFlyServerType(EAP73_ID, EAP73_NAME, EAP73_DESC);
	public static final BaseJBossServerType EAP74_SERVER_TYPE = 
			new WildFlyServerType(EAP74_ID, EAP74_NAME, EAP74_DESC);
}
