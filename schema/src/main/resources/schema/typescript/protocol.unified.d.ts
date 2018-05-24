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
    plugin: string;
    ok: boolean;
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

export interface CreateServerAttribute {
    type: string;
    description: string;
    defaultVal: any;
}

export interface ServerHandle {
    id: string;
    type: string;
}

export interface ServerProcess {
    server: ServerHandle;
    processId: string;
}

export interface ServerHandle {
    id: string;
    type: string;
}

export interface CreateServerAttributes {
    attributes: { [index: string]: CreateServerAttribute };
}

export interface CreateServerAttribute {
    type: string;
    description: string;
    defaultVal: any;
}

