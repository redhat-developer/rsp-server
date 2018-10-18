export interface ServerState {
    server: ServerHandle;
    state: number;
    publishState: number;
    moduleState: ModuleState[];
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ModuleState {
    id: string;
    path: string;
    state: number;
    publishState: number;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}