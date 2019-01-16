export interface DeployableState {
    server: ServerHandle;
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface DeployableReference {
    label: string;
    path: string;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}