export interface StartServerResponse {
    status: Status;
    details: CommandLineDetails;
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

export interface CommandLineDetails {
    cmdLine: string[];
    workingDir: string;
    envp: string[];
    properties: { [index: string]: string };
}