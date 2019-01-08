export interface DownloadSingleRuntimeRequest {
    requestId: number;
    downloadRuntimeId: string;
    data: { [index: string]: any };
}