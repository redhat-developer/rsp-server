import { Protocol } from './protocol';
import { Messages } from './messages';
import { Common } from '../../util/common';
import { MessageConnection } from 'vscode-jsonrpc';

/**
 * Server Outgoing
 */
export class Outgoing {

    private connection: MessageConnection;

     /**
     * Constructs a new discovery handler
     * @param connection message connection to the RSP
     */
    constructor(connection: MessageConnection) {
        this.connection = connection;
    }
    registerClientCapabilities(param: Protocol.ClientCapabilitiesRequest, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.ServerCapabilitiesResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.RegisterClientCapabilitiesRequest.type,
            param, timeout, ErrorMessages.REGISTERCLIENTCAPABILITIES_TIMEOUT);
    }
    shutdown(timeout: number = Common.DEFAULT_TIMEOUT): void {
        return Common.sendSimpleNotification(this.connection, Messages.Server.ShutdownNotification.type, null);
    }
    shutdownIfLastClient(timeout: number = Common.DEFAULT_TIMEOUT): void {
        return Common.sendSimpleNotification(this.connection, Messages.Server.ShutdownIfLastClientNotification.type, null);
    }
    getDiscoveryPaths(timeout: number = Common.DEFAULT_TIMEOUT): Promise<Array<Protocol.DiscoveryPath>> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetDiscoveryPathsRequest.type,
            null, timeout, ErrorMessages.GETDISCOVERYPATHS_TIMEOUT);
    }
    findServerBeans(param: Protocol.DiscoveryPath, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Array<Protocol.ServerBean>> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.FindServerBeansRequest.type,
            param, timeout, ErrorMessages.FINDSERVERBEANS_TIMEOUT);
    }
    addDiscoveryPath(param: Protocol.DiscoveryPath, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.AddDiscoveryPathRequest.type,
            param, timeout, ErrorMessages.ADDDISCOVERYPATH_TIMEOUT);
    }
    removeDiscoveryPath(param: Protocol.DiscoveryPath, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.RemoveDiscoveryPathRequest.type,
            param, timeout, ErrorMessages.REMOVEDISCOVERYPATH_TIMEOUT);
    }
    getServerHandles(timeout: number = Common.DEFAULT_TIMEOUT): Promise<Array<Protocol.ServerHandle>> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetServerHandlesRequest.type,
            null, timeout, ErrorMessages.GETSERVERHANDLES_TIMEOUT);
    }
    getServerTypes(timeout: number = Common.DEFAULT_TIMEOUT): Promise<Array<Protocol.ServerType>> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetServerTypesRequest.type,
            null, timeout, ErrorMessages.GETSERVERTYPES_TIMEOUT);
    }
    deleteServer(param: Protocol.ServerHandle, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.DeleteServerRequest.type,
            param, timeout, ErrorMessages.DELETESERVER_TIMEOUT);
    }
    getRequiredAttributes(param: Protocol.ServerType, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Attributes> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetRequiredAttributesRequest.type,
            param, timeout, ErrorMessages.GETREQUIREDATTRIBUTES_TIMEOUT);
    }
    getOptionalAttributes(param: Protocol.ServerType, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Attributes> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetOptionalAttributesRequest.type,
            param, timeout, ErrorMessages.GETOPTIONALATTRIBUTES_TIMEOUT);
    }
    createServer(param: Protocol.ServerAttributes, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.CreateServerResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.CreateServerRequest.type,
            param, timeout, ErrorMessages.CREATESERVER_TIMEOUT);
    }
    getServerAsJson(param: Protocol.ServerHandle, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.GetServerJsonResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetServerAsJsonRequest.type,
            param, timeout, ErrorMessages.GETSERVERASJSON_TIMEOUT);
    }
    updateServer(param: Protocol.UpdateServerRequest, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.UpdateServerResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.UpdateServerRequest.type,
            param, timeout, ErrorMessages.UPDATESERVER_TIMEOUT);
    }
    getLaunchModes(param: Protocol.ServerType, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Array<Protocol.ServerLaunchMode>> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetLaunchModesRequest.type,
            param, timeout, ErrorMessages.GETLAUNCHMODES_TIMEOUT);
    }
    getRequiredLaunchAttributes(param: Protocol.LaunchAttributesRequest, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Attributes> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetRequiredLaunchAttributesRequest.type,
            param, timeout, ErrorMessages.GETREQUIREDLAUNCHATTRIBUTES_TIMEOUT);
    }
    getOptionalLaunchAttributes(param: Protocol.LaunchAttributesRequest, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Attributes> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetOptionalLaunchAttributesRequest.type,
            param, timeout, ErrorMessages.GETOPTIONALLAUNCHATTRIBUTES_TIMEOUT);
    }
    getLaunchCommand(param: Protocol.LaunchParameters, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.CommandLineDetails> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetLaunchCommandRequest.type,
            param, timeout, ErrorMessages.GETLAUNCHCOMMAND_TIMEOUT);
    }
    serverStartingByClient(param: Protocol.ServerStartingAttributes, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.ServerStartingByClientRequest.type,
            param, timeout, ErrorMessages.SERVERSTARTINGBYCLIENT_TIMEOUT);
    }
    serverStartedByClient(param: Protocol.LaunchParameters, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.ServerStartedByClientRequest.type,
            param, timeout, ErrorMessages.SERVERSTARTEDBYCLIENT_TIMEOUT);
    }
    getServerState(param: Protocol.ServerHandle, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.ServerState> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetServerStateRequest.type,
            param, timeout, ErrorMessages.GETSERVERSTATE_TIMEOUT);
    }
    startServerAsync(param: Protocol.LaunchParameters, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.StartServerResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.StartServerAsyncRequest.type,
            param, timeout, ErrorMessages.STARTSERVERASYNC_TIMEOUT);
    }
    stopServerAsync(param: Protocol.StopServerAttributes, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.StopServerAsyncRequest.type,
            param, timeout, ErrorMessages.STOPSERVERASYNC_TIMEOUT);
    }
    getDeployables(param: Protocol.ServerHandle, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.ListDeployablesResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetDeployablesRequest.type,
            param, timeout, ErrorMessages.GETDEPLOYABLES_TIMEOUT);
    }
    listDeploymentOptions(param: Protocol.ServerHandle, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.ListDeploymentOptionsResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.ListDeploymentOptionsRequest.type,
            param, timeout, ErrorMessages.LISTDEPLOYMENTOPTIONS_TIMEOUT);
    }
    addDeployable(param: Protocol.ServerDeployableReference, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.AddDeployableRequest.type,
            param, timeout, ErrorMessages.ADDDEPLOYABLE_TIMEOUT);
    }
    removeDeployable(param: Protocol.ServerDeployableReference, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.RemoveDeployableRequest.type,
            param, timeout, ErrorMessages.REMOVEDEPLOYABLE_TIMEOUT);
    }
    publish(param: Protocol.PublishServerRequest, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.PublishRequest.type,
            param, timeout, ErrorMessages.PUBLISH_TIMEOUT);
    }
    publishAsync(param: Protocol.PublishServerRequest, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.PublishAsyncRequest.type,
            param, timeout, ErrorMessages.PUBLISHASYNC_TIMEOUT);
    }
    listDownloadableRuntimes(timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.ListDownloadRuntimeResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.ListDownloadableRuntimesRequest.type,
            null, timeout, ErrorMessages.LISTDOWNLOADABLERUNTIMES_TIMEOUT);
    }
    downloadRuntime(param: Protocol.DownloadSingleRuntimeRequest, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.WorkflowResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.DownloadRuntimeRequest.type,
            param, timeout, ErrorMessages.DOWNLOADRUNTIME_TIMEOUT);
    }
    listServerActions(param: Protocol.ServerHandle, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.ListServerActionResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.ListServerActionsRequest.type,
            param, timeout, ErrorMessages.LISTSERVERACTIONS_TIMEOUT);
    }
    executeServerAction(param: Protocol.ServerActionRequest, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.WorkflowResponse> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.ExecuteServerActionRequest.type,
            param, timeout, ErrorMessages.EXECUTESERVERACTION_TIMEOUT);
    }
    getJobs(timeout: number = Common.DEFAULT_TIMEOUT): Promise<Array<Protocol.JobProgress>> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.GetJobsRequest.type,
            null, timeout, ErrorMessages.GETJOBS_TIMEOUT);
    }
    cancelJob(param: Protocol.JobHandle, timeout: number = Common.DEFAULT_TIMEOUT): Promise<Protocol.Status> {
        return Common.sendSimpleRequest(this.connection, Messages.Server.CancelJobRequest.type,
            param, timeout, ErrorMessages.CANCELJOB_TIMEOUT);
    }
}
/**
 * Error messages
 */
