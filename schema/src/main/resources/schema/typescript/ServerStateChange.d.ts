export interface ServerStateChange {
    server: ServerHandle;
    state: number;
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