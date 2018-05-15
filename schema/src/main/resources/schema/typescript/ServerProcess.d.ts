export interface ServerProcess {
    server: ServerHandle;
    processId: string;
}

export interface ServerHandle {
    id: string;
    type: string;
}