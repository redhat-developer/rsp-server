/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.IStringSubstitutionProvider;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerType;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.VersionComparisonUtility;

public class GenericJavaLauncher extends AbstractGenericJavaLauncher
		implements IServerStartLauncher, IServerShutdownLauncher {

	private JSONMemento memento;
	private boolean flagMode;
	public GenericJavaLauncher(GenericServerBehavior serverDelegate, JSONMemento memento) {
		super(serverDelegate);
		this.memento = memento;
	}

	public GenericJavaLauncher(GenericServerBehavior serverDelegate, JSONMemento memento, boolean startup) {
		super(serverDelegate);
		this.memento = memento;
		this.flagMode = startup;
	}

	/*
	 * Entry point for shutdown launcher
	 */
	@Override
	public ILaunch launch(boolean force) throws CoreException {
		IServerDelegate delegate = getDelegate();
		ILaunch launch = (ILaunch) delegate.getSharedData(GenericServerBehavior.START_LAUNCH_SHARED_DATA);
		if (force && terminateProcesses(launch)) {
			return null;
		}
		if( launch == null )
			return null;
		
		return launch("run");
	}

	private String getDefaultWorkingDirectory() {
		String serverHome = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR,(String) null);
		if( serverHome != null )
			return serverHome;
		
		String serverHomeFile = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_FILE,(String) null);
		if( serverHomeFile != null )
			return new File(serverHomeFile).getParent();

		return null;
	}
	
	@Override
	protected String getWorkingDirectory() {
		JSONMemento launchProperties = memento.getChild("launchProperties");
		if (launchProperties != null) {
			String wd = launchProperties.getString("workingDirectory");
			if( wd == null ) {
				return getDefaultWorkingDirectory();
			}
			
			String postSub = null;
			try {
				postSub = applySubstitutions(wd);
			} catch(CoreException ce) {
				return getDefaultWorkingDirectory();
			}
			
			Path p = new Path(postSub);
			if( p.isAbsolute())
				return p.toOSString();
			
			String serverHome = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR,(String) null);
			if (serverHome != null && !serverHome.trim().isEmpty()) {
				return new Path(serverHome).append(p).toOSString();
			}
		}
		return null;
	}

	@Override
	protected String getMainTypeName() {
		JSONMemento launchProperties = memento.getChild("launchProperties");
		if (launchProperties != null) {
			String main = launchProperties.getString("mainType");
			if( main != null ) {
				try {
					return applySubstitutions(main);
				} catch(CoreException ce) {
					return main;
				}
			}
		}
		return null;
	}

	@Override
	protected String getVMArguments() {
		String def = getDefaultVMArguments();
		if(shouldOverrideArgs(getServer())) {
			return getOverrideVMArgs(getServer(), def);
		}
		return def;
	}
	protected String getDefaultVMArguments() {
		JSONMemento launchProperties = memento.getChild("launchProperties");
		if (launchProperties != null) {
			return getJavaRelativeProperty(launchProperties, "vmArgs");
		}
		return null;
	}

	protected String getJavaRelativeProperty(JSONMemento launchProperties, String key) {
		String javaVersion = getJavaVersion();
		if( javaVersion != null ) {
			String versionKey = getJavaVersionProperty(javaVersion, launchProperties.getNames(), key);
			String args = launchProperties.getString(versionKey);
			if( args != null ) {
				try {
					return applySubstitutions(args);
				} catch(CoreException ce) {
					return args;
				}
			}
		}
		return null;
	}
	
	
	@Override
	protected String getProgramArguments() {
		String def = getDefaultProgramArguments();
		if(shouldOverrideArgs(getServer())) {
			return getOverrideArgs(getServer(), def);
		}
		return def;
	}
	
	protected String getOverrideArgs(IServer server, String def) {
		if( flagMode == GenericServerBehavior.FLAG_MODE_START ) {
			return getServer().getAttribute(GenericServerType.LAUNCH_OVERRIDE_PROGRAM_ARGS, def);
		}
		// TODO allow users to override shutdown args 
		return def;
	}

	protected String getOverrideVMArgs(IServer server, String def) {
		if( flagMode == GenericServerBehavior.FLAG_MODE_START ) {
			return getServer().getAttribute(GenericServerType.JAVA_LAUNCH_OVERRIDE_VM_ARGS, def);
		}
		// TODO allow users to override shutdown vm args 
		return def;
	}

	protected boolean shouldOverrideArgs(IServer server) {
		return getServer().getAttribute(GenericServerType.LAUNCH_OVERRIDE_BOOLEAN, false);
	}
	
	protected String getDefaultProgramArguments() {
		JSONMemento launchProperties = memento.getChild("launchProperties");
		if (launchProperties != null) {
			return getJavaRelativeProperty(launchProperties, "programArgs");
		}
		return null;
	}

	@Override
	protected String[] getClasspath() {
		String serverHome = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR,
				(String) null);
		JSONMemento launchProperties = memento.getChild("launchProperties");
		if (launchProperties != null) {
			String javaVersion = getJavaVersion();
			if( javaVersion != null ) {
				String key = getJavaVersionProperty(javaVersion, launchProperties.getNames(), "classpath");
				String cpFromJson = launchProperties.getString(key);
				if (cpFromJson != null && !cpFromJson.isEmpty()) {
					return getClasspathFromString(serverHome,cpFromJson);
				}
			}
		}
		return null;
	}
	
	protected String getJavaVersion() {
		IVMInstall vmi = getVMInstall(getDelegate());
		String javaVersion = vmi.getJavaVersion();
		return javaVersion;
	}
	
	public static String getJavaVersionProperty(String javaVersion, 
			List<String> attributes, String prefix) {
		if( attributes.contains(prefix))
			return prefix;
		
		String prefixVersion = prefix + ".version.";
		List<String> jsonVersions = new ArrayList<>();
		for( String s : attributes ) {
			if(s.startsWith(prefixVersion)) {
				jsonVersions.add((s.substring(prefixVersion.length())));
			}
		}
		VersionComparisonUtility.sort(jsonVersions);
		String last = null;
		for( String s : jsonVersions ) {
			if( !VersionComparisonUtility.isGreaterThanOrEqualTo(javaVersion, s))
				return last == null ? null : prefixVersion + last;
			last = s;
		}
		return prefixVersion + last;
	}

	protected String[] getClasspathFromString(String serverHome, String cpFromJson) {
		// First apply substitutions
		String postSub = cpFromJson;
		try {
			postSub = applySubstitutions(postSub);
		} catch(CoreException ce) {
		}
		return convertStringToClasspathEntries(serverHome, postSub);
	}
	
	private String[] convertStringToClasspathEntries(String serverHome, String postSub) {
		String[] entriesToAdd = postSub.split(";");
		List<String> ret = new ArrayList<String>();
		File absolute = null;
		for (int i = 0; i < entriesToAdd.length; i++) {
			Path p = new Path(entriesToAdd[i]);
			if( p.isAbsolute()) {
				absolute = p.toFile();
			} else {
				absolute = new File(serverHome, entriesToAdd[i]);
			}
			if( absolute.isFile()) {
				ret.add(absolute.getAbsolutePath());
			} else if( absolute.isDirectory()) {
				File[] children = absolute.listFiles();
				for( int j = 0; j < children.length; j++ ) {
					if( children[j].getName().endsWith(".jar")) {
						ret.add(children[j].getAbsolutePath());
					}
				}
			}
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}

	private String applySubstitutions(String input) throws CoreException {
		IServerDelegate del = getDelegate();
		return (del instanceof IStringSubstitutionProvider) ? 
				((IStringSubstitutionProvider)del).applySubstitutions(input) : input;
	}
}