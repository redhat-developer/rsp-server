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
import org.jboss.tools.rsp.api.dao.DeployableReferenceWithOptions;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WildFlyPublishController extends StandardJBossPublishController implements IJBossPublishController {

	private static final Logger LOG = LoggerFactory.getLogger(WildFlyPublishController.class);
	private HashMap<String, String> markersToWrite = new HashMap<>();
	
	public WildFlyPublishController(IServer server, AbstractJBossServerDelegate delegate) {
		super(server, delegate);
	}
	
	@Override
	protected Path getDeploymentFolder() {
		// TODO this may need to be abstracted out eventually if we 
		// support things like custom config folders etc. 
		String home = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		Path p = new File(home).toPath().resolve("standalone").resolve("deployments");
		return p;
	}
	
	@Override
	public int publishModule(DeployableReferenceWithOptions withOptions, 
			int serverPublishRequest, int modulePublishState)
			throws CoreException {
		File src = new File(withOptions.getReference().getPath());
		File dest = getDestinationPath(withOptions).toFile();
		if( src == null || !src.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Module source does not exist"));
		}
		int newStatus = super.publishModule(withOptions, serverPublishRequest, modulePublishState);
		if( newStatus == ServerManagementAPIConstants.PUBLISH_STATE_NONE) {
			// A successful copy / removal... then... 
			if( modulePublishState != ServerManagementAPIConstants.PUBLISH_STATE_REMOVE) {
				// An actual copy was performed.
				boolean isExploded = isExploded(withOptions);
				boolean fullPublish = getModulePublishType(serverPublishRequest, modulePublishState) == ServerManagementAPIConstants.PUBLISH_FULL;
				
				if( isExploded(withOptions) && !fullPublish)
					markersToWrite.put(dest.toString(), ".dodeploy");
			}
		}
		// TODO more? 
		return newStatus;
	}
	
	protected boolean isExploded(DeployableReferenceWithOptions withOptions) {
		File src = new File(withOptions.getReference().getPath());
		if( src != null && src.exists()) {
			if( src.isDirectory())
				return true;
		}
		return false;
	}
	protected int removeExplodedModule(DeployableReferenceWithOptions reference, 
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
		try {
			touch(new File(modulePath + marker));
		} catch(IOException ioe) {
			LOG.error("Error publishing module {0} to server {1}", ioe);
		}
		// TODO Auto-generated method stub
		
	}
	public static void touch(File file) throws IOException{
	    long timestamp = System.currentTimeMillis();
	    touch(file, timestamp);
	}

	public static void touch(File file, long timestamp) throws IOException{
	    if (!file.exists()) {
	       new FileOutputStream(file).close();
	    }

	    file.setLastModified(timestamp);
	}
	private static final String[] MARKER_FILES = {
		".dodeploy", ".skipdeploy", ".isdeploying", ".deployed", ".failed", 
		".isundeploying", ".undeployed", ".pending" 
	};
	private void cleanAllMarkers(String modulePath) {
		for( String k : MARKER_FILES) {
			File f = new File(modulePath + k);
			if( f.exists()) 
				f.delete();
		}
	}
	
	
}
