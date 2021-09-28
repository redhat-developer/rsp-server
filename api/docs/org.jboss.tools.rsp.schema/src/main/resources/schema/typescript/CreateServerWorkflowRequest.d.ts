export interface CreateServerWorkflowRequest {
    requestId: number;
    serverTypeId: string;
    data: { [index: string]: any };
}