/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.impl;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.wildfly.beans.impl.DataVirtualization6ServerBeanType;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeAS;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeAS7;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeAS72;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeAS7GateIn;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP6;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP61;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP70;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP71;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAPStandalone;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEPP;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEWP;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeFSW6;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeJPP6;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeJPP61;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeSOA6;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeSOAP;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeSOAPStandalone;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeUnknownAS71Product;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeUnknownAS72Product;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly100;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly100Web;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly110;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly110Web;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly120;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly120Web;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly13;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly130Web;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly14;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly140Web;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly80;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly90;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly90Web;

public class JBossServerBeanTypeProvider implements IServerBeanTypeProvider {


	public static final ServerBeanType AS = new ServerBeanTypeAS();
	public static final ServerBeanType AS7 = new ServerBeanTypeAS7();
	public static final ServerBeanType EAP_STD = new ServerBeanTypeEAPStandalone();
	public static final ServerBeanType EAP = new ServerBeanTypeEAP();
	public static final ServerBeanType EAP6 = new ServerBeanTypeEAP6();
	public static final ServerBeanType UNKNOWN_AS72_PRODUCT = new ServerBeanTypeUnknownAS72Product();
	public static final ServerBeanType AS72 = new ServerBeanTypeAS72();
	public static final ServerBeanType WILDFLY80 = new ServerBeanTypeWildfly80();
	public static final ServerBeanType WILDFLY90 = new ServerBeanTypeWildfly90();
	public static final ServerBeanType WILDFLY90_WEB = new ServerBeanTypeWildfly90Web();
	public static final ServerBeanType WILDFLY100 = new ServerBeanTypeWildfly100();
	public static final ServerBeanType WILDFLY100_WEB = new ServerBeanTypeWildfly100Web();
	public static final ServerBeanType WILDFLY110 = new ServerBeanTypeWildfly110();
	public static final ServerBeanType WILDFLY110_WEB = new ServerBeanTypeWildfly110Web();
	public static final ServerBeanType WILDFLY120 = new ServerBeanTypeWildfly120();
	public static final ServerBeanType WILDFLY120_WEB = new ServerBeanTypeWildfly120Web();
	public static final ServerBeanType WILDFLY130 = new ServerBeanTypeWildfly13();
	public static final ServerBeanType WILDFLY130_WEB = new ServerBeanTypeWildfly130Web();
	public static final ServerBeanType WILDFLY140 = new ServerBeanTypeWildfly14();
	public static final ServerBeanType WILDFLY140_WEB = new ServerBeanTypeWildfly140Web();
	public static final ServerBeanType EAP70 = new ServerBeanTypeEAP70();
	public static final ServerBeanType EAP71 = new ServerBeanTypeEAP71();
	
	public static final ServerBeanType JPP6 = new ServerBeanTypeJPP6();
	
	/**
	 * @since 3.0 (actually 2.4.101)
	 */
	public static final ServerBeanType JPP61 = new ServerBeanTypeJPP61();
	public static final ServerBeanType DV6 = new DataVirtualization6ServerBeanType();
	public static final ServerBeanType FSW6 = new ServerBeanTypeFSW6();
	public static final ServerBeanType EAP61 = new ServerBeanTypeEAP61();
	public static final ServerBeanType UNKNOWN_AS71_PRODUCT = new ServerBeanTypeUnknownAS71Product();	
	public static final ServerBeanType SOA6 = new ServerBeanTypeSOA6();; 
	public static final ServerBeanType SOAP = new ServerBeanTypeSOAP(); 
	public static final ServerBeanType SOAP_STD = new ServerBeanTypeSOAPStandalone();
	public static final ServerBeanType EWP = new ServerBeanTypeEWP();
	public static final ServerBeanType EPP = new ServerBeanTypeEPP();
	public static final ServerBeanType AS7GateIn = new ServerBeanTypeAS7GateIn();

	
	/**
	 * This public variable duplicates the hidden one. 
	 * We shouldn't have to update this in multiple places.
	 * 
	 * 	// NEW_SERVER_ADAPTER
	 */
	public static final ServerBeanType[] KNOWN_TYPES =
		{
		AS,  EAP70, EAP71,
		WILDFLY90,  WILDFLY90_WEB, WILDFLY100, WILDFLY100_WEB,
		WILDFLY110, WILDFLY110_WEB, WILDFLY120, WILDFLY120_WEB,
		WILDFLY130, WILDFLY130_WEB,
		WILDFLY140, WILDFLY140_WEB,
		WILDFLY80, 
		FSW6, EAP61, SOA6, JPP61,  DV6, 
		UNKNOWN_AS72_PRODUCT,
		AS72,  JPP6,  EAP6,  AS7GateIn, 
		UNKNOWN_AS71_PRODUCT,
		AS7, EAP_STD, 
		SOAP, SOAP_STD, 
		EPP, EAP,  EWP
	};

	
	
	@Override
	public ServerBeanType[] getServerBeanTypes() {
		return KNOWN_TYPES;
	}

}
