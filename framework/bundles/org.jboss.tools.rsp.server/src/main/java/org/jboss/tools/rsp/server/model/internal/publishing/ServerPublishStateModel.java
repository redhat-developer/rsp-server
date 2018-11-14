/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal.publishing;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherEventListener;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherService;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;

public class ServerPublishStateModel implements IServerPublishModel, IFileWatcherEventListener {

	private final Map<String, DeployableState> state;
	private AbstractServerDelegate server;
	private IFileWatcherService fileWatcher;

	public ServerPublishStateModel(AbstractServerDelegate delegate) {
		this.server = delegate;
		this.fileWatcher = delegate.getServer().getServerManagementModel().getFileWatcherService();
		this.state = new LinkedHashMap<>();
	}

	@Override
	public void initialize(List<DeployableReference> references) {
		for( DeployableReference reference : references ) {
			addDeployableImpl(reference);
		}
	}
	
	private void addDeployableImpl(DeployableReference reference) {
		DeployableState sActual = new DeployableState();
		sActual.setReference(reference);
		sActual.setState(ServerManagementAPIConstants.STATE_UNKNOWN);
		sActual.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_FULL);
		state.put(getKey(reference), sActual);
		
		// TODO Maybe make this recursive if we support exploded deployments
		String path = reference.getPath();
		fileWatcher.registerListener(new File(path).toPath(), this, false);
	}
	
	@Override
	public IStatus addDeployable(DeployableReference reference) {
		if (contains(reference)) {
			return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, IStatus.ERROR, 
					NLS.bind("Could not add deploybale with path {0}: it already exists.", getKey(reference)),
							null);
		}
		addDeployableImpl(reference);
		return Status.OK_STATUS;
	}

	@Override
	public boolean contains(DeployableReference reference ) {
		return state.containsKey(getKey(reference));
	}
	
	@Override
	public IStatus removeDeployable(DeployableReference reference) {
		if (!contains(reference)) {
			return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, IStatus.ERROR, 
					NLS.bind("Could not remove deploybale with path {0}: it doesn't exist", getKey(reference)),
							null);
		}
			
		DeployableState ds = state.get(getKey(reference));
		if( ds.getPublishState() == ServerManagementAPIConstants.PUBLISH_STATE_ADD) { 
			// It hasn't been added / published yet, so we can remove it immediately
			deployableRemoved(reference);
		}
		ds.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE);
		return Status.OK_STATUS;
	}

	private String getKey(DeployableReference reference) {
		if (reference == null) {
			return null;
		}
		return reference.getPath();
	}
	
	@Override
	public void deployableRemoved(DeployableReference reference) {
		state.remove(getKey(reference));
	}

	@Override
	public List<DeployableState> getDeployableStates() {
		return new ArrayList<>(state.values());
	}
	
	@Override
	public DeployableState getDeployableState(DeployableReference reference) {
		if (reference == null) {
			return null;
		}
		DeployableState ds = state.get(getKey(reference));
		if (ds == null) {
			return null;
		}
		DeployableState ret = new DeployableState();
		ret.setPublishState(ds.getPublishState());
		ret.setState(ds.getState());
		ret.setReference(reference);
		return ret;
	}

	@Override
	public void setDeployablePublishState(DeployableReference reference, int publishState) {
		DeployableState ds = state.get(getKey(reference));
		DeployableState next = new DeployableState();
		next.setReference(reference);
		next.setState(ds.getState());
		next.setPublishState(publishState);
		state.put(getKey(reference), next);
	}

	@Override
	public void setDeployableState(DeployableReference reference, int runState) {
		DeployableState ds = state.get(getKey(reference));
		DeployableState next = new DeployableState();
		next.setReference(reference);
		next.setState(runState);
		next.setPublishState(ds.getPublishState());
		state.put(getKey(reference), next);
	}

	/*
	 * If a path matching one of our deployments has been modified,
	 * created, or deleted, we should respond to this. However, most 
	 * cases will not require us to do much of anything. 
	 * 
	 *  If the deployment is currently set to be 'added', we should not make any changes
	 *  to the publish state, since the next publish will do the full add as expected.
	 *  
	 *  If the deployment is currently set to be 'removed', we should not make any 
	 *  changes to the publish state, since the next publish will remove the deployment.
	 *  
	 *  If the deployment is currently set to be 'incremental', 
	 *  no change is needed. It's already marked as requiring a publish. 
	 *  
	 *  If the deployment is currently set to be 'full',
	 *  no change is needed. It's already marked as requiring a publish.
	 *  
	 *  If the deployment is currently set to 'unknown', 
	 *  we should not make any change, so the delegate knows the state is still uncertain.
	 *  
	 *  So only if the deployment is currently set to 'none' do we know 
	 *  that we should now mark it as requiring an incremental publish. 
	 * 
	 */
	@Override
	public void fireEvent(FileWatcherEvent event) {
		Path affected = event.getPath();
		List<DeployableState> ds = new ArrayList<>(state.values());
		for( DeployableState d : ds ) {
			Path deploymentPath = new File(d.getReference().getPath()).toPath();
			if( affected.startsWith(deploymentPath)) {
				int currentPubState = d.getPublishState();
				if( currentPubState == ServerManagementAPIConstants.PUBLISH_STATE_NONE) {
					d.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL);
					// Feels strange to allow this class to fire the event
					// but whatever
					server.getServer().getServerManagementModel().getServerModel().fireServerStateChanged(server.getServer(), server.getServerState());
				}
			}
		}
	}
	
}
