/* tslint:disable */
// Generated using typescript-generator version 2.2.413 on 2019-06-03 12:08:28.

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
    secret: boolean;
}

export interface Attributes {
    attributes: { [index: string]: Attribute };
}

export interface ClientCapabilitiesRequest {
    map: { [index: string]: string };
}

export interface CommandLineDetails {
    cmdLine: string[];
    workingDir: string;
    envp: string[];
    properties: { [index: string]: string };
}

export interface CreateServerResponse {
    status: Status;
    invalidKeys: string[];
}

export interface DeployableReference {
    label: string;
    path: string;
    options?: { [index: string]: any };
}

export interface DeployableState {
    server: ServerHandle;
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface DiscoveryPath {
    filepath: string;
}

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

export interface DownloadSingleRuntimeRequest {
    requestId: number;
    downloadRuntimeId: string;
    data: { [index: string]: any };
}

export interface JobHandle {
    name: string;
    id: string;
}

export interface JobProgress {
    percent: number;
    handle: JobHandle;
}

export interface JobRemoved {
    status: Status;
    handle: JobHandle;
}

export interface LaunchAttributesRequest {
    serverTypeId: string;
    mode: string;
}

export interface LaunchParameters {
    mode: string;
    params: ServerAttributes;
}

export interface ListDownloadRuntimeResponse {
    runtimes: DownloadRuntimeDescription[];
}

export interface PublishServerRequest {
    server: ServerHandle;
    kind: number;
}

export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
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

export interface ServerCapabilitiesResponse {
    serverCapabilities: { [index: string]: string };
    clientRegistrationStatus: Status;
}

export interface ServerDeployableReference {
    server: ServerHandle;
    deployableReference: DeployableReference;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerLaunchMode {
    mode: string;
    desc: string;
}

export interface ServerProcess {
    server: ServerHandle;
    processId: string;
}

export interface ServerProcessOutput {
    server: ServerHandle;
    processId: string;
    streamType: number;
    text: string;
}

export interface ServerStartingAttributes {
    initiatePolling: boolean;
    request: LaunchParameters;
}

export interface ServerState {
    server: ServerHandle;
    state: number;
    publishState: number;
    runMode: string;
    deployableStates: DeployableState[];
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}

export interface StartServerResponse {
    status: Status;
    details: CommandLineDetails;
}

export interface Status {
    severity: number;
    plugin: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
}

export interface StopServerAttributes {
    id: string;
    force: boolean;
}

export interface StringPrompt {
    code: number;
    prompt: string;
    secret: boolean;
}

export interface VMDescription {
    id: string;
    installLocation: string;
    version: string;
}

export interface VMHandle {
    id: string;
}

export interface WorkflowResponse {
    status: Status;
    requestId: number;
    jobId: string;
    items: WorkflowResponseItem[];
}

export interface WorkflowResponseItem {
    id: string;
    itemType: string;
    label: string;
    content: string;
    responseType: string;
    responseSecret: boolean;
    validResponses: string[];
}
