export interface DeployableState {
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface DeployableReference {
    label: string;
    path: string;
}