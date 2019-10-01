export interface UpdateServerResponse {
    handle: ServerHandle;
    validation: CreateServerResponse;
    serverJson: GetServerJsonResponse;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface CreateServerResponse {
    status: Status;
    invalidKeys: string[];
}

export interface GetServerJsonResponse {
    status: Status;
    serverJson: string;
    serverHandle: ServerHandle;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}

export interface Status {
    severity: number;
    plugin: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
}