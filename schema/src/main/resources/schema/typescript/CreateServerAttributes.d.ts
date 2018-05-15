export interface CreateServerAttributes {
    attributes: { [index: string]: CreateServerAttribute };
}

export interface CreateServerAttribute {
    type: string;
    description: string;
    defaultVal: any;
}