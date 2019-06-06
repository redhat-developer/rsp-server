import { NotificationType, RequestType } from 'vscode-jsonrpc';
import { Protocol } from './protocol';

/**
 * Message types sent between the RSP server and the client
 */
export namespace Messages {

    /**
     * Server methods
     */
    export namespace Server {

        /**
         * Register client capabilities so the server knows what this client can support
         */
        export namespace RegisterClientCapabilitiesRequest {
            export const type = new RequestType<Protocol.ClientCapabilitiesRequest, Protocol.ServerCapabilitiesResponse, void, void>('server/registerClientCapabilities');
        }
        /**
         * The `server/shutdown` notification is sent by the client to shut down the
         * RSP itself.
         */
        export namespace ShutdownNotification {
            export const type = new NotificationType<void, void>('server/shutdown');
        }
        /**
         * The `server/getDiscoveryPaths` request is sent by the client to fetch a list
         * of discovery paths that can be searched.
         *
         * Discovery paths exist in the RSP model as paths suitable to be searched for
         * server runtime installations. Additional paths may be added via the
         * `server/addDiscoveryPath` entry point, or removed via the
         * `server/removeDiscoveryPath` entry point.
         */
        export namespace GetDiscoveryPathsRequest {
            export const type = new RequestType<void, Array<Protocol.DiscoveryPath>, void, void>('server/getDiscoveryPaths');
        }
        /**
         * The `server/findServerBeans` request is sent by the client to fetch a list of
         * server beans for the given path.
         *
         * The RSP model will iterate through a number of `IServerBeanTypeProvider`
         * instances and ask them if they recognize the contents of the folder
         * underlying the discovery path. Any providers that claim to be able to handle
         * the given path will return an object representing the details of this
         * recognized server runtime, its version, etc.
         *
         * The path parameter must be an absolute file-system path, and
         * may not be a relative path.
         */
        export namespace FindServerBeansRequest {
            export const type = new RequestType<Protocol.DiscoveryPath, Array<Protocol.ServerBean>, void, void>('server/findServerBeans');
        }
        /**
         * The `server/addDiscoveryPath` request is sent by the client to add a new
         * path to search when discovering servers. These paths will be stored in a
         * model, to be queried or searched later by a client.
         *
         * The path parameter must be an absolute file-system path, and
         * may not be a relative path.
         */
        export namespace AddDiscoveryPathRequest {
            export const type = new RequestType<Protocol.DiscoveryPath, Protocol.Status, void, void>('server/addDiscoveryPath');
        }
        /**
         * The `server/removeDiscoveryPath` request is sent by the client to remove
         * a path from the model and prevent it from being searched by clients when
         * discovering servers in the future.
         *
         * The path parameter must be an absolute file-system path, and
         * may not be a relative path.
         */
        export namespace RemoveDiscoveryPathRequest {
            export const type = new RequestType<Protocol.DiscoveryPath, Protocol.Status, void, void>('server/removeDiscoveryPath');
        }
        /**
         * The `server/getServerHandles` request is sent by the client to list the
         * server adapters currently configured. A server adapter is configured when a
         * call to `server/createServer` completes without error, or, some may be
         * pre-configured by the server upon installation.
         */
        export namespace GetServerHandlesRequest {
            export const type = new RequestType<void, Array<Protocol.ServerHandle>, void, void>('server/getServerHandles');
        }
        /**
         * The `server/getServerTypes` request is sent by the client to list the server
         * types currently supported. The details of how many server types are supported
         * by an RSP, or how they are registered, is implementation-specific.
         */
        export namespace GetServerTypesRequest {
            export const type = new RequestType<void, Array<Protocol.ServerType>, void, void>('server/getServerTypes');
        }
        /**
         * The `server/deleteServer` request is sent by the client to delete a
         * server from the model. This server will no longer be able to be started, shut
         * down, or interacted with in any fashion.
         */
        export namespace DeleteServerRequest {
            export const type = new RequestType<Protocol.ServerHandle, Protocol.Status, void, void>('server/deleteServer');
        }
        /**
         * The `server/getRequiredAttributes` request is sent by the client to list the
         * required attributes that must be stored on a server object of this type, such
         * as a server-home or other required parameters. This request may return null in
         * case of error.
         */
        export namespace GetRequiredAttributesRequest {
            export const type = new RequestType<Protocol.ServerType, Protocol.Attributes, void, void>('server/getRequiredAttributes');
        }
        /**
         * The `server/getOptionalAttributes` request is sent by the client to list the
         * optional attributes that can be stored on a server object of this type. This
         * may include things like customizing ports, or custom methods of interacting
         * with various functionality specific to the server type. This request may return
         * null in case of error.
         */
        export namespace GetOptionalAttributesRequest {
            export const type = new RequestType<Protocol.ServerType, Protocol.Attributes, void, void>('server/getOptionalAttributes');
        }
        /**
         * The `server/createServer` request is sent by the client to create a server in
         * the model using the given attributes (both required and optional. This
         * request may fail if required attributes are missing, any attributes
         * have impossible, unexpected, or invalid values, or any error occurs
         * while attempting to create the server adapter as requested.
         *
         * In the event of failure, the returned `Status` object will
         * detail the cause of error.
         */
        export namespace CreateServerRequest {
            export const type = new RequestType<Protocol.ServerAttributes, Protocol.CreateServerResponse, void, void>('server/createServer');
        }
        /**
         * The `server/getServerAsJson` request is sent by the client to
         * fetch a string representation of a server adapter.
         * A server handle and a status object are included along with the
         * json representation of the server.
         *
         * In the event of failure, the returned `Status` object will
         * detail the cause of error.
         */
        export namespace GetServerAsJsonRequest {
            export const type = new RequestType<Protocol.ServerHandle, Protocol.GetServerJsonResponse, void, void>('server/getServerAsJson');
        }
        /**
         * The `server/updateServer` request is sent by the client to
         * update a server with new k/v pairs or changed deployment options.
         *
         * In the event of failure, the returned `Status` object will
         * detail the cause of error.
         */
        export namespace UpdateServerRequest {
            export const type = new RequestType<Protocol.UpdateServerRequest, Protocol.UpdateServerResponse, void, void>('server/updateServer');
        }
        /**
         * The `server/getLaunchModes` request is sent by the client to get
         * a list of launch modes that are applicable to this server type.
         * Some servers can only be started.
         * Others can be started, debugged, profiled, etc.
         *
         * Server types may come up with their own launch modes if desired.
         * This method may return null if an error occurs on the server or
         * the parameter is invalid.
         */
        export namespace GetLaunchModesRequest {
            export const type = new RequestType<Protocol.ServerType, Array<Protocol.ServerLaunchMode>, void, void>('server/getLaunchModes');
        }
        /**
         * The `server/getRequiredLaunchAttributes` request is sent by the client to get
         * any additional attributes required for launch or that can customize launch
         * behavior. Some server types may require references to a specific library, a
         * clear decision about which of several configurations the server should be
         * launched with, or any other required details required to successfully start
         * up the server.
         *
         * This request may return null if the parameter is invalid.
         */
        export namespace GetRequiredLaunchAttributesRequest {
            export const type = new RequestType<Protocol.LaunchAttributesRequest, Protocol.Attributes, void, void>('server/getRequiredLaunchAttributes');
        }
        /**
         * The `server/getOptionalLaunchAttributes` request is sent by the client to get
         * any optional attributes which can be used to modify the launch behavior. Some
         * server types may allow overrides to any number of launch flags or settings,
         * but not require these changes in order to function.
         *
         * This request may return null if the parameter is invalid.
         */
        export namespace GetOptionalLaunchAttributesRequest {
            export const type = new RequestType<Protocol.LaunchAttributesRequest, Protocol.Attributes, void, void>('server/getOptionalLaunchAttributes');
        }
        /**
         * The `server/getLaunchCommand` request is sent by the client to the server to
         * get the command which can be used to launch the server.
         *
         * This entry point is most often used if an editor or IDE wishes to start
         * the server by itself, but does not know the servertype-specific command
         * that must be launched. The parameter will include a mode the server
         * should run in (run, debug, etc), as well as any custom attributes
         * that may have an effect on the generation of the launch command.
         *
         * This request may return null if the parameter is invalid.
         */
        export namespace GetLaunchCommandRequest {
            export const type = new RequestType<Protocol.LaunchParameters, Protocol.CommandLineDetails, void, void>('server/getLaunchCommand');
        }
        /**
         * The `server/serverStartingByClient` request is sent by the client to the
         * server to inform the server that the client itself has launched the server
         * instead of asking the RSP to do so.
         *
         * The parameters include both the request used to get the launch command, and a
         * boolean as to whether the server should initiate the 'state-polling'
         * mechanism to inform the client when the selected server has completed its
         * startup.
         *
         * If the `polling` boolean is false, the client is expected to also alert
         * the RSP when the launched server has completed its startup via the
         * `server/serverStartedByClient` request.
         */
        export namespace ServerStartingByClientRequest {
            export const type = new RequestType<Protocol.ServerStartingAttributes, Protocol.Status, void, void>('server/serverStartingByClient');
        }
        /**
         * The `server/serverStartedByClient` request is sent by the client to the
         * server to inform the server that the client itself has launched the server
         * instead of asking the RSP to do so, AND that the startup has completed.
         */
        export namespace ServerStartedByClientRequest {
            export const type = new RequestType<Protocol.LaunchParameters, Protocol.Status, void, void>('server/serverStartedByClient');
        }
        /**
         * Get the state of the current server
         */
        export namespace GetServerStateRequest {
            export const type = new RequestType<Protocol.ServerHandle, Protocol.ServerState, void, void>('server/getServerState');
        }
        /**
         * The `server/startServerAsync` request is sent by the client to the server to
         * start an existing server in the model.
         *
         * This request will cause the server to launch the server and
         * keep organized the spawned processes, their I/O streams,
         * and any events that must be propagated to the client.
         */
        export namespace StartServerAsyncRequest {
            export const type = new RequestType<Protocol.LaunchParameters, Protocol.StartServerResponse, void, void>('server/startServerAsync');
        }
        /**
         * The `server/stopServerAsync` request is sent by the client to the server to
         * stop an existing server in the model.
         */
        export namespace StopServerAsyncRequest {
            export const type = new RequestType<Protocol.StopServerAttributes, Protocol.Status, void, void>('server/stopServerAsync');
        }
        /**
         * The `server/getDeployables` request is sent by the client to the server to
         * get a list of all deployables
         */
        export namespace GetDeployablesRequest {
            export const type = new RequestType<Protocol.ServerHandle, Array<Protocol.DeployableState>, void, void>('server/getDeployables');
        }
        /**
         * The `server/listDeploymentOptions` request is sent by the client to list the
         * possible attributes that may be used when adding a new deployment.
         * This can be any set of custom server-defined settings, or standard options,
         * for example, an output name for an archive, whether to zip folders, etc.
         * This request may return null in case of error.
         *
         * @param serverHandle
         */
        export namespace ListDeploymentOptionsRequest {
            export const type = new RequestType<Protocol.ServerHandle, Protocol.Attributes, void, void>('server/listDeploymentOptions');
        }
        /**
         * The `server/addDeployable` request is sent by the client to the server
         * to add a deployable reference to a server's list of deployable items
         * so that it can be published thereafter.
         *
         * @param handle
         * @param reference
         * @return
         */
        export namespace AddDeployableRequest {
            export const type = new RequestType<Protocol.ServerDeployableReference, Protocol.Status, void, void>('server/addDeployable');
        }
        /**
         * The `server/removeDeployable` request is sent by the client to the server
         * to remove a deployable reference from a server's list of deployable items
         * so that it can be unpublished thereafter.
         *
         * @param handle
         * @param reference
         * @return
         */
        export namespace RemoveDeployableRequest {
            export const type = new RequestType<Protocol.ServerDeployableReference, Protocol.Status, void, void>('server/removeDeployable');
        }
        /**
         * The `server/publish` request is sent by the client to the server
         * to instruct the server adapter to publish any changes to the backing runtime.
         *
         * @param request
         * @return
         */
        export namespace PublishRequest {
            export const type = new RequestType<Protocol.PublishServerRequest, Protocol.Status, void, void>('server/publish');
        }
        /**
         * Get a list of all downloadable runtimes
         * @return
         */
        export namespace ListDownloadableRuntimesRequest {
            export const type = new RequestType<void, Protocol.ListDownloadRuntimeResponse, void, void>('server/listDownloadableRuntimes');
        }
        /**
         * Initiate a request to download a runtime
         * @return
         */
        export namespace DownloadRuntimeRequest {
            export const type = new RequestType<Protocol.DownloadSingleRuntimeRequest, Protocol.WorkflowResponse, void, void>('server/downloadRuntime');
        }
        /**
         * Initiate a request to get a view of the current job model
         * @return
         */
        export namespace GetJobsRequest {
            export const type = new RequestType<void, Array<Protocol.JobProgress>, void, void>('server/getJobs');
        }
        /**
         * Initiate a request to cancel a given job
         * @param job The handle to the given job
         * @return
         */
        export namespace CancelJobRequest {
            export const type = new RequestType<Protocol.JobHandle, Protocol.Status, void, void>('server/cancelJob');
        }
    }
    /**
     * Client methods
     */
    export namespace Client {
        /**
         * Prompt the user for some feature
         */
        export namespace PromptStringRequest {
            export const type = new RequestType<Protocol.StringPrompt, string, void, void>('client/promptString');
        }
        /**
         * The `client/discoveryPathAdded` notification is sent by the server to all
         * clients in response to the `server/addDiscoveryPath` notification.
         *
         * This call indicates that a discovery path has been added to the RSP model
         * which keeps track of filesystem paths that may be searched for server
         * runtimes.
         */
        export namespace DiscoveryPathAddedNotification {
            export const type = new NotificationType<Protocol.DiscoveryPath, void>('client/discoveryPathAdded');
        }
        /**
         * The `client/discoveryPathRemoved` notification is sent by the server to all
         * clients in response to the `server/removeDiscoveryPath` notification.
         *
         * This call indicates that a discovery path has been removed from the RSP model
         * which keeps track of filesystem paths that may be searched for server
         * runtimes.
         */
        export namespace DiscoveryPathRemovedNotification {
            export const type = new NotificationType<Protocol.DiscoveryPath, void>('client/discoveryPathRemoved');
        }
        /**
         * The `client/serverAdded` notification is sent by the server to all clients in
         * a response to the `server/createServer` notification.
         *
         * This notification indicates that a new server adapter has been created in the
         * RSP model of existing servers. As mentioned above, this was most likely in
         * response to a server/createServer notification, but is not strictly limited
         * to this entrypoint.
         */
        export namespace ServerAddedNotification {
            export const type = new NotificationType<Protocol.ServerHandle, void>('client/serverAdded');
        }
        /**
         * The `client/serverRemoved` notification is sent by the server to all clients
         * in response to the `server/deleteServer` notification.
         *
         * This notification indicates that a server adapter has been removed from the
         * RSP model of existing servers. As mentioned above, this was most likely in
         * response to a server/deleteServer notification, but is not strictly limited
         * to this entrypoint.
         */
        export namespace ServerRemovedNotification {
            export const type = new NotificationType<Protocol.ServerHandle, void>('client/serverRemoved');
        }
        /**
         * The `client/serverRemoved` notification is sent by the server to all clients
         * when any server has had one of its attributes changed.
         */
        export namespace ServerAttributesChangedNotification {
            export const type = new NotificationType<Protocol.ServerHandle, void>('client/serverAttributesChanged');
        }
        /**
         * The `client/serverStateChanged` notification is sent by the server to all
         * clients when any server has had its state change.
         *
         * Possible values include:
         *   `0` representing an unknown state
         *   `1` representing starting
         *   `2` representing started
         *   `3` representing stopping
         *   `4` representing stopped
         *
         */
        export namespace ServerStateChangedNotification {
            export const type = new NotificationType<Protocol.ServerState, void>('client/serverStateChanged');
        }
        /**
         * The `client/serverProcessCreated` notification is sent
         * by the server to all clients when any server
         * has launched a new process which can be monitored.
         *
         * This notification is most often sent in response to a call to
         * `server/startServerAsync` which will typically launch a process
         * to run the server in question.
         */
        export namespace ServerProcessCreatedNotification {
            export const type = new NotificationType<Protocol.ServerProcess, void>('client/serverProcessCreated');
        }
        /**
         * The `client/serverProcessTerminated` notification is sent by
         * the server to all clients when any process associated with a
         * server has been terminated.
         *
         * This notification is most often sent as a result of a call to
         * `server/stopServerAsync`, which  should shut down a given server
         * and cause all of that server's processes to terminate after some time.
         */
        export namespace ServerProcessTerminatedNotification {
            export const type = new NotificationType<Protocol.ServerProcess, void>('client/serverProcessTerminated');
        }
        /**
         * The `client/serverProcessOutputAppended` notification is sent by
         * the server to all clients when any process associated with a
         * server generated output on any of its output streams.
         *
         * This notification may be sent as a result of anything that
         * causes a given server process to emit output, such as a change in
         * configuration, a deployment, an error, normal logging,
         * or any other number of possibilities.
         */
        export namespace ServerProcessOutputAppendedNotification {
            export const type = new NotificationType<Protocol.ServerProcessOutput, void>('client/serverProcessOutputAppended');
        }
        /**
         * The `client/jobAdded` notification is sent by
         * the server to all clients when any long-running task
         * has been scheduled on the server.
         *
         * This notification may be sent by any procedure that an extension
         * to the RSP may decide requires a long-running task to be registered.
         */
        export namespace JobAddedNotification {
            export const type = new NotificationType<Protocol.JobHandle, void>('client/jobAdded');
        }
        /**
         * The `client/jobRemoved` notification is sent by
         * the server to all clients when any long-running task
         * has been completed, canceled, or errors.
         *
         * This notification may be sent by the server when any of its long-running
         * tasks have been completed;
         */
        export namespace JobRemovedNotification {
            export const type = new NotificationType<Protocol.JobRemoved, void>('client/jobRemoved');
        }
        /**
         * The `client/jobChanged` notification is sent by the server to all clients
         * when any long-running task has its progress updated.
         */
        export namespace JobChangedNotification {
            export const type = new NotificationType<Protocol.JobProgress, void>('client/jobChanged');
        }
    }
}
