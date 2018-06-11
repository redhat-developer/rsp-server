/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.jboss.tools.ssp.api.dao.Attributes;
import org.jboss.tools.ssp.api.dao.CommandLineDetails;
import org.jboss.tools.ssp.api.dao.DiscoveryPath;
import org.jboss.tools.ssp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.ssp.api.dao.LaunchParameters;
import org.jboss.tools.ssp.api.dao.ServerAttributes;
import org.jboss.tools.ssp.api.dao.ServerBean;
import org.jboss.tools.ssp.api.dao.ServerHandle;
import org.jboss.tools.ssp.api.dao.ServerStartingAttributes;
import org.jboss.tools.ssp.api.dao.ServerType;
import org.jboss.tools.ssp.api.dao.Status;
import org.jboss.tools.ssp.api.dao.StopServerAttributes;

@JsonSegment("server")
public interface SSPServer {

	/**
	 * The `server/getDiscoveryPaths` request is sent by the client to fetch a list
	 * of discovery paths that can be searched.
	 * 
	 * Discovery paths exist in the SSP model as paths suitable to be searched for
	 * server runtime installations. Additional paths may be added via the
	 * `server/addDiscoveryPath` entry point, or removed via the
	 * `server/removeDiscoveryPath` entry point.
	 */
	@JsonRequest
	CompletableFuture<List<DiscoveryPath>> getDiscoveryPaths();

	/**
	 * The `server/findServerBeans` request is sent by the client to fetch a list of
	 * server beans for the given path.
	 * 
	 * The SSP model will iterate through a number of `IServerBeanTypeProvider`
	 * instances and ask them if they recognize the contents of the folder
	 * underlying the discovery path. Any providers that claim to be able to handle
	 * the given path will return an object representing the details of this
	 * recognized server runtime, its version, etc.
	 */
	@JsonRequest
	CompletableFuture<List<ServerBean>> findServerBeans(DiscoveryPath path);

	/**
	 * The `server/addDiscoveryPath` notification is sent by the client to add a new
	 * path to search when discovering servers. These paths will be stored in a
	 * model, to be queried or searched later by a client.
	 */
	@JsonNotification
	void addDiscoveryPath(DiscoveryPath path);

	/**
	 * The `server/removeDiscoveryPath` notification is sent by the client to remove
	 * a path from the model and prevent it from being searched by clients when
	 * discovering servers in the future.
	 */
	@JsonNotification
	void removeDiscoveryPath(DiscoveryPath path);

	/**
	 * The `server/getServerHandles` request is sent by the client to list the
	 * server adapters currently configured. A server adapter is configured when a
	 * call to `server/createServer` completes without error, or, some may be
	 * pre-configured by the server upon installation.
	 */
	@JsonRequest
	CompletableFuture<List<ServerHandle>> getServerHandles();

	/**
	 * The `server/getServerTypes` request is sent by the client to list the server
	 * types currently supported. The details of how many server types are supported
	 * by an SSP, or how they are registered, is implementation-specific.
	 */
	@JsonRequest
	CompletableFuture<List<ServerType>> getServerTypes();

	/**
	 * The `server/deleteServer` notification is sent by the client to delete a
	 * server from the model. This server will no longer be able to be started, shut
	 * down, or interacted with in any fashion.
	 */
	@JsonNotification
	void deleteServer(ServerHandle handle);

	/**
	 * The `server/getRequiredAttributes` request is sent by the client to list the
	 * required attributes that must be stored on a server object of this type, such
	 * as a server-home or other required parameters.
	 */
	@JsonRequest
	CompletableFuture<Attributes> getRequiredAttributes(ServerType type);

	/**
	 * The `server/getOptionalAttributes` request is sent by the client to list the
	 * optional attributes that can be stored on a server object of this type. This
	 * may include things like customizing ports, or custom methods of interacting
	 * with various functionality specific to the server type.
	 */
	@JsonRequest
	CompletableFuture<Attributes> getOptionalAttributes(ServerType type);

	/**
	 * The `server/getRequiredLaunchAttributes` request is sent by the client to get
	 * any additional attributes required for launch or that can customize launch
	 * behavior. Some server types may require references to a specific library, a
	 * clear decision about which of several configurations the server should be
	 * launched with, or any other required details required to successfully start
	 * up the server.
	 */
	@JsonRequest
	CompletableFuture<Attributes> getRequiredLaunchAttributes(LaunchAttributesRequest req);

	/**
	 * The `server/getOptionalLaunchAttributes` request is sent by the client to get
	 * any optional attributes which can be used to modify the launch behavior. Some
	 * server types may allow overrides to any number of launch flags or settings, 
	 * but not require these changes in order to function. 
	 */
	@JsonRequest
	CompletableFuture<Attributes> getOptionalLaunchAttributes(LaunchAttributesRequest req);

	/**
	 * The `server/createServer` request is sent by the client to create a server in
	 * the model using the given attributes (both required and optional. This
	 * request may fail if required attributes are missing, any attributes 
	 * have impossible, unexpected, or invalid values, or any error occurs
	 * while attempting to create the server adapter as requested.
	 * 
	 * In the event of failure, the returend `Status` object will 
	 * detail the cause of error.   
	 */
	@JsonRequest
	CompletableFuture<Status> createServer(ServerAttributes csa);

	/**
	 * The `server/getLaunchCommand` request is sent by the client to the server to
	 * get the command which can be used to launch the server.
	 * 
	 * This entry point is most often used if an editor or IDE wishes to start 
	 * the server by itself, but does not know the servertype-specific command
	 * that must be launched. The parameter will include a mode the server
	 * should run in (run, debug, etc), as well as any custom attributes
	 * that may have an effect on the generation of the launch command.
	 */
	@JsonRequest
	CompletableFuture<CommandLineDetails> getLaunchCommand(LaunchParameters req);

	/**
	 * The `server/serverStartingByClient` request is sent by the client to the
	 * server to inform the server that the client itself has launched the server
	 * instead of asking the SSP to do so.
	 * 
	 * The parameters include both the request used to get the launch command, and a
	 * boolean as to whether the server should initiate the 'state-polling'
	 * mechanism to inform the client when the selected server has completed its
	 * startup.
	 * 
	 * If the `polling` boolean is false, the client is expected to also alert
	 * the SSP when the launched server has completed its startup via the 
	 * `server/serverStartedByClient` request. 
	 */
	@JsonRequest
	CompletableFuture<Status> serverStartingByClient(ServerStartingAttributes attr);

	/**
	 * The `server/serverStartedByClient` request is sent by the client to the
	 * server to inform the server that the client itself has launched the server
	 * instead of asking the SSP to do so, AND that the startup has completed.
	 */
	@JsonRequest
	CompletableFuture<Status> serverStartedByClient(LaunchParameters attr);

	/**
	 * The `server/startServerAsync` request is sent by the client to the server to
	 * start an existing server in the model.
	 * 
	 * This request will cause the server to launch the server and 
	 * keep organized the spawned processes, their I/O streams, 
	 * and any events that must be propagated to the client. 
	 */
	@JsonRequest
	CompletableFuture<Status> startServerAsync(LaunchParameters params);

	/**
	 * The `server/stopServerAsync` request is sent by the client to the server to
	 * stop an existing server in the model.
	 */
	@JsonRequest
	CompletableFuture<Status> stopServerAsync(StopServerAttributes attr);

	/**
	 * The `server/shutdown` notification is sent by the client to shut down the
	 * SSP itself. 
	 */
	@JsonNotification
	void shutdown();
}
