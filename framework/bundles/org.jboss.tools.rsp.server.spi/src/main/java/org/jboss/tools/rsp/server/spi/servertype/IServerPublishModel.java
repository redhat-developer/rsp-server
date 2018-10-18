/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.servertype;

import java.util.List;

import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;

public interface IServerPublishModel {
	
	
	/**
	 * Add a deployable to the list of objects we want published to the server. 
	 * On the next publish request, a publish of this deployable will be attempted. 
	 *  
	 * @param reference
	 */
	public void addDeployable(DeployableReference reference);

	/**
	 * Remove a deployable from the list of objects we want published to the server. 
	 * On the next publish request, a removal of this deployable will be attempted. 
	 *  
	 * @param reference
	 */
	public void removeDeployable(DeployableReference reference);
	
	/**
	 * Get a list of the deployables for this server and their current states
	 * @return
	 */
	public List<DeployableState> getDeployables();

	/**
	 * Allow the framework to initialize the model from a data store
	 * @param references
	 */
	public void initialize(List<DeployableReference> references);

	/**
	 * Set the publish state for a module. 
	 * Clients should call this method after publishing to update
	 * the model with what the current state is.
	 * @param reference
	 * @param publishState
	 */
	public void setModulePublishState(DeployableReference reference, int publishState);

	/**
	 * Set the run state (starting, started, etc) for a deployment.
	 * Clients should call this method after publishing, 
	 * stopping, starting, etc a given deployment. 
	 * 
	 * @param reference
	 * @param runState
	 */
	public void setModuleState(DeployableReference reference, int runState);
}
