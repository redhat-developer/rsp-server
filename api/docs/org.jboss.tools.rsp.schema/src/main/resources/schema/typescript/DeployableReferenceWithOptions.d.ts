export interface DeployableReferenceWithOptions {
    reference: DeployableReference;
    options?: { [index: string]: any };
}

export interface DeployableReference {
    label: string;
    path: string;
}