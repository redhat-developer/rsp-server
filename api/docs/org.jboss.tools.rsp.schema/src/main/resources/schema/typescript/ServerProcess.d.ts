export interface ServerProcess {
    server: ServerHandle;
    processId: string;
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