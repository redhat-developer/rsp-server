/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.impl.util;

public interface IJBossRuntimeConstants {
	// Launch configuration constants / Command Line Args
	public static final String SPACE = " ";//$NON-NLS-1$
	public static final String DASH = "-"; //$NON-NLS-1$
	public static final String SYSPROP = "-D";//$NON-NLS-1$
	public static final String EQ = "="; //$NON-NLS-1$
	public static final String QUOTE = "\""; //$NON-NLS-1$
	public static final String FILE_COLON = "file:"; //$NON-NLS-1$
	
	public static final String JBOSS_SERVER_HOME_DIR = "jboss.server.home.dir"; //$NON-NLS-1$
	/*
	 *  This constant is incorrectly named. 
	 *  It is deprecated for now. 
	 *  It may one day come back, with the value
	 *     'jboss.server.home.url'
	 *  Any consumers who may be affected should change now  
	 */
	@Deprecated 
	public static final String JBOSS_SERVER_HOME_URL = "jboss.server.base.url"; //$NON-NLS-1$
	public static final String JBOSS_SERVER_BASE_URL = "jboss.server.base.url"; //$NON-NLS-1$
	public static final String JBOSS_SERVER_BASE_DIR = "jboss.server.base.dir"; //$NON-NLS-1$
	public static final String JBOSS_SERVER_NAME = "jboss.server.name"; //$NON-NLS-1$
	public static final String JBOSS_HOME_DIR = "jboss.home.dir"; //$NON-NLS-1$
	public static final String ENDORSED_DIRS = "java.endorsed.dirs"; //$NON-NLS-1$
	public static final String JAVA_LIB_PATH = "java.library.path"; //$NON-NLS-1$
	public static final String LOGGING_CONFIG_PROP = "logging.configuration"; //$NON-NLS-1$
	public static final String SHUTDOWN_SERVER_ARG = "-s";//$NON-NLS-1$
	public static final String SHUTDOWN_USER_ARG = "-u";//$NON-NLS-1$
	public static final String SHUTDOWN_PASS_ARG = "-p";//$NON-NLS-1$
	public static final String SHUTDOWN_ADAPTER_ARG = "-a";//$NON-NLS-1$
	public static final String SHUTDOWN_STOP_ARG = "-S"; //$NON-NLS-1$
	public static final String SHUTDOWN_ARG_PORT_LONG = "--port"; //$NON-NLS-1$
	public static final String STARTUP_ARG_HOST_SHORT = "-b"; //$NON-NLS-1$
	public static final String STARTUP_ARG_HOST_LONG = "--host"; //$NON-NLS-1$
	public static final String STARTUP_ARG_CONFIG_SHORT = "-c"; //$NON-NLS-1$
	public static final String STARTUP_ARG_CONFIG_LONG = "--configuration"; //$NON-NLS-1$
	public static final String SERVER_ID = "server-id"; //$NON-NLS-1$
	public static final String SERVER_ARG = "-server"; //$NON-NLS-1$
	public static final String DEFAULT_MEM_ARGS = "-Xms256m -Xmx512m -XX:MaxPermSize=256m "; //$NON-NLS-1$
	public static final String DEFAULT_MEM_ARGS_AS50 = "-Xms256m -Xmx768m -XX:MaxPermSize=256m "; //$NON-NLS-1$
	public static final String SUN_CLIENT_GC_ARG = "sun.rmi.dgc.client.gcInterval"; //$NON-NLS-1$
	public static final String SUN_SERVER_GC_ARG = "sun.rmi.dgc.server.gcInterval"; //$NON-NLS-1$
	public static final String JAVA_PREFER_IP4_ARG = "java.net.preferIPv4Stack"; //$NON-NLS-1$
	public static final String PROGRAM_NAME_ARG = "program.name"; //$NON-NLS-1$
	
	public static final String JB7_MP_ARG = "mp"; //$NON-NLS-1$
	public static final String JB7_SERVER_CONFIG = "server-config"; //$NON-NLS-1$
	public static final String JB7_SERVER_CONFIG_ARG = "--server-config"; //$NON-NLS-1$
	
	public static final String MODULES = "modules"; //$NON-NLS-1$
	public static final String JB7_LOGMODULE_ARG = "logmodule"; //$NON-NLS-1$
	public static final String JB7_LOGMODULE_DEFAULT = "org.jboss.logmanager"; //$NON-NLS-1$
	public static final String JB7_JAXPMODULE = "jaxpmodule"; //$NON-NLS-1$
	public static final String JB7_JAXP_PROVIDER = "javax.xml.jaxp-provider"; //$NON-NLS-1$
	public static final String JB7_STANDALONE_ARG = "org.jboss.as.standalone"; //$NON-NLS-1$
	public static final String JB7_BOOT_LOG_ARG = "org.jboss.boot.log.file"; //$NON-NLS-1$
	public static final String JB7_LOGGING_CONFIG_FILE = "logging.configuration"; //$NON-NLS-1$
	public static final String JB7_EXPOSE_MANAGEMENT = "jboss.bind.address.management"; //$NON-NLS-1$
	
	
	/* JBoss classes and methods for reflection */
	public static final String TWIDDLE_MAIN_TYPE = "org.jboss.console.twiddle.Twiddle"; //$NON-NLS-1$
	public static final String SHUTDOWN_MAIN_TYPE = "org.jboss.Shutdown"; //$NON-NLS-1$
	public static final String START_MAIN_TYPE = "org.jboss.Main"; //$NON-NLS-1$
	public static final String START7_MAIN_TYPE = "org.jboss.modules.Main"; //$NON-NLS-1$
	public static final String CLASS_SIMPLE_PRINCIPAL = "org.jboss.security.SimplePrincipal"; //$NON-NLS-1$
	public static final String CLASS_SECURITY_ASSOCIATION = "org.jboss.security.SecurityAssociation"; //$NON-NLS-1$
	public static final String METHOD_SET_CREDENTIAL = "setCredential"; //$NON-NLS-1$
	public static final String METHOD_SET_PRINCIPAL = "setPrincipal"; //$NON-NLS-1$
	
	/* JNDI Properties */
	public static final String NAMING_FACTORY_KEY = "java.naming.factory.initial"; //$NON-NLS-1$
	public static final String NAMING_FACTORY_VALUE = "org.jnp.interfaces.NamingContextFactory"; //$NON-NLS-1$
	public static final String NAMING_FACTORY_PKGS = "java.naming.factory.url.pkgs"; //$NON-NLS-1$
	public static final String NAMING_FACTORY_INTERFACES = "org.jboss.naming:org.jnp.interfaces"; //$NON-NLS-1$
	public static final String NAMING_FACTORY_PROVIDER_URL = "java.naming.provider.url"; //$NON-NLS-1$
	public static final String JNP_DISABLE_DISCOVERY = "jnp.disableDiscovery"; //$NON-NLS-1$
	
	/* JMX Constants */
	public static final String RMIAdaptor = "jmx/invoker/RMIAdaptor"; //$NON-NLS-1$
	public static final String STARTED_METHOD = "Started"; //$NON-NLS-1$
	public static final String SYSTEM_MBEAN = "jboss.system:type=Server"; //$NON-NLS-1$
	public static final String DEPLOYMENT_SCANNER_MBEAN_NAME="jboss.deployment:flavor=URL,type=DeploymentScanner"; //$NON-NLS-1$
	public static final String STOP = "stop"; //$NON-NLS-1$
	public static final String START = "start"; //$NON-NLS-1$
	public static final String addURL = "addURL"; //$NON-NLS-1$
	
}
