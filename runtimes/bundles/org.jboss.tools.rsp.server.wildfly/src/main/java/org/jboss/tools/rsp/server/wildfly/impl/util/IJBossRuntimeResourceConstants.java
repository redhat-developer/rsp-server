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

import java.io.File;

import org.jboss.tools.rsp.eclipse.core.runtime.Path;

public interface IJBossRuntimeResourceConstants {
	
	/* Files and folders that are in various JBoss installations */
	public static final String SERVER = "server"; //$NON-NLS-1$
	public static final String BIN = "bin"; //$NON-NLS-1$
	public static final String CLIENT = "client"; //$NON-NLS-1$
	public static final String LIB = "lib"; //$NON-NLS-1$
	public static final String DEPLOY = "deploy"; //$NON-NLS-1$
	public static final String COMMON = "common"; //$NON-NLS-1$
	public static final String DEPLOYERS = "deployers"; //$NON-NLS-1$
	public static final String WORK = "work"; //$NON-NLS-1$
	public static final String DATA = "data"; //$NON-NLS-1$
	public static final String WEB_INF = "WEB-INF"; //$NON-NLS-1$
	public static final String FOLDER_TMP = "tmp"; //$NON-NLS-1$
	public static final String FOLDER_LOG = "log"; //$NON-NLS-1$
	public static final String ENDORSED = "endorsed"; //$NON-NLS-1$
	public static final String NATIVE = "native"; //$NON-NLS-1$
	public static final String AS7_STANDALONE = "standalone";//$NON-NLS-1$
	public static final String AS7_DEPLOYMENTS = "deployments";//$NON-NLS-1$
	public static final String CONFIGURATION = "configuration"; //$NON-NLS-1$
	public static final String AS7_MODULES = "modules";//$NON-NLS-1$
	public static final String AS7_BOOT_LOG = "boot.log"; //$NON-NLS-1$
	public static final String JAVAX = "javax";//$NON-NLS-1$
	
	public static final String[] JBOSS_TEMPORARY_FOLDERS = new String[] { WORK, DATA, FOLDER_TMP, FOLDER_LOG};

	public static final String TWIDDLE_JAR = "twiddle.jar"; //$NON-NLS-1$
	public static final String TWIDDLE_SH = "twiddle.sh"; //$NON-NLS-1$
	public static final String SHUTDOWN_JAR = "shutdown.jar"; //$NON-NLS-1$
	public static final String SHUTDOWN_JAR_LOC = BIN + File.separator + SHUTDOWN_JAR;
	public static final String SHUTDOWN_SH = "shutdown.sh"; //$NON-NLS-1$
	public static final String START_JAR = "run.jar"; //$NON-NLS-1$
	public static final String START_JAR_LOC = BIN + Path.SEPARATOR	+ START_JAR; 
	public static final String TOOLS_JAR = "tools.jar"; //$NON-NLS-1$
	public static final String JBOSS7_MODULES_JAR = "jboss-modules.jar"; //$NON-NLS-1$
	public static final String LOGGING_PROPERTIES = "logging.properties"; //$NON-NLS-1$
	public static final String JSF_LIB = "jsf-libs"; //$NON-NLS-1$
	public static final String JBOSSWEB_TOMCAT55_SAR = "jbossweb-tomcat55.sar"; //$NON-NLS-1$
	public static final String JBOSSWEB_SAR = "jbossweb.sar"; //$NON-NLS-1$
	public static final String JSTL_JAR = "jstl.jar"; //$NON-NLS-1$
	public static final String JBOSS_WEB_SERVICE_JAR = "jboss-web-service.jar";  //$NON-NLS-1$
	public static final String EJB3_DEPLOYER = "ejb3.deployer"; //$NON-NLS-1$
	public static final String JB6_EJB3_ENDPOINT_DEPLOYER_JAR = "jboss-ejb3-endpoint-deployer.jar";//$NON-NLS-1$
	public static final String JB6_EJB3_METRICS_DEPLOYER_JAR = "jboss-ejb3-metrics-deployer.jar";//$NON-NLS-1$
	public static final String WEBBEANS_DEPLOYER = "webbeans.deployer"; //$NON-NLS-1$
	public static final String AS5_AOP_DEPLOYER = "jboss-aop-jboss5.deployer"; //$NON-NLS-1$
	public static final String AOP_JDK5_DEPLOYER = "jboss-aop-jdk50.deployer"; //$NON-NLS-1$
	public static final String AOP_JBOSS5_DEPLOYER = "jboss-aop-jboss5.deployer"; //$NON-NLS-1$
	public static final String JBOSS_AOP_JDK5_JAR = "jboss-aop-jdk50.jar"; //$NON-NLS-1$
	public static final String JBOSS_WEB_DEPLOYER = "jboss-web.deployer"; //$NON-NLS-1$
	public static final String REST_EASY_DEPLOYER = "resteasy.deployer"; //$NON-NLS-1$
	public static final String JSF_DEPLOYER = "jsf.deployer"; //$NON-NLS-1$
	public static final String MOJARRA_12 = "Mojarra-1.2"; //$NON-NLS-1$
	public static final String MOJARRA_20 = "Mojarra-2.0"; //$NON-NLS-1$
	public static final String JSP_API_JAR = "jsp-api.jar"; //$NON-NLS-1$
	public static final String SERVLET_API_JAR = "servlet-api.jar"; //$NON-NLS-1$
	public static final String JSF_API_JAR = "jsf-api.jar"; //$NON-NLS-1$
	public static final String JSF_IMPL_JAR = "jsf-impl.jar"; //$NON-NLS-1$
	public static final String JBOSS_J2EE_JAR = "jboss-j2ee.jar"; //$NON-NLS-1$
	public static final String JBOSS_EJB3X_JAR = "jboss-ejb3x.jar"; //$NON-NLS-1$
	public static final String JBOSS_EJB3_JAR = "jboss-ejb3.jar"; //$NON-NLS-1$
	public static final String JBOSS_ANNOTATIONS_EJB3_JAR = "jboss-annotations-ejb3.jar"; //$NON-NLS-1$
	public static final String EJB3_PERSISTENCE_JAR = "ejb3-persistence.jar"; //$NON-NLS-1$
	public static final String JB5_EJB_DEPLOYER_JAR = "jboss-ejb3-deployer.jar"; //$NON-NLS-1$
	public static final String JB5_EJB_IIOP_JAR = "jboss-ejb3-iiop.jar"; //$NON-NLS-1$
	public static final String jboss_ejb3_common_client = "jboss-ejb3-common-client.jar";//$NON-NLS-1$
	public static final String jboss_ejb3_core_client = "jboss-ejb3-core-client.jar";//$NON-NLS-1$
	public static final String jboss_ejb3_ext_api_impl = "jboss-ejb3-ext-api-impl.jar";//$NON-NLS-1$
	public static final String jboss_ejb3_ext_api = "jboss-ejb3-ext-api.jar";//$NON-NLS-1$
	public static final String jboss_ejb3_proxy_client = "jboss-ejb3-proxy-client.jar";//$NON-NLS-1$
	public static final String jboss6_ejb3_proxy_spi_client = "jboss-ejb3-proxy-spi-client.jar";//$NON-NLS-1$
	public static final String jboss6_ejb3_proxy_impl_client = "jboss-ejb3-proxy-impl-client.jar";//$NON-NLS-1$
	
