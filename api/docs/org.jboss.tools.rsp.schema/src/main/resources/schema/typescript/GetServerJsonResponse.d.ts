export interface GetServerJsonResponse {
    status: Status;
    serverJson: string;
    serverHandle: ServerHandle;
}

export interface Status {
    severity: number;
    plugin: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
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