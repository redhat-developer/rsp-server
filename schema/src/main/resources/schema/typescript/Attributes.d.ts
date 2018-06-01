export interface Attributes {
    attributes: { [index: string]: Attribute };
}

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
}