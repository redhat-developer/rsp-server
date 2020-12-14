/******************************************************************************* 
 * Copyright (c) 2012-2019 Red Hat, Inc. 
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
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly100ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly110ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly120ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly130ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly140ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly150ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly160ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly170ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly180ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly190ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly200ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly210ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly220ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly80ExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.WildFlyExtendedProperties.Wildfly90ExtendedProperties;

public class ExtendedServerPropertiesAdapterFactory implements IServerConstants {

	public ServerExtendedProperties getExtendedProperties(IServer s) {
		switch(s.getServerType().getId()) {
		case SERVER_AS_32:
		case SERVER_AS_40:
		case SERVER_AS_42:
		case SERVER_AS_50:
		case SERVER_AS_51:
			return new JBossExtendedProperties(s);
		case SERVER_AS_60:
			return new JBossAS6ExtendedProperties(s);
		case SERVER_EAP_43:
			return new JBossExtendedProperties(s);
		case SERVER_EAP_50:
			return new JBossEAP5ExtendedProperties(s);
		
		case SERVER_AS_70:
			return new JBossAS7ExtendedProperties(s);
		case SERVER_AS_71:
			return new JBossAS710ExtendedProperties(s);
		case SERVER_EAP_60:
			return new JBossEAP60ExtendedProperties(s);
		case SERVER_EAP_61:
			return new JBossEAP61ExtendedProperties(s);
		case SERVER_EAP_70:
			return new JBossEAP70ExtendedProperties(s);
		case SERVER_EAP_71:
			return new JBossEAP71ExtendedProperties(s);
		case SERVER_EAP_72:
			return new JBossEAP72ExtendedProperties(s);
		case SERVER_EAP_73:
			return new JBossEAP73ExtendedProperties(s);

		case SERVER_WILDFLY_80:
			return new Wildfly80ExtendedProperties(s);
		case SERVER_WILDFLY_90:
			return new Wildfly90ExtendedProperties(s);
		case SERVER_WILDFLY_100:
			return new Wildfly100ExtendedProperties(s);
		case SERVER_WILDFLY_110:
			return new Wildfly110ExtendedProperties(s);
		case SERVER_WILDFLY_120:
			return new Wildfly120ExtendedProperties(s);
		case SERVER_WILDFLY_130:
			return new Wildfly130ExtendedProperties(s);
		case SERVER_WILDFLY_140:
			return new Wildfly140ExtendedProperties(s);
		case SERVER_WILDFLY_150:
			return new Wildfly150ExtendedProperties(s);
		case SERVER_WILDFLY_160:
			return new Wildfly160ExtendedProperties(s);
		case SERVER_WILDFLY_170:
			return new Wildfly170ExtendedProperties(s);
		case SERVER_WILDFLY_180:
			return new Wildfly180ExtendedProperties(s);
		case SERVER_WILDFLY_190:
			return new Wildfly190ExtendedProperties(s);
		case SERVER_WILDFLY_200:
			return new Wildfly200ExtendedProperties(s);
		case SERVER_WILDFLY_210:
			return new Wildfly210ExtendedProperties(s);
		case SERVER_WILDFLY_220:
			return new Wildfly220ExtendedProperties(s);
		// NEW_SERVER_ADAPTER
		default:
			return null;
		}
	}
	
}
