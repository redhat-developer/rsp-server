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