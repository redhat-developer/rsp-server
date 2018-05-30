package org.jboss.tools.ssp.api.dao;

public class CommandLineDetails {
	private String[] cmdLine;
	private String workingDir;
	private String[] envp;

	public CommandLineDetails(String[] cmdLine, String workingDir, String[] envp) {
		this.cmdLine = cmdLine;
		this.workingDir = workingDir;
		this.envp = envp;
	}

	public String[] getCmdLine() {
		return cmdLine;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public String[] getEnvp() {
		return envp;
	}
}