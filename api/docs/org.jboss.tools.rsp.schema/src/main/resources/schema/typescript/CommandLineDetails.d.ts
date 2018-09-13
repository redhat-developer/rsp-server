export interface CommandLineDetails {
    cmdLine: string[];
    workingDir: string;
    envp: string[];
    properties: { [index: string]: string };
}