export interface ServerCapabilitiesResponse {
    clientRegistrationStatus: Status;
    map: { [index: string]: string };
}

export interface Status {
    severity: number;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}