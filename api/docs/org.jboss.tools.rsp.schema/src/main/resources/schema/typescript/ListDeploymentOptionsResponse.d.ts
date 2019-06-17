export interface ListDeploymentOptionsResponse {
    attributes: Attributes;
    status: Status;
}

export interface Attributes {
    attributes: { [index: string]: Attribute };
}

export interface Status {
    severity: number;
    plugin: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
}

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
    secret: boolean;
}