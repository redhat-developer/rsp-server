export interface ServerActionWorkflow {
    actionId: string;
    actionLabel: string;
    actionWorkflow: WorkflowResponse;
}

export interface WorkflowResponse {
    status: Status;
    requestId: number;
    jobId: string;
    items: WorkflowResponseItem[];
    invalidFields: string[];
}

export interface Status {
    severity: number;
    plugin: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
}

export interface WorkflowResponseItem {
    id: string;
    itemType: string;
    label: string;
    content: string;
    prompt: WorkflowPromptDetails;
    properties: { [index: string]: string };
}

export interface WorkflowPromptDetails {
    responseType: string;
    responseSecret: boolean;
    validResponses: string[];
}