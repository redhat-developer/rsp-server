export interface ServerState {
    server: ServerHandle;
    state: number;
    publishState: number;
    moduleState: DeployableState[];
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface DeployableState {
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
    id: string;
    path: string;
}