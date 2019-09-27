/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.servertype;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;

public interface IServerDelegate {

	/**
	 * Server state constant (value 0) indicating that the
	 * server is in an unknown state.
	 * 
	 * @see #getServerRunState()
	 * @see IServerPublishModel#getDeployableState(DeployableReference)
	 * @see DeployableState#getState()
	 */
	public static final int STATE_UNKNOWN = ServerManagementAPIConstants.STATE_UNKNOWN;

	/**
	 * Server state constant (value 1) indicating that the
	 * server is starting, but not yet ready to serve content.
	 * 
	 * @see #getServerRunState()
	 * @see IServerPublishModel#getDeployableState(DeployableReference)
	 * @see DeployableState#getState()
	 */
	public static final int STATE_STARTING = ServerManagementAPIConstants.STATE_STARTING;

	/**
	 * Server state constant (value 2) indicating that the
	 * server is ready to serve content.
	 * 
	 * @see #getServerRunState()
	 * @see IServerPublishModel#getDeployableState(DeployableReference)
	 * @see DeployableState#getState()
	 */
	public static final int STATE_STARTED = ServerManagementAPIConstants.STATE_STARTED;

	/**
	 * Server state constant (value 3) indicating that the
	 * server is shutting down.
	 * 
	 * @see #getServerRunState()
	 * @see IServerPublishModel#getDeployableState(DeployableReference)
	 * @see DeployableState#getState()
	 */
	public static final int STATE_STOPPING = ServerManagementAPIConstants.STATE_STOPPING;

	/**
	 * Server state constant (value 4) indicating that the
	 * server is stopped.
	 * 
	 * @see #getServerRunState()
	 * @see IServerPublishModel#getDeployableState(DeployableReference)
	 * @see DeployableState#getState()
	 */
	public static final int STATE_STOPPED = ServerManagementAPIConstants.STATE_STOPPED;

	/**
	 * Publish state constant (value 0) indicating that it's
	 * in an unknown state.
	 * 
	 * @see #getServerRunState()
	 * @see IServerPublishModel#getDeployableState(DeployableReference)
	 * @see DeployableState#getPublishState()
	 */
	public static final int PUBLISH_STATE_UNKNOWN = ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN;

	/**
	 * Publish state constant (value 1) indicating that there
	 * is no publish required.
	 * 
	 * @see #getServerRunState()
	 * @see IServerPublishModel#getDeployableState(DeployableReference)
	 * @see DeployableState#getPublishState()
	 */
	public static final int PUBLISH_STATE_NONE = ServerManagementAPIConstants.PUBLISH_STATE_NONE;

	/**
	 * Publish state constant (value 2) indicating that an
	 * incremental publish is required.
	 * 
	 * @see #getServerRunState()
	 * @see IServerPublishModel#getDeployableState(DeployableReference)
	 * @see DeployableState#getPublishState()
	 */
	public static final int PUBLISH_STATE_INCREMENTAL = ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL;

	/**
	 * Publish state constant (value 3) indicating that a
	 * full publish is required.
	 * 
	 * @see #getServerRunState()
	 * @see IServerPublishModel#getDeployableState(DeployableReference)
	 * @see DeployableState#getPublishState()
	 */
	public static final int PUBLISH_STATE_FULL = ServerManagementAPIConstants.PUBLISH_STATE_FULL;

	/**
	 * Publish kind constant (value 1) indicating an incremental publish request.
	 * 
	 * @see #publish(int, IProgressMonitor)
	 */
	public static final int PUBLISH_INCREMENTAL = ServerManagementAPIConstants.PUBLISH_INCREMENTAL;

	/**
	 * Publish kind constant (value 2) indicating a full publish request.
	 * 
	 * @see #publish(int, IProgressMonitor)
	 */
	public static final int PUBLISH_FULL = ServerManagementAPIConstants.PUBLISH_FULL;

	/**
	 * Publish kind constant (value 3) indicating an automatic publish request.
	 * 
	 * @see #publish(int, IProgressMonitor)
	 */
	public static final int PUBLISH_AUTO = ServerManagementAPIConstants.PUBLISH_AUTO;

	/**
	 * Publish kind constant (value 4) indicating a publish clean request
	 * 
	 * @see #publish(int, IProgressMonitor)
	 */
	public static final int PUBLISH_CLEAN = ServerManagementAPIConstants.PUBLISH_CLEAN;

