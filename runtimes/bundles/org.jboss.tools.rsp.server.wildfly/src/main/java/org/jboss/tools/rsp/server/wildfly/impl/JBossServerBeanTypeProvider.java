/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.impl;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.wildfly.beans.impl.DataVirtualization6ServerBeanType;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IJBossServerResourceConstants;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeAS;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeAS7;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeAS72;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeAS7GateIn;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP6;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP61;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP70;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP71;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP72;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeEAP73;
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
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly19;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildfly80;
import org.jboss.tools.rsp.server.wildfly.beans.impl.ServerBeanTypeWildflyX;

public class JBossServerBeanTypeProvider implements IServerBeanTypeProvider, IJBossServerResourceConstants {


	public static final ServerBeanType AS = new ServerBeanTypeAS();
	public static final ServerBeanType AS7 = new ServerBeanTypeAS7();
	public static final ServerBeanType EAP_STD = new ServerBeanTypeEAPStandalone();
	public static final ServerBeanType EAP = new ServerBeanTypeEAP();
	public static final ServerBeanType EAP6 = new ServerBeanTypeEAP6();
	public static final ServerBeanType UNKNOWN_AS72_PRODUCT = new ServerBeanTypeUnknownAS72Product();
	public static final ServerBeanType AS72 = new ServerBeanTypeAS72();
	public static final ServerBeanType WILDFLY80 = new ServerBeanTypeWildfly80();
	
	
	public static final ServerBeanTypeWildflyX WILDFLY90 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "9.", IServerConstants.SERVER_WILDFLY_90);
	public static final ServerBeanTypeWildflyX WILDFLY90_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN, 
			true, "9.", IServerConstants.SERVER_WILDFLY_90);

	public static final ServerBeanTypeWildflyX WILDFLY100 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "10.", IServerConstants.SERVER_WILDFLY_100);
	public static final ServerBeanTypeWildflyX WILDFLY100_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "10.", IServerConstants.SERVER_WILDFLY_100);

	public static final ServerBeanTypeWildflyX WILDFLY110 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "11.", IServerConstants.SERVER_WILDFLY_110);
	public static final ServerBeanTypeWildflyX WILDFLY110_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "11.", IServerConstants.SERVER_WILDFLY_110);

	public static final ServerBeanTypeWildflyX WILDFLY120 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "12.", IServerConstants.SERVER_WILDFLY_120);
	public static final ServerBeanTypeWildflyX WILDFLY120_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "12.", IServerConstants.SERVER_WILDFLY_120);

	public static final ServerBeanTypeWildflyX WILDFLY130 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "13.", IServerConstants.SERVER_WILDFLY_130);
	public static final ServerBeanTypeWildflyX WILDFLY130_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "13.", IServerConstants.SERVER_WILDFLY_130);

	public static final ServerBeanTypeWildflyX WILDFLY140 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "14.", IServerConstants.SERVER_WILDFLY_140);
	public static final ServerBeanTypeWildflyX WILDFLY140_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "14.", IServerConstants.SERVER_WILDFLY_140);

	public static final ServerBeanTypeWildflyX WILDFLY150 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "15.", IServerConstants.SERVER_WILDFLY_150);
	public static final ServerBeanTypeWildflyX WILDFLY150_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "15.", IServerConstants.SERVER_WILDFLY_150);

	public static final ServerBeanTypeWildflyX WILDFLY160 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "16.", IServerConstants.SERVER_WILDFLY_160);
	public static final ServerBeanTypeWildflyX WILDFLY160_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "16.", IServerConstants.SERVER_WILDFLY_160);

	public static final ServerBeanTypeWildflyX WILDFLY170 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "17.", IServerConstants.SERVER_WILDFLY_170);
	public static final ServerBeanTypeWildflyX WILDFLY170_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "17.", IServerConstants.SERVER_WILDFLY_170);

	public static final ServerBeanTypeWildflyX WILDFLY180 = new ServerBeanTypeWildflyX(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "18.", IServerConstants.SERVER_WILDFLY_180);
	public static final ServerBeanTypeWildflyX WILDFLY180_WEB = new ServerBeanTypeWildflyX(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "18.", IServerConstants.SERVER_WILDFLY_180);
	
	public static final ServerBeanTypeWildfly19 WILDFLY190 = new ServerBeanTypeWildfly19(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "19.", IServerConstants.SERVER_WILDFLY_190);
	public static final ServerBeanTypeWildfly19 WILDFLY190_WEB = new ServerBeanTypeWildfly19(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "19.", IServerConstants.SERVER_WILDFLY_190);

	
	public static final ServerBeanTypeWildfly19 WILDFLY200 = new ServerBeanTypeWildfly19(
			ID_WILDFLY, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			false, "20.", IServerConstants.SERVER_WILDFLY_200);
	public static final ServerBeanTypeWildfly19 WILDFLY200_WEB = new ServerBeanTypeWildfly19(
			ID_WILDFLY_WEB, NAME_WILDFLY, AS7_MODULE_LAYERED_SERVER_MAIN,
			true, "20.", IServerConstants.SERVER_WILDFLY_200);


	public static final ServerBeanType EAP70 = new ServerBeanTypeEAP70();
	public static final ServerBeanType EAP71 = new ServerBeanTypeEAP71();
	public static final ServerBeanType EAP72 = new ServerBeanTypeEAP72();
	public static final ServerBeanType EAP73 = new ServerBeanTypeEAP73();
	
	public static final ServerBeanType JPP6 = new ServerBeanTypeJPP6();
	
	/**
	 * @since 3.0 (actually 2.4.101)
	 */
	public static final ServerBeanType JPP61 = new ServerBeanTypeJPP61();
	public static final ServerBeanType DV6 = new DataVirtualization6ServerBeanType();
	public static final ServerBeanType FSW6 = new ServerBeanTypeFSW6();
	public static final ServerBeanType EAP61 = new ServerBeanTypeEAP61();
	public static final ServerBeanType UNKNOWN_AS71_PRODUCT = new ServerBeanTypeUnknownAS71Product();	
	public static final ServerBeanType SOA6 = new ServerBeanTypeSOA6(); 
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
		AS,  EAP70, EAP71, EAP72, EAP73,
		WILDFLY90,  WILDFLY90_WEB, 
		WILDFLY100, WILDFLY100_WEB,
		WILDFLY110, WILDFLY110_WEB,
		WILDFLY120, WILDFLY120_WEB,
		WILDFLY130, WILDFLY130_WEB,
		WILDFLY140, WILDFLY140_WEB,
		WILDFLY150, WILDFLY150_WEB,
		WILDFLY160, WILDFLY160_WEB,
		WILDFLY170, WILDFLY170_WEB,
		WILDFLY180, WILDFLY180_WEB,
		WILDFLY190, WILDFLY190_WEB,
		WILDFLY200, WILDFLY200_WEB,
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
