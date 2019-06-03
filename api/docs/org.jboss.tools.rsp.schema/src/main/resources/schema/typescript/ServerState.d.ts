export interface ServerState {
    server: ServerHandle;
    state: number;
    publishState: number;
    runMode: string;
    deployableStates: DeployableState[];
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface DeployableState {
    server: ServerHandle;
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}

export interface DeployableReference {
    label: string;
    path: string;
    options?: { [index: string]: any };
}