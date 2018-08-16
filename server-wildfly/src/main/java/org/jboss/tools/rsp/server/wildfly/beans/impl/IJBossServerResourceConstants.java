/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.beans.impl;

import org.jboss.tools.rsp.launching.utils.FileUtil;

/**
 * Constants having to do with resources, folder paths, 
 * file paths, or manifest entries inside a jboss / eap / wf installation.
 */
public interface IJBossServerResourceConstants {
	public static final String BIN = "bin"; //$NON-NLS-1$
	public static final String TWIDDLE_JAR_NAME = "twiddle.jar"; //$NON-NLS-1$
	public static final String VERSION_PROP = "version";
	public static final String JBOSS_AS_PATH = "jboss-as"; //$NON-NLS-1$
	public static final String RUN_JAR_NAME = "run.jar"; //$NON-NLS-1$
	public static final String SERVER = "server"; //$NON-NLS-1$
	public static final String CONFIG_DEFAULT = "default"; //$NON-NLS-1$
	public static final String DEPLOY = "deploy"; //$NON-NLS-1$
	public static final String MODULES = "modules";
	public static final String PRODUCT_CONF = "product.conf"; //$NON-NLS-1$
	public static final String PRODUCT_CONF_SLOT = "slot"; //$NON-NLS-1$
	public static final String SLOT_MAIN = "main"; //$NON-NLS-1$
	
	public static final String LAYERS_CONF = "layers.conf";
	public static final String LAYERS_CONF_LAYERS = "layers";
	
	public static final String META_INF = "META-INF";

	public static final String AS7_GATE_IN_SYSTEM_JAR_FOLDER = "/gatein/modules/org/gatein/main/";
	public static final String GATEIN_35_PROPERTY_FILE = "gatein/extensions/gatein-wsrp-integration.ear/extension-war.war/META-INF/maven/org.gatein.integration/extension-war/pom.properties";
	public static final String EAP_PRODUCT_META_INF = "modules/org/jboss/as/product/eap/dir/META-INF"; //$NON-NLS-1$
	public static final String EAP_LAYERED_PRODUCT_META_INF = "modules/system/layers/base/org/jboss/as/product/eap/dir/META-INF"; //$NON-NLS-1$
	public static final String AS7_MODULE_SERVER_MAIN = FileUtil.asPath( "modules","org","jboss","as","server","main");
	public static final String AS7_MODULE_LAYERED_SERVER_MAIN = 
			FileUtil.asPath("modules","system","layers","base","org","jboss","as","server","main");
	public static final String JBOSSAS_TWIDDLE_PATH = FileUtil.asPath(JBOSS_AS_PATH,BIN,TWIDDLE_JAR_NAME);
	public static final String BIN_TWIDDLE_PATH = FileUtil.asPath(BIN,TWIDDLE_JAR_NAME);
	
	
	
	public static final String ID_AS = "AS";
	public static final String NAME_AS = "JBoss Application Server";
	public static final String ID_EAP = "EAP";
	public static final String NAME_EAP = "Enterprise Application Platform";
	public static final String RELEASE_NAME_JBOSS_EAP = "JBoss EAP";
	public static final String ID_GATEIN = "GateIn";
	public static final String NAME_GATEIN = "GateIn Application Server";
	public static final String ID_WILDFLY = "WildFly";
	public static final String NAME_WILDFLY = "WildFly Application Server";
	public static final String ID_WILDFLY_WEB = "WildFly-Web";

	
	
	public static final String MANIFEST_MF= "MANIFEST.MF"; //$NON-NLS-1$
	public static final String MANIFEST_PROD_RELEASE_NAME = "JBoss-Product-Release-Name"; //$NON-NLS-1$
	public static final String MANIFEST_PROD_RELEASE_VERS = "JBoss-Product-Release-Version"; //$NON-NLS-1$
	public static final String IMPLEMENTATION_TITLE = "Implementation-Title"; //$NON-NLS-1$
	public static final String IMPLEMENTATION_VERSION = "Implementation-Version";
	public static final String JBAS7_RELEASE_VERSION = "JBossAS-Release-Version"; //$NON-NLS-1$

}
