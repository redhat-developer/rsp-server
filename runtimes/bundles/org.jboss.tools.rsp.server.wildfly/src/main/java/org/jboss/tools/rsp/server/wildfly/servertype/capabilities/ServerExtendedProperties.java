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

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class ServerExtendedProperties {

	/**
	 * Is thrown when method getWelcomePageUrl() cannot find some resources,
	 * and there is a reason not to log exception, for example, user 
	 * declined authentication, or resource is missing in user application.
	 * This exception will be caught and shown in a warning dialog.
	 * Otherwise, either NPE error dialog would be shown, or user would 
	 * not be notified why browser is not open. 
	 *
	 */
	public static class GetWelcomePageURLException extends Exception {
		private static final long serialVersionUID = -3723110412526178637L;

		public GetWelcomePageURLException(String message) {
			super(message);
		}
		public GetWelcomePageURLException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	protected IServer server;
	
	/**
	 * Create the properties for either an IServer or an IRuntime
	 * 
	 * @param adaptable an IServer or an IRuntime
	 */
	public ServerExtendedProperties(IServer adaptable) {
		this.server = (IServer)adaptable;
	}
//
//	/*
//	 * Convenience enhancements include fileset and xpath support
//	 */
//	public boolean allowConvenienceEnhancements() {
//		return true;
//	}
//	public String getNewXPathDefaultRootFolder() {
//		return "${jboss_config_dir}"; //$NON-NLS-1$
//	}
//	public String getNewFilesetDefaultRootFolder() {
//		return "${jboss_config_dir}"; //$NON-NLS-1$
//	}
//	public String getNewClasspathFilesetDefaultRootFolder() {
//		return getNewFilesetDefaultRootFolder();
//	}
//	
	public static final int JMX_NULL_PROVIDER = -1;
	public static final int JMX_DEFAULT_PROVIDER = 0;
	public static final int JMX_OVER_JNDI_PROVIDER = 1;
	
	/**
	 * Server types that have JMX_OVER_AS_MANAGEMENT_PORT as their jmx type
	 * are expected to implement IManagementPortProvider, so the jmx
	 * knows what port to check. Any servers that use JMX_OVER_AS_MANAGEMENT_PORT
	 * but do not implement the interface will have a default port of 9999 used.
	 */
	public static final int JMX_OVER_AS_MANAGEMENT_PORT_PROVIDER = 2;
	public int getJMXProviderType() {
		return JMX_NULL_PROVIDER;
	}
	
	public boolean hasWelcomePage() {
		return false;
	}
	
	public String getWelcomePageUrl() throws GetWelcomePageURLException {
		return null;
	}
	
	public static final int DEPLOYMENT_SCANNER_NO_SUPPORT = 1;
	public static final int DEPLOYMENT_SCANNER_JMX_SUPPORT = 2;
	public static final int DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT = 3;
	
	public int getMultipleDeployFolderSupport() {
		return DEPLOYMENT_SCANNER_NO_SUPPORT;
	}
	
	public IStatus verifyServerStructure() {
		return Status.OK_STATUS;
	}
	
	public boolean canVerifyRemoteModuleState() {
		return false;
	}
	
//	public IServerModuleStateVerifier getModuleStateVerifier() {
//		return null;
//	}
	
	public static final int FILE_STRUCTURE_UNKNOWN = 0;
	public static final int FILE_STRUCTURE_SERVER_CONFIG_DEPLOY = 1;
	public static final int FILE_STRUCTURE_CONFIG_DEPLOYMENTS = 2;
	public int getFileStructure() {
		return FILE_STRUCTURE_UNKNOWN;
	}

	/**
	 * Server allows exploded modules in some.war/WEB-INF/lib (JBIDE-20071, AS7-4704).
	 * <p>
	 * The default is to assume that the server allows this.
	 * 
	 * @return exploded modules in some.war/WEB-INF/lib are allowed
	 */
	public boolean allowExplodedModulesInWarLibs() {
		return true;
	}
	
	public boolean allowExplodedModulesInEars() {
		return true;
	}

}
