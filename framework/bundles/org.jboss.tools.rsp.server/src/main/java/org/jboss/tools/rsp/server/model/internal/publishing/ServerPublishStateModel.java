/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal.publishing;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableReferenceWithOptions;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherEventListener;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherService;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableResourceDelta;
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;

public class ServerPublishStateModel implements IServerPublishModel, IFileWatcherEventListener {

	private final Map<String, DeployableState> state;
	private final Map<String, Map<String,Object>> deploymentOptions;
	private final Map<String, DeployableDelta> deltas = new HashMap<>();
	
	private AbstractServerDelegate server;
	private IFileWatcherService fileWatcher;
	private int publishState = AbstractServerDelegate.PUBLISH_STATE_UNKNOWN;
	
	public ServerPublishStateModel(AbstractServerDelegate delegate, IFileWatcherService fileWatcher) {
		this.server = delegate;
		this.fileWatcher = fileWatcher;
		this.state = new LinkedHashMap<>();
		this.deploymentOptions = new LinkedHashMap<>();
	}

	@Override
	public void initialize(List<DeployableReferenceWithOptions> references) {
		for( DeployableReferenceWithOptions reference : references ) {
			addDeployableImpl(reference, ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN);
		}
		fireState();
	}
	
	private void addDeployableImpl(DeployableReferenceWithOptions withOptions, int publishState) {
		DeployableReference reference = withOptions.getReference();
		DeployableState sActual = new DeployableState();
		sActual.setReference(reference);
		sActual.setState(ServerManagementAPIConstants.STATE_UNKNOWN);
		sActual.setPublishState(publishState);
		sActual.setServer(server.getServerHandle());
		
		String key = getKey(reference);
		state.put(key, sActual);
		deploymentOptions.put(key, withOptions.getOptions());
		
		File f = new File(reference.getPath());
		boolean recursive = (f == null || !f.exists()) ? false : !f.isFile();
		
		String path = reference.getPath();
		if( fileWatcher != null ) {
			fileWatcher.addFileWatcherListener(new File(path).toPath(), this, recursive);
		}
	}
	
	@Override
	public IStatus addDeployable(DeployableReferenceWithOptions withOptions) {
		if (contains(withOptions.getReference())) {
			return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, IStatus.ERROR, 
					NLS.bind("Could not add deploybale with path {0}: it already exists.", 
							getKey(withOptions.getReference())), null);
		}
		addDeployableImpl(withOptions, ServerManagementAPIConstants.PUBLISH_STATE_ADD);
		fireState();
		return Status.OK_STATUS;
	}

	@Override
	public boolean contains(DeployableReference reference ) {
		return state.containsKey(getKey(reference));
	}
	
	@Override
	public IStatus removeDeployable(DeployableReferenceWithOptions ref) {
		DeployableReference reference = ref.getReference();
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
		String path = reference.getPath();
		if( fileWatcher != null ) {
			fileWatcher.removeFileWatcherListener(new File(path).toPath(), this);
		}
		fireState();
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
		String k = getKey(reference);
		state.remove(k);
		deploymentOptions.remove(k);
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
		ret.setServer(server.getServerHandle());
		return ret;
	}

	@Override
	public void setDeployablePublishState(DeployableReference reference, int publishState) {
		DeployableState ds = state.get(getKey(reference));
		DeployableState next = new DeployableState();
		next.setReference(reference);
		next.setState(ds.getState());
		next.setPublishState(publishState);
		next.setServer(server.getServerHandle());
		state.put(getKey(reference), next);
		if( publishState == ServerManagementAPIConstants.PUBLISH_STATE_NONE) {
			DeployableDelta delta2 = deltas.get(getKey(reference));
			if( delta2 != null ) {
				delta2.clearDelta();
			}
		}
		updateServerPublishStateFromDeployments();
	}

	@Override
	public void setDeployableState(DeployableReference reference, int runState) {
		DeployableState ds = state.get(getKey(reference));
		DeployableState next = new DeployableState();
		next.setReference(reference);
		next.setState(runState);
		next.setPublishState(ds.getPublishState());
		next.setServer(server.getServerHandle());
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
	public void fileChanged(FileWatcherEvent event) {
		Path affected = event.getPath();
		List<DeployableState> ds = new ArrayList<>(state.values());
		boolean changed = false;
		for( DeployableState d : ds ) {
			Path deploymentPath = new File(d.getReference().getPath()).toPath();
			if( affected.startsWith(deploymentPath)) {
				int currentPubState = d.getPublishState();
				if( currentPubState == ServerManagementAPIConstants.PUBLISH_STATE_NONE) {
					d.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL);
					changed = true;
				}
				if( currentPubState == ServerManagementAPIConstants.PUBLISH_STATE_NONE ||
						currentPubState == ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL) {
					registerSingleDelta(event, d.getReference());
				}

			}
		}
		if( changed ) 
			fireState();
	}

	private void registerSingleDelta(FileWatcherEvent event, DeployableReference reference) {
		String key = getKey(reference);
		DeployableDelta dd = deltas.get(key);
		if( dd == null ) {
			dd = new DeployableDelta(reference);
			deltas.put(key, dd);
		}
		dd.registerChange(event);
	}

	private void fireState() {
		updateServerPublishStateFromDeployments();
		
		// Feels strange to allow this class to fire the event
		// but whatever. This feels so dirty. 
		if( server != null && server.getServer() != null && server.getServer().getServerManagementModel() != null 
				&& server.getServer().getServerManagementModel().getServerModel() != null ) {
			server.getServer().getServerManagementModel().getServerModel().fireServerStateChanged(server.getServer(), server.getServerState());
		}
	}
	
	private void updateServerPublishStateFromDeployments() {
		ArrayList<DeployableState> vals = new ArrayList<>(state.values());
		int newState = ServerManagementAPIConstants.PUBLISH_STATE_NONE;
		
		if( deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_ADD, vals) || 
				 deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE, vals) ||
				deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_FULL, vals)) {
			newState = ServerManagementAPIConstants.PUBLISH_STATE_FULL;
		} else {
			if( deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN, vals)) {
				newState = ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN;
			} else if( deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, vals)) {
				newState = ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL;
			} else {
				newState = ServerManagementAPIConstants.PUBLISH_STATE_NONE;
			}
		}
		setServerPublishState(newState, false);
	}
	
	private boolean deployableExists(int publishState, List<DeployableState> list) {
		for( DeployableState i : list ) {
			if( i.getPublishState() == publishState ) 
				return true;
		}
		return false;
	}
	
	public int getServerPublishState() {
		return this.publishState;
	}
	public void setServerPublishState(int state, boolean fire) {
		if( state != this.publishState) {
			this.publishState = state;
			if( fire ) 
				fireState();
		}
	}

	@Override
	public DeployableReferenceWithOptions getReferenceOptions(DeployableReference reference) {
		DeployableReferenceWithOptions ret = new DeployableReferenceWithOptions();
		ret.setReference(reference);
		ret.setOptions(deploymentOptions.get(getKey(reference)));
		return ret;
	}

	@Override
	public IDeployableResourceDelta getDeployableResourceDelta(DeployableReference reference) {
		return deltas.get(getKey(reference));
	}

}
