/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.Map;

public class CommandLineDetails {
	private String[] cmdLine;
	private String workingDir;
	private String[] envp;
	private Map<String,String> properties;
	
	public CommandLineDetails() {
		
	}

	
	public CommandLineDetails(String[] cmdLine, String workingDir, String[] envp,
			Map<String,String> properties) {
		this.cmdLine = cmdLine;
		this.workingDir = workingDir;
		this.envp = envp;
		this.properties = properties;
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

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}