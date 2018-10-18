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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;

public class ServerPublishStateModel implements IServerPublishModel {
	// TODO maybe change to a list for ordered deployments?
	private Map<String, DeployableState> state;
	
	public ServerPublishStateModel() {
		state = new HashMap<>();
	}
	
	public void addDeployable(DeployableReference reference) {
		DeployableState sActual = new DeployableState();
		sActual.setReference(reference);
		sActual.setState(ServerManagementAPIConstants.STATE_UNKNOWN);
		sActual.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_ADD);
		state.put(reference.getId(), sActual);
	}

	public void removeDeployable(DeployableReference reference) {
		DeployableState ds = state.get(reference.getId());
		if( ds.getPublishState() == ServerManagementAPIConstants.PUBLISH_STATE_ADD) { 
			// It hasn't been added / published yet, so we can remove it immediately
			deployableRemoved(reference);
		}
		ds.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE);
	}

	public void deployableRemoved(DeployableReference reference) {
		state.remove(reference.getId());
	}

	@Override
	public List<DeployableState> getDeployables() {
		return new ArrayList<DeployableState>(state.values());
	}

	@Override
	public void initialize(List<DeployableReference> references) {
		for( DeployableReference reference : references ) {
			DeployableState sActual = new DeployableState();
			sActual.setReference(reference);
			sActual.setState(ServerManagementAPIConstants.STATE_UNKNOWN);
			sActual.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_FULL);
			state.put(reference.getId(), sActual);
		}
	}

	@Override
	public void setModulePublishState(DeployableReference reference, int publishState) {
		DeployableState ds = state.get(reference.getId());
		DeployableState next = new DeployableState();
		next.setReference(reference);
		next.setState(ds.getState());
		next.setPublishState(publishState);
		state.put(reference.getId(), next);
	}

	@Override
	public void setModuleState(DeployableReference reference, int runState) {
		DeployableState ds = state.get(reference.getId());
		DeployableState next = new DeployableState();
		next.setReference(reference);
		next.setState(runState);
		next.setPublishState(ds.getPublishState());
		state.put(reference.getId(), next);
	}
	
}
