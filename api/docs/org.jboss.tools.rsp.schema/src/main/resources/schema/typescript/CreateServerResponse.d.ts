export interface CreateServerResponse {
    status: Status;
    invalidKeys: string[];
}

export interface Status {
    severity: number;
    plugin: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
}