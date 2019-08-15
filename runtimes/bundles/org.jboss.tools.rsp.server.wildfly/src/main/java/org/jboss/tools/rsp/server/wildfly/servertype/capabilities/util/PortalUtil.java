/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util;

import java.io.File;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.launching.utils.OSUtils;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;


public class PortalUtil {
	public static final String GATEIN = "gatein";
	public static final int TYPE_PORTAL_UNKNOWN = 0;
	public static final int TYPE_PORTAL = 1;
	public static final int TYPE_PORTAL_CLUSTER = 2;
	public static final int TYPE_PORTLET_CONTAINER = 3;
	public static final int TYPE_GATE_IN = 4;
	public static final int TYPE_JPP6 = 5;
	
	private static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR = "deploy/jboss-portal.sar"; //$NON-NLS-1$
	private static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR = "deploy/jboss-portal-ha.sar"; //$NON-NLS-1$
	private static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL = "deploy/simple-portal"; //$NON-NLS-1$
	private static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL_SAR = "deploy/simple-portal.sar"; //$NON-NLS-1$
	private static final String SERVER_DEFAULT_DEPLOY_GATEIN = "deploy/gatein.ear"; //$NON-NLS-1$
	
	private static final String SIMPLE_PORTAL_PATH = "simple-portal"; //$NON-NLS-1$
	private static final String PORTAL_PATH = "portal"; //$NON-NLS-1$
	
	public static int getServerPortalType(IServer server) {
		IPath configPath = getConfigurationFullPath(server);
		File configFile = configPath.toFile();
		
		// JBoss Portal server
		if (exists(configFile, SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR)) {
			return TYPE_PORTAL;
		}
		// JBoss Portal clustering server
		if (exists(configFile, SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR)) {
			return TYPE_PORTAL_CLUSTER;
		}
		// JBoss portletcontainer
		if (exists(configFile,SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL) ||
				exists(configFile,SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL_SAR)) {
			return TYPE_PORTLET_CONTAINER;
		}
		// GateIn Portal Server
		if (exists(configFile, SERVER_DEFAULT_DEPLOY_GATEIN)) {
			return TYPE_GATE_IN;
		}
		
		// JPP 6.0
		IPath location = getLocation(server);
		if (exists (location.toFile(), GATEIN)) {
			return TYPE_JPP6;
		}
		return TYPE_PORTAL_UNKNOWN;
	}
	
	private static IPath getLocation(IServer server) {
		String serverHome = server.getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
		return new Path(serverHome);
	}
	
	public static String getPortalSuffix(IServer server) {
		int type = getServerPortalType(server);
		if( type != TYPE_PORTAL_UNKNOWN) {
			if( type == TYPE_PORTLET_CONTAINER) 
				return SIMPLE_PORTAL_PATH;
			return PORTAL_PATH;
		}
		return null;
	}
	
	private static boolean exists(final File location,String portalDir) {
		if( OSUtils.isWindows()) {
			portalDir = portalDir.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		File file = new File(location,portalDir);
		return file.exists();
	}
	
	
	
	// This stuff might need to be extracted out
	private static IPath getConfigLocationFullPath(IServer server) {
		String cl = "server";
		if( new Path(cl).isAbsolute())
			return new Path(cl);
		return getLocation(server).append(cl);
	}
	
	private static IPath getConfigurationFullPath(IServer server) {
		return getConfigLocationFullPath(server).append("default");
	}
}
