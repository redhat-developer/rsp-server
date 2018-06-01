export interface Status {
    severity: number;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}