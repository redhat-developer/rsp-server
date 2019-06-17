export interface ListDeployablesResponse {
    states: DeployableState[];
    status: Status;
}

export interface DeployableState {
    server: ServerHandle;
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface Status {
    severity: number;
    plugin: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface DeployableReference {
    label: string;
    path: string;
    options?: { [index: string]: any };
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}