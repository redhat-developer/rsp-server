/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;

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
		default:
			return null;
		}
	}
	
}
