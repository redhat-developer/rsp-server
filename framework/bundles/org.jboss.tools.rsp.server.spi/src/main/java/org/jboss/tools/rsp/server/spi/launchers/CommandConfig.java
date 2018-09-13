package org.jboss.tools.rsp.server.spi.launchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;

public class CommandConfig {
	/**
	 * 
	 */
	private String command;
	private String workingDir;
	private String[] parsedArgs;
	private String[] environment;
	
	public CommandConfig(String cmd, String wd, String[] parsed, String[] env) {
		this.command = cmd;
		this.workingDir = wd;
		this.parsedArgs = parsed;
		this.environment = env;
	}

	public String getCommand() {
		return command;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public String[] getParsedArgs() {
		return parsedArgs;
	}

	public String[] getEnvironment() {
		return environment;
	}
	public CommandLineDetails toDetails() {
		List<String> arguments = new ArrayList<String>();
		arguments.add(command);
		arguments.addAll(Arrays.asList(parsedArgs));
		String[] argFinal = (String[]) arguments.toArray(new String[arguments.size()]);
		return new CommandLineDetails(argFinal, workingDir,
				environment,new HashMap<String,String>()); 
	}
}