export interface WorkflowResponse {
    status: Status;
    requestId: number;
    items: WorkflowResponseItem[];
}

export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}

export interface WorkflowResponseItem {
    id: string;
    itemType: string;
    label: string;
    content: string;
    responseType: string;
    responseSecret: boolean;
    validResponses: string[];
}