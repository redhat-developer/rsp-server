export interface WorkflowResponseItem {
    id: string;
    itemType: string;
    label: string;
    content: string;
    responseType: string;
    responseSecret: boolean;
    validResponses: string[];
}