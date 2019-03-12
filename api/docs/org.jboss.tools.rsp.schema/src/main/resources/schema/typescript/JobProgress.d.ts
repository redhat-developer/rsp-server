export interface JobProgress {
    pctg: number;
    handle: JobHandle;
}

export interface JobHandle {
    name: string;
    id: string;
}