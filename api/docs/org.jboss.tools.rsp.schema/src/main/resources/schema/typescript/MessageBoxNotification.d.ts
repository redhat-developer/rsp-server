export interface MessageBoxNotification {
    code: number;
    severity: number;
    message: string;
    properties: { [index: string]: any };
}