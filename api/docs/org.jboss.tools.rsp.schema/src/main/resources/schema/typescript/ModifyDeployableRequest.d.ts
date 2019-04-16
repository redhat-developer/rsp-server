export interface ModifyDeployableRequest {
    server: ServerHandle;
    deployable: DeployableReferenceWithOptions;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface DeployableReferenceWithOptions {
    reference: DeployableReference;
    options?: { [index: string]: any };
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}

export interface DeployableReference {
    label: string;
    path: string;
}