	/**
	 * Returns the current state of this server.
	 * <p>
	 * Note that this operation is guaranteed to be fast
	 * (it does not actually communicate with any actual
	 * server).
	 * </p>
	 *
	 * @return one of the server state (<code>STATE_XXX</code>)
	 * constants declared on {@link IServer}
	 */
	public int getServerRunState();
	
	/**
	 * Get the IServer wrapper and holder of attribute key/value pairs
	 * @return
	 */
	public IServer getServer();

	/**
	 * Dispose of this delegate, clean up any maintained resources
	 */
	public void dispose();

	/** 
	 * Access a datastore to hold arbitrary k/v pairs
	 * @param key
	 * @return
	 */
	public Object getSharedData(String key);

	/**
	 * Add a value to a datastore to hold arbitrary k/v pairs
	 * @param key
	 * @param o
	 */
	public void putSharedData(String key, Object o);
	
	/**
	 * Returns the ILaunchManager mode that the server is in. This method will
	 * return null if the server is not running.
	 * 
	 * @return the mode in which a server is running, one of the mode constants
	 *    defined by {@link org.eclipse.debug.core.ILaunchManager}, or
	 *    <code>null</code> if the server is stopped.
	 */
	public String getMode();

	/**
	 * Ask the delegate whether the current configuration is valid and all 
	 * expected resources, keys, files, etc, exist and are accessible.
	 * 
	 * @return
	 */
	public CreateServerValidation validate();

	
	/**
	 * Set the server state to starting. 
	 * This is most likely to be called by the model, passing along
	 * a request from an external client that wished to launch the process
	 * themselves. 
	 * 
	 * They may request we initiate polling and alert them when the 
	 * server state is up. 
	 * 
	 * @param attr
	 */
	public IStatus clientSetServerStarting(ServerStartingAttributes attr);
	
	/**
	 * Set the server state to starting. 
	 * This is most likely to be called by the model, passing along
	 * a request from an external client that wished to launch the process
	 * themselves. 
	 */
	public IStatus clientSetServerStarted(LaunchParameters attr);
	
	/**
	 * Start the server in the given mode
	 * 
	 * @param mode
	 * @return
	 */
	public StartServerResponse start(String mode);
	
	/**
	 * Stop the server. 
	 * 
	 * @param force
	 * @return
	 */
	public IStatus stop(boolean force);

	/**
	 * Get the launch command for this server in the given mode
	 * @param mode
	 * @param params
	 * @return
	 */
	public CommandLineDetails getStartLaunchCommand(String mode, ServerAttributes params);
	
	
	/**
	 * Get the publish model for this server
	 * @return
	 */
	public IServerPublishModel getServerPublishModel();

	/**
	 * Can this deployable be added to this server? 
	 * @param handle
	 * @param req
	 * @return
	 */
	public IStatus canAddDeployable(DeployableReference ref);

	
	/**
	 * Can this deployable be removed from this server?
	 * @param reference
	 * @return
	 */
	public IStatus canRemoveDeployable(DeployableReference reference);

	/**
	 * A request to publish the server
	 * @param kind
	 * @return
	 * @throws CoreException 
	 */
	public IStatus publish(int kind);

	/**
	 * Get the server state, including run state, publish state, 
	 * as well as the run state and publish state for the deployables.
	 * @return
	 */
	public ServerState getServerState();

	/**
	 * Is the server in a state that can be published to?
	 * @return
	 */
	public IStatus canPublish();

	/**
	 * Get the publish state for this server
	 * @return
	 */
	int getServerPublishState();

	/**
	 * List deployment options for this server
	 * @return
	 */
	public Attributes listDeploymentOptions();
	
	/**
	 * List all currently accessible / enabled server actions
	 * for the given server. 
	 * 
	 * @return
	 */
	public ListServerActionResponse listServerActions();

	/**
	 * Execute a given server action request
	 * 
	 * @return
	 */
	public WorkflowResponse executeServerAction(ServerActionRequest req);

	/**
	 * Take all actions required to update this server to look exactly 
	 * like the dummy server from the client's edits. 
	 * 
	 * Validation should occur first, to ensure that all fields are 100%
	 * able to be saved. If any fields are invalid or cannot be saved, 
	 * the update should not occur. 
	 * 
	 * In the event of an error, subclasses must make sure to roll-back any changes
	 * so as to avoid an inconsistent state. 
	 * 
	 * @param dummyServer
	 * @param resp
	 */
	public void updateServer(IServer dummyServer, UpdateServerResponse resp);


	/**
	 * Allow the delegate the opportunity to fill default values for any 
	 * of the server's attributes  
	 * 
	 * @param server
	 */
	public void setDefaults(IServerWorkingCopy server);


}
