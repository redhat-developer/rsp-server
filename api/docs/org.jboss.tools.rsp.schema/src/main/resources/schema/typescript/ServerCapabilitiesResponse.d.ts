export interface ServerCapabilitiesResponse {
    serverCapabilities: { [index: string]: string };
    clientRegistrationStatus: Status;
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