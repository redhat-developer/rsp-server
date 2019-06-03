export interface UpdateServerRequest {
    handle: ServerHandle;
    serverJson: string;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}