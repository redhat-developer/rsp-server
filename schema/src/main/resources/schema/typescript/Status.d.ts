export interface Status {
    severity: number;
    code: number;
    message: string;
    trace: string;
    plugin: string;
    ok: boolean;
}