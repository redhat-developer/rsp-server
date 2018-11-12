export interface CreateServerResponse {
    status: Status;
    invalidKeys: string[];
}

export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}