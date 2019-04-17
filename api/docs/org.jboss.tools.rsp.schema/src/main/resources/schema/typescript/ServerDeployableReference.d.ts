export interface ServerDeployableReference {
    server: ServerHandle;
    deployableReference: DeployableReference;
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