	public static final String jboss_ejb3_proxy_clustered_client = "jboss-ejb3-proxy-clustered-client.jar";//$NON-NLS-1$
	public static final String jboss_ejb3_security_client = "jboss-ejb3-security-client.jar";//$NON-NLS-1$

	public static final String JBOSS_ASPECT_LIBRARY_JDK5_0 = "jboss-aspect-library-jdk50.jar"; //$NON-NLS-1$
	public static final String JBOSS5_ASPECT_LIBRARY_JAR = "jboss-aspect-library.jar"; //$NON-NLS-1$
	public static final String JBOSS6_AOP_ASPECTS_JAR = "jboss-aop-aspects.jar"; //$NON-NLS-1$
	public static final String JBOSS6_AS_ASPECT_LIBRARY_JAR = "jboss-as-aspects-jboss-aspect-library.jar"; //$NON-NLS-1$
	
	public static final String HIBERNATE_CLIENT_JAR = "hibernate-client.jar";  //$NON-NLS-1$
	public static final String JB50_HIBERNATE_ANNOTATIONS_JAR = "hibernate-annotations.jar"; //$NON-NLS-1$
	public static final String JBOSSALL_CLIENT_JAR = "jbossall-client.jar"; //$NON-NLS-1$
	public static final String JBOSSWEB_TOMCAT_50_SAR = "jbossweb-tomcat50.sar"; //$NON-NLS-1$
	public static final String JSR299_API_JAR = "jsr299-api.jar"; //$NON-NLS-1$
	public static final String JNDI_PROPERTIES = "jndi.properties"; //$NON-NLS-1$
	
	public static final String JAVAX_SERVLET_JAR = "javax.servlet.jar"; //$NON-NLS-1$
	public static final String JAVAX_SERVLET_JSP_JAR = "javax.servlet.jsp.jar"; //$NON-NLS-1$

	public static final String CONFIG_DEFAULT = "default"; //$NON-NLS-1$
	public static final String DEFAULT_CONFIGURATION = CONFIG_DEFAULT;
	public static final String CONFIG_ALL = "all"; //$NON-NLS-1$
	public static final String CONFIG_MINIMAL = "minimal"; //$NON-NLS-1$
	
	public static final String DESCRIPTOR_WEB = "WEB-INF/web.xml"; //$NON-NLS-1$
	public static final String DESCRIPTOR_EJB = "META-INF/ejb-jar.xml"; //$NON-NLS-1$
	public static final String DESCRIPTOR_EAR = "META-INF/application.xml"; //$NON-NLS-1$
	public static final String DESCRIPTOR_CLIENT = "META-INF/application-client.xml"; //$NON-NLS-1$
	public static final String DESCRIPTOR_CONNECTOR = "META-INF/ra.xml"; //$NON-NLS-1$
	public static final String JBOSS_AS = "JBOSS_AS";  //$NON-NLS-1$
	public static final String JBOSS_AS_EAP_DIRECTORY = "jboss-as";  //$NON-NLS-1$
	
	public static final String AS_70_MANAGEMENT_SCRIPT = "jboss-admin.sh"; //$NON-NLS-1$
	public static final String AS_71_MANAGEMENT_SCRIPT = "jboss-cli.sh"; //$NON-NLS-1$
}
