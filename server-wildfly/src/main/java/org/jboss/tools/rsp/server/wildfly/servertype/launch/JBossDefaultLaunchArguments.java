/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.launch;

import java.util.HashMap;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.launching.utils.OSUtils;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.impl.util.IJBossRuntimeConstants;
import org.jboss.tools.rsp.server.wildfly.impl.util.IJBossRuntimeResourceConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.JBossVMRegistryDiscovery;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util.IP6Util;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util.JavaUtils;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util.PortalUtil;

public class JBossDefaultLaunchArguments implements IDefaultLaunchArguments, IJBossRuntimeResourceConstants, IJBossRuntimeConstants {
	protected IServer server;
	
	private IPath serverHome;
	
	// If created with a server, we can try to get values from 
	// whatever 'mode' it is in (local vs rse). 
	public JBossDefaultLaunchArguments(IServer s) {
		this.server = s;
	}

	private void setServerHome(IPath path) {
		this.serverHome = path;
	}
	
	protected IPath getServerHome() {
		// If we have a set server home, use it
		if( serverHome != null)
			return serverHome;
		// Get from server-mode data (local/rse)
		String serverHome = server.getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
		return new Path(serverHome);
	}

	public String getStartDefaultProgramArgs() {
		String s1 = STARTUP_ARG_CONFIG_LONG + "=" + IJBossRuntimeResourceConstants.CONFIG_DEFAULT + SPACE;  //$NON-NLS-1$
		if( PortalUtil.getServerPortalType(server) == PortalUtil.TYPE_GATE_IN) {
			s1 += IJBossRuntimeConstants.SPACE + "-Dexo.conf.dir.name=gatein"; //$NON-NLS-1$
		}
		return s1;
	}

	public String getStartDefaultVMArgs() {
		return getProgramNameArgs() + getServerFlagArgs() +   
				"-Djava.awt.headless=true " + //$NON-NLS-1$
				getMemoryArgs() + getJavaFlags() + getJBossJavaFlags();  
	}

	protected String getProgramNameArgs() {
		String name = server.getName();
		String ret = QUOTE + SYSPROP + PROGRAM_NAME_ARG + EQ +  
			"JBossTools: " + name + QUOTE + SPACE; //$NON-NLS-1$
		return ret;
	}
	
	protected String getServerFlagArgs() {
		// We assume that even if the server is in remote mode, the remote configuration
		// matches the local very closely. 
		// But if there's no local runtime, we'll just not include the -server flag
		IVMInstall install = JBossVMRegistryDiscovery.findVMInstall(server.getDelegate());
		if( JavaUtils.supportsServerMode(install))
			return SERVER_ARG + SPACE;
		return new String();
	}
	
	protected boolean isLinux() {
		return OSUtils.isUnix();
	}
	
	protected String getJavaFlags() {
		return getJavaFlags(isLinux());
	}
	
	protected String getJavaFlags(boolean includeIPVersionFlag) {
		String ret = new String();
		if( includeIPVersionFlag )
			ret += SYSPROP + JAVA_PREFER_IP4_ARG + EQ + !isIP6() + SPACE; 
		ret += SYSPROP + SUN_CLIENT_GC_ARG + EQ + 3600000 + SPACE;
		ret += SYSPROP + SUN_SERVER_GC_ARG + EQ + 3600000 + SPACE;
		return ret;
	}
	
	protected boolean isIP6() {
		return IP6Util.matchesIP6t("localhost");
	}
	
	
	// Get flags that are java flags that point to files somewhere in the AS or dependent on runtime data
	protected String getJBossJavaFlags() {
		IPath serverHome = getServerHome();
		String ret = QUOTE + SYSPROP + ENDORSED_DIRS + EQ + 
				(serverHome.append(LIB).append(ENDORSED).toOSString()) + QUOTE + SPACE;
		
		// Assume the remote also has a native folder if the local has a native folder
		// This avoids costly remote lookups. 
		if( serverHome.append(BIN).append(NATIVE).toFile().exists() ) 
			ret += SYSPROP + JAVA_LIB_PATH + EQ + QUOTE + 
				serverHome.append(BIN).append(NATIVE).toOSString() + QUOTE + SPACE;
		return ret;
	}
	
	protected IPath getLocalRuntimeHomeDirectory() {
		String serverHome = server.getAttribute(IJBossServerAttributes.SERVER_HOME, (String) null);
		return new Path(serverHome);
	}
	
	// Subclasses can override
	protected String getMemoryArgs() {
		return DEFAULT_MEM_ARGS;
	}
	
	// TODO wtf?
	@Override
	public HashMap<String, String> getDefaultRunEnvVars() {
		HashMap<String, String> envVars = new HashMap<String, String>(1);
		envVars.put("PATH", NATIVE + System.getProperty("path.separator") + "${env_var:PATH}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return envVars;
	}

	/* The following methods are likely to be removed from the interface */
	@Override
	public String getStartDefaultProgramArgs(IPath serverHome) {
		setServerHome(serverHome);
		return getStartDefaultProgramArgs();
	}

	@Override
	public String getStartDefaultVMArgs(IPath serverHome) {
		setServerHome(serverHome);
		return getStartDefaultVMArgs();
	}

	protected String getShutdownServerUrl() {
		return getHost() + ":" + getJNDIPort(); //$NON-NLS-1$
	}
	
	protected String getHost() {
		return "localhost";
	}
	
	protected int getJNDIPort() {
		return 1099;
	}
	
	protected boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	protected String getUsername() {
		return "admin";
	}

	protected String getPassword() {
		return "admin";
	}

	@Override
	public String getDefaultStopArgs() {
		String username = getUsername();
		String pw = getPassword();
		
		String serverUrl = getShutdownServerUrl();
		
		StringBuffer sb = new StringBuffer();
		sb.append(IJBossRuntimeConstants.SHUTDOWN_STOP_ARG );
		sb.append(IJBossRuntimeConstants.SPACE);
		sb.append(IJBossRuntimeConstants.SHUTDOWN_SERVER_ARG); 
		sb.append(IJBossRuntimeConstants.SPACE); 
		sb.append(serverUrl); 
		sb.append(IJBossRuntimeConstants.SPACE);
		
		if( !isEmpty(username)) {
			sb.append(IJBossRuntimeConstants.SHUTDOWN_USER_ARG); 
			sb.append(IJBossRuntimeConstants.SPACE);
			sb.append(username);
			sb.append(IJBossRuntimeConstants.SPACE);
		}
		if( !isEmpty(pw)) {
			sb.append(IJBossRuntimeConstants.SHUTDOWN_PASS_ARG);
			sb.append(IJBossRuntimeConstants.SPACE);
			sb.append(pw);
			sb.append(IJBossRuntimeConstants.SPACE);
		}
		return sb.toString();
	}

	@Override
	public String getDefaultStopVMArgs() {
		return "";
	}

}
