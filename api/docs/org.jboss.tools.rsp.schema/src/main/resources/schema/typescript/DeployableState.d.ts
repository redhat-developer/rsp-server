export interface DeployableState {
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface DeployableReference {
    id: string;
    path: string;
}