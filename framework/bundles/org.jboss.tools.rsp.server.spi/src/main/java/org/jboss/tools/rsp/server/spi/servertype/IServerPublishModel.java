/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.servertype;

import java.util.List;

import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableReferenceWithOptions;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;

public interface IServerPublishModel {
	/**
	 * Get a resource delta for the given deployment
	 * @param reference
	 * @return
	 */
	public IDeployableResourceDelta getDeployableResourceDelta(DeployableReference reference);
	
	/**
	 * Adds a deployable to the list of objects we want published to the server. 
	 * On the next publish request, a publish of this deployable will be attempted. 
	 *  
	 * @param req
	 * @return IStatus#OK if the deployable was added. IStatus.ERROR otherwise.
	 */
	public IStatus addDeployable(DeployableReferenceWithOptions ref);

	/**
	 * Returns {@code true} if the given reference can be added to this model. This
	 * is the case if this model does not contain a DeployableReference with the
	 * same id.
	 * 
	 * @param reference
	 * @return true if the given reference can be added
	 * 
	 * @see DeployableReference#getId
	 */
	public boolean contains(DeployableReference reference);

	/**
	 * Removes a deployable from the list of objects we want published to the server. 
	 * On the next publish request, a removal of this deployable will be attempted. 
	 *  
	 * @param reference
	 * @return IStatus#OK if the deployable was removed. IStatus.ERROR otherwise.
	 * 
	 * @see #removeDeployable
	 */
	public IStatus removeDeployable(DeployableReferenceWithOptions reference);

	/**
	 * Returns a list of the deployables for this server and their current states
	 * @return
	 */
	public List<DeployableState> getDeployableStates();
	
	/**
	 * Allows the framework to initialize the model from a data store
	 * @param references
	 */
	public void initialize(List<DeployableReferenceWithOptions> references);

	/**
	 * Sets the publish state for a deployable. 
	 * Clients should call this method after publishing to update
	 * the model with what the current state is.
	 * @param reference
	 * @param publishState
	 */
	public void setDeployablePublishState(DeployableReference reference, int publishState);

	/**
	 * Sets the run state (starting, started, etc) for a deployment.
	 * Clients should call this method after publishing, 
	 * stopping, starting, etc a given deployment. 
	 * 
	 * @param reference
	 * @param runState
	 */
	public void setDeployableState(DeployableReference reference, int runState);

	/**
	 * Returns the current state for the given deployment. Return {@code null} if
	 * the deployment doesn't exist yet.
	 * 
	 * @param reference
	 * @return
	 */
	public DeployableState getDeployableState(DeployableReference reference);
	
	/**
	 * Get the deployment options for the given reference
	 * @return
	 */
	public DeployableReferenceWithOptions getReferenceOptions(DeployableReference reference);

	/**
	 * Forces the model to remove the given deployable from its stores entirely.
	 * @param reference
	 */
	public void deployableRemoved(DeployableReference reference);
	
	
	/**
	 * Set the publish state of the entire server, 
	 * typically based on some union of the individual modules' publish states
	 * @param state
	 * @param fire
	 */
	public void setServerPublishState(int state, boolean fire);

	/**
	 * Get the publish state of the entire server, 
	 * typically based on some union of the individual modules' publish states
	 * 
	 * @return
	 */
	public int getServerPublishState();
}
