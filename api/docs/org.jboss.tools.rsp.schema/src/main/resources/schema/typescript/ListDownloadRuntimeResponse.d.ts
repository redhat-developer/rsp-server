export interface ListDownloadRuntimeResponse {
    runtimes: DownloadRuntimeDescription[];
}

export interface DownloadRuntimeDescription {
    name: string;
    id: string;
    version: string;
    url: string;
    licenseURL: string;
    humanUrl: string;
    disclaimer: boolean;
    properties: { [index: string]: string };
    size: string;
    installationMethod: string;
}