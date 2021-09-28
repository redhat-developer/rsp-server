/* tslint:disable */
// Generated using typescript-generator version 2.2.413 on 2021-09-28 16:58:30.

export interface DownloadRuntimeDescription {
    name: string;
    id: string;
    version: string;
    url: string;
    licenseURL: string;
    humanUrl: string;
    disclaimer: boolean;
    properties: { [index: string]: string };
    size: string;
    installationMethod: string;
}

export interface DeployableState {
    server: ServerHandle;
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface CreateServerResponse {
    status: Status;
    invalidKeys: string[];
}

export interface UpdateServerResponse {
    handle: ServerHandle;
    validation: CreateServerResponse;
    serverJson: GetServerJsonResponse;
}

export interface ServerState {
    server: ServerHandle;
    state: number;
    publishState: number;
    runMode: string;
    deployableStates: DeployableState[];
}

export interface ClientCapabilitiesRequest {
    map: { [index: string]: string };
}

export interface ServerStartingAttributes {
    initiatePolling: boolean;
    request: LaunchParameters;
}

export interface PublishServerRequest {
    server: ServerHandle;
    kind: number;
}

export interface UpdateServerRequest {
    handle: ServerHandle;
    serverJson: string;
}

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
    secret: boolean;
}

export interface ServerCapabilitiesResponse {
    serverCapabilities: { [index: string]: string };
    clientRegistrationStatus: Status;
}

export interface ListDownloadRuntimeResponse {
    runtimes: DownloadRuntimeDescription[];
}

export interface StartServerResponse {
    status: Status;
    details: CommandLineDetails;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerBean {
    location: string;
    typeCategory: string;
    specificType: string;
    name: string;
    version: string;
    fullVersion: string;
    serverAdapterTypeId: string;
}

export interface LaunchAttributesRequest {
    serverTypeId: string;
    mode: string;
}

export interface WorkflowPromptDetails {
    responseType: string;
    responseSecret: boolean;
    validResponses: string[];
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}

export interface ServerProcessOutput {
    server: ServerHandle;
    processId: string;
    streamType: number;
    text: string;
}

export interface JobHandle {
    name: string;
    id: string;
}

export interface ListDeployablesResponse {
    states: DeployableState[];
    status: Status;
}

export interface WorkflowResponseItem {
    id: string;
    itemType: string;
    label: string;
    content: string;
    prompt: WorkflowPromptDetails;
    properties: { [index: string]: string };
}

export interface ListDeploymentOptionsResponse {
    attributes: Attributes;
    status: Status;
}

export interface StopServerAttributes {
    id: string;
    force: boolean;
}

export interface DeployableReference {
    label: string;
    path: string;
    options?: { [index: string]: any };
}

export interface WorkflowResponse {
    status: Status;
    requestId: number;
    jobId: string;
    items: WorkflowResponseItem[];
    invalidFields: string[];
}

export interface ServerActionRequest {
    requestId: number;
    actionId: string;
    serverId: string;
    data: { [index: string]: any };
}

export interface LaunchParameters {
    mode: string;
    params: ServerAttributes;
}

export interface Status {
    severity: number;
    plugin: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
}

export interface JobRemoved {
    status: Status;
    handle: JobHandle;
}

export interface ServerProcess {
    server: ServerHandle;
    processId: string;
}

export interface VMHandle {
    id: string;
}

export interface ListServerActionResponse {
    workflows: ServerActionWorkflow[];
    status: Status;
}

export interface ServerLaunchMode {
    mode: string;
    desc: string;
}

export interface VMDescription {
    id: string;
    installLocation: string;
    version: string;
}

export interface CommandLineDetails {
    cmdLine: string[];
    workingDir: string;
    envp: string[];
    properties: { [index: string]: string };
}

export interface CreateServerWorkflowRequest {
    requestId: number;
    serverTypeId: string;
    data: { [index: string]: any };
}

export interface ServerActionWorkflow {
    actionId: string;
    actionLabel: string;
    actionWorkflow: WorkflowResponse;
}

export interface DiscoveryPath {
    filepath: string;
}

export interface MessageBoxNotification {
    code: number;
    severity: number;
    message: string;
    properties: { [index: string]: any };
}

export interface GetServerJsonResponse {
    status: Status;
    serverJson: string;
    serverHandle: ServerHandle;
}

export interface DownloadSingleRuntimeRequest {
    requestId: number;
    downloadRuntimeId: string;
    data: { [index: string]: any };
}

export interface JobProgress {
    percent: number;
    handle: JobHandle;
}

export interface ServerDeployableReference {
    server: ServerHandle;
    deployableReference: DeployableReference;
}

export interface StringPrompt {
    code: number;
    prompt: string;
    secret: boolean;
}

export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
}

export interface Attributes {
    attributes: { [index: string]: Attribute };
}
