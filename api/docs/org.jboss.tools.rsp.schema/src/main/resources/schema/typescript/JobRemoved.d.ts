export interface JobRemoved {
    status: Status;
    handle: JobHandle;
}

export interface Status {
    severity: number;
    plugin: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
}

export interface JobHandle {
    name: string;
    id: string;
}