export namespace ErrorMessages {
    export const REGISTERCLIENTCAPABILITIES_TIMEOUT = 'Failed to register client capabilities in time';
    export const SHUTDOWN_TIMEOUT = 'Failed to shutdown in time';
    export const SHUTDOWNIFLASTCLIENT_TIMEOUT = 'Failed to shutdown if last client in time';
    export const GETDISCOVERYPATHS_TIMEOUT = 'Failed to get discovery paths in time';
    export const FINDSERVERBEANS_TIMEOUT = 'Failed to find server beans in time';
    export const ADDDISCOVERYPATH_TIMEOUT = 'Failed to add discovery path in time';
    export const REMOVEDISCOVERYPATH_TIMEOUT = 'Failed to remove discovery path in time';
    export const GETSERVERHANDLES_TIMEOUT = 'Failed to get server handles in time';
    export const GETSERVERTYPES_TIMEOUT = 'Failed to get server types in time';
    export const DELETESERVER_TIMEOUT = 'Failed to delete server in time';
    export const GETREQUIREDATTRIBUTES_TIMEOUT = 'Failed to get required attributes in time';
    export const GETOPTIONALATTRIBUTES_TIMEOUT = 'Failed to get optional attributes in time';
    export const CREATESERVER_TIMEOUT = 'Failed to create server in time';
    export const GETSERVERASJSON_TIMEOUT = 'Failed to get server as json in time';
    export const UPDATESERVER_TIMEOUT = 'Failed to update server in time';
    export const GETLAUNCHMODES_TIMEOUT = 'Failed to get launch modes in time';
    export const GETREQUIREDLAUNCHATTRIBUTES_TIMEOUT = 'Failed to get required launch attributes in time';
    export const GETOPTIONALLAUNCHATTRIBUTES_TIMEOUT = 'Failed to get optional launch attributes in time';
    export const GETLAUNCHCOMMAND_TIMEOUT = 'Failed to get launch command in time';
    export const SERVERSTARTINGBYCLIENT_TIMEOUT = 'Failed to server starting by client in time';
    export const SERVERSTARTEDBYCLIENT_TIMEOUT = 'Failed to server started by client in time';
    export const GETSERVERSTATE_TIMEOUT = 'Failed to get server state in time';
    export const STARTSERVERASYNC_TIMEOUT = 'Failed to start server async in time';
    export const STOPSERVERASYNC_TIMEOUT = 'Failed to stop server async in time';
    export const GETDEPLOYABLES_TIMEOUT = 'Failed to get deployables in time';
    export const LISTDEPLOYMENTOPTIONS_TIMEOUT = 'Failed to list deployment options in time';
    export const ADDDEPLOYABLE_TIMEOUT = 'Failed to add deployable in time';
    export const REMOVEDEPLOYABLE_TIMEOUT = 'Failed to remove deployable in time';
    export const PUBLISH_TIMEOUT = 'Failed to publish in time';
    export const PUBLISHASYNC_TIMEOUT = 'Failed to publish async in time';
    export const LISTDOWNLOADABLERUNTIMES_TIMEOUT = 'Failed to list downloadable runtimes in time';
    export const DOWNLOADRUNTIME_TIMEOUT = 'Failed to download runtime in time';
    export const LISTSERVERACTIONS_TIMEOUT = 'Failed to list server actions in time';
    export const EXECUTESERVERACTION_TIMEOUT = 'Failed to execute server action in time';
    export const GETJOBS_TIMEOUT = 'Failed to get jobs in time';
    export const CANCELJOB_TIMEOUT = 'Failed to cancel job in time';
}
