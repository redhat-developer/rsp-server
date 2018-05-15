/* tslint:disable */
// Generated using typescript-generator version 2.2.413 on 2018-05-15 10:25:48.

export interface ServerProcessOutput {
    server: ServerHandle;
    processId: string;
    streamType: number;
    text: string;
}

export interface ServerHandle {
    id: string;
    type: string;
}
