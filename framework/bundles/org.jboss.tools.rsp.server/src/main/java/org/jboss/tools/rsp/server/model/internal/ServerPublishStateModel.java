/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal;

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
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;

public class ServerPublishStateModel implements IServerPublishModel {

	private final Map<String, DeployableState> state;

	public ServerPublishStateModel() {
		this.state = new LinkedHashMap<>();
	}

	@Override
	public IStatus addDeployable(DeployableReference reference) {
		if (contains(reference)) {
			return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, IStatus.ERROR, 
					NLS.bind("Could not add deploybale with path {0}: it already exists.", getKey(reference)),
							null);
		}
		DeployableState newState = new DeployableState();
		newState.setReference(reference);
		newState.setState(ServerManagementAPIConstants.STATE_UNKNOWN);
		newState.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_ADD);
		state.put(getKey(reference), newState);
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
	public void initialize(List<DeployableReference> references) {
		for( DeployableReference reference : references ) {
			DeployableState sActual = new DeployableState();
			sActual.setReference(reference);
			sActual.setState(ServerManagementAPIConstants.STATE_UNKNOWN);
			sActual.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_FULL);
			state.put(getKey(reference), sActual);
		}
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
	
}
