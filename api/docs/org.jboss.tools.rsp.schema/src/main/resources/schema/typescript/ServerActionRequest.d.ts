export interface ServerActionRequest {
    requestId: number;
    actionId: string;
    serverId: string;
    data: { [index: string]: any };
}