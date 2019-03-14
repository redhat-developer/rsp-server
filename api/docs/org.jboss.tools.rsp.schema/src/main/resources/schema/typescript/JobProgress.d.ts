export interface JobProgress {
    percent: number;
    handle: JobHandle;
}

export interface JobHandle {
    name: string;
    id: string;
}