export interface ServerType {
    id: string;
}

export interface VMDescription {
    id: string;
    installLocation: string;
    version: string;
}

export interface ServerProcessOutput {
    server: ServerHandle;
    processId: string;
    streamType: number;
    text: string;
}

export interface ServerHandle {
    id: string;
    type: string;
}

export interface ServerStartingAttributes {
    initiatePolling: boolean;
    request: LaunchCommandRequest;
}

export interface LaunchCommandRequest {
    mode: string;
    params: ServerAttributes;
}

export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
}

export interface CommandLineDetails {
    cmdLine: string[];
    workingDir: string;
    envp: string[];
}

export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
}

export interface Status {
    severity: number;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}

export interface LaunchAttributesRequest {
    id: string;
    mode: string;
}

export interface DiscoveryPath {
    filepath: string;
}

export interface ServerStateChange {
    server: ServerHandle;
    state: number;
}

export interface ServerHandle {
    id: string;
    type: string;
}

export interface Attributes {
    attributes: { [index: string]: Attribute };
}

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
}

export interface StopServerAttributes {
    id: string;
    force: boolean;
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

export interface StartServerAttributes {
    id: string;
    mode: string;
}

export interface VMHandle {
    id: string;
}

export interface ServerHandle {
    id: string;
    type: string;
}

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
}

export interface LaunchCommandRequest {
    mode: string;
    params: ServerAttributes;
}

export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
}

export interface ServerProcess {
    server: ServerHandle;
    processId: string;
}

export interface ServerHandle {
    id: string;
    type: string;
}

