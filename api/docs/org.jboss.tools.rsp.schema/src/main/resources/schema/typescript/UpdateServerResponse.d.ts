export interface UpdateServerResponse {
    handle: ServerHandle;
    validation: CreateServerResponse;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface CreateServerResponse {
    status: Status;
    invalidKeys: string[];
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