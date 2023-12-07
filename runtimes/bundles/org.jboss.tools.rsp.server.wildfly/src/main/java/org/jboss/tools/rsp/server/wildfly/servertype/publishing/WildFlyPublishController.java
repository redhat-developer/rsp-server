/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.publishing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WildFlyPublishController extends StandardJBossPublishController {

	private static final Logger LOG = LoggerFactory.getLogger(WildFlyPublishController.class);
    
	private HashMap<String, String> markersToWrite = new HashMap<>();
	
	public WildFlyPublishController(IServer server, AbstractJBossServerDelegate delegate) {
		super(server, delegate);
	}
	
	@Override
	protected Path getDeploymentFolder() {
		Path fromServer = getDeploymentFolderFromServer();
		if( fromServer != null )
			return fromServer;
		return getDefaultDeploymentFolder();
	}

	protected Path getDefaultDeploymentFolder() {
		String home = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		String base = getServer().getAttribute(IJBossServerAttributes.SERVER_BASE_DIR, (String)null);
		base = (base == null || base.isEmpty()) ? IJBossServerAttributes.SERVER_BASE_DIR_DEFAULT : base;
		Path basePath = WildFlyServerDelegate.isRelativePath(base) ?  
				new File(home).toPath().resolve(base) :
				new File(base).toPath(); 
		return basePath.resolve("deployments");
	}

	protected Path getDeploymentFolderFromServer() {
		// TODO This is a bit repetitive for user, as deploy path may include base path within it
		String deploy = getServer().getAttribute(IJBossServerAttributes.WILDFLY_DEPLOY_DIR, (String)null);
		if( deploy == null || deploy.isEmpty())
			return null;
		// Absolute
		if( !WildFlyServerDelegate.isRelativePath(deploy)) {
			return new File(deploy).toPath();
		}
		// Relative
		String home = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		return new File(home).toPath().resolve(deploy);
	}

	@Override
	public int publishModule(DeployableReference withOptions, 
			int serverPublishRequest, int modulePublishState)
			throws CoreException {
		
		// Check error conditions
		String withOptionsPath = withOptions.getPath(); 
		File dest = getDestinationPath(withOptions).toFile();
		if( withOptionsPath == null || !(new File(withOptionsPath).exists())) {
			// Source doesn't need to exist when deleting deployments, only when copying them
			if( modulePublishState != ServerManagementAPIConstants.PUBLISH_STATE_REMOVE) 
				throw new CoreException(new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Module source does not exist"));
		}
		
		// Do the publish and possibly add deployment markers
		int newStatus = super.publishModule(withOptions, serverPublishRequest, modulePublishState);
		conditionallyAddDeploymentMarker(newStatus, modulePublishState, serverPublishRequest, withOptions, dest);
		return newStatus;
	}
	
	protected void conditionallyAddDeploymentMarker(int newStatus, int modulePublishState, int serverPublishRequest, DeployableReference withOptions, File dest) {
		if( newStatus == ServerManagementAPIConstants.PUBLISH_STATE_NONE) {
			// A successful copy / removal... then... 
			if( modulePublishState != ServerManagementAPIConstants.PUBLISH_STATE_REMOVE) {
				// An actual copy was performed.
				boolean fullPublish = getModulePublishType(serverPublishRequest, modulePublishState) == ServerManagementAPIConstants.PUBLISH_FULL;
				if( isExploded(withOptions) && fullPublish)
					markersToWrite.put(dest.toString(), ".dodeploy");
			}
		}
	}
	
	protected boolean isExploded(DeployableReference withOptions) {
		File src = new File(withOptions.getPath());
		if(src.exists()) {
			if( src.isDirectory())
				return true;
		}
		return false;
	}
	
	@Override
	protected int removeExplodedModule(DeployableReference reference, 
			int publishType, int modulePublishType,
			File destination) throws CoreException {
		cleanAllMarkers(destination.getAbsolutePath());
		return super.removeExplodedModule(reference, publishType, modulePublishType, destination);
	}

	@Override
	public void publishFinish(int publishType) throws CoreException {
		// Add the markers where appropriate
		// The reason we do this in publishFinish is so that all modules have 
		// been copied over before we go adding deployment markers, in case
		// one module depends on another. 
		for( String modulePath : markersToWrite.keySet()) {
			cleanAllMarkers(modulePath);
			createMarker(modulePath, markersToWrite.get(modulePath));
		}
		markersToWrite.clear();
	}

	private void createMarker(String modulePath, String marker) {
		File toTouch = new File(modulePath+marker);
		try {
			touch(toTouch);
		} catch(IOException ioe) {
			String errMsg = NLS.bind("Error creating deployment marker file: {0}", toTouch.getAbsolutePath());
			LOG.error(errMsg, ioe);
		}
		
	}

	public static void touch(File file) throws IOException{
	    long timestamp = System.currentTimeMillis();
	    touch(file, timestamp);
	}

	public static void touch(File file, long timestamp) throws IOException{
	    if (!file.exists()) {
	       new FileOutputStream(file).close();
	    }

	    if( !file.setLastModified(timestamp) ) {
	    	LOG.debug("Unable to set timestamp on file " + file.getAbsolutePath());
	    }
	}

	private static final String[] MARKER_FILES = {
		".dodeploy",
		".skipdeploy",
		".isdeploying",
		".deployed",
		".failed",
		".isundeploying",
		".undeployed",
		".pending" 
	};

	private void cleanAllMarkers(String modulePath) {
		for( String k : MARKER_FILES) {
			File f = new File(modulePath + k);
			if( f.exists()) {
				if( !f.delete() ) {
					LOG.error("Error: Cannot remove marker file " + f.getAbsolutePath());
				}
			}
		}
	}
	
	
}
