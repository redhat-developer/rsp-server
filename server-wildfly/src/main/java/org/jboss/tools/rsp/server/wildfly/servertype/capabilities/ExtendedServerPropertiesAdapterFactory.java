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
		String typeId = null;
		typeId = s.getServerType().getId();
		if( typeId != null ) {
			if( SERVER_AS_32.equals(typeId) )
				return new JBossExtendedProperties(s);
			if( SERVER_AS_40.equals(typeId))
				return new JBossExtendedProperties(s);
			if( SERVER_AS_42.equals(typeId) )
				return new JBossExtendedProperties(s);
			if( SERVER_AS_50.equals(typeId) )
				return new JBossExtendedProperties(s);
			if( SERVER_AS_51.equals(typeId) )
				return new JBossExtendedProperties(s);
			if( SERVER_AS_60.equals(typeId) )
				return new JBossAS6ExtendedProperties(s);
			if( SERVER_EAP_43.equals(typeId) )
				return new JBossExtendedProperties(s);
			if( SERVER_EAP_50.equals(typeId) )
				return new JBossEAP5ExtendedProperties(s);
			
			if( SERVER_AS_70.equals(typeId) )
				return new JBossAS7ExtendedProperties(s);
			if( SERVER_AS_71.equals(typeId) )
				return new JBossAS710ExtendedProperties(s);
			if( SERVER_EAP_60.equals(typeId) )
				return new JBossEAP60ExtendedProperties(s);
			if( SERVER_EAP_61.equals(typeId) )
				return new JBossEAP61ExtendedProperties(s);
			if( SERVER_EAP_70.equals(typeId) )
				return new JBossEAP70ExtendedProperties(s);
			if( SERVER_EAP_71.equals(typeId))
				return new JBossEAP71ExtendedProperties(s);

			if( SERVER_WILDFLY_80.equals(typeId) )
				return new Wildfly80ExtendedProperties(s);
			if( SERVER_WILDFLY_90.equals(typeId) )
				return new Wildfly90ExtendedProperties(s);
			if( SERVER_WILDFLY_100.equals(typeId) )
				return new Wildfly100ExtendedProperties(s);
			if( SERVER_WILDFLY_110.equals(typeId) )
				return new Wildfly110ExtendedProperties(s);
			if( SERVER_WILDFLY_120.equals(typeId) )
				return new Wildfly120ExtendedProperties(s);
			if( SERVER_WILDFLY_130.equals(typeId) )
				return new Wildfly130ExtendedProperties(s);

		}
		return null;
	}
	
}
