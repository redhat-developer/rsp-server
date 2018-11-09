/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.launchers;

import java.io.IOException;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.server.spi.SPIActivator;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class GenericProcessRunner {

	private IServerDelegate serverDel;
	private CommandConfig details;

	public GenericProcessRunner(IServerDelegate serverDel, CommandConfig config) {
		this.serverDel = serverDel;
		this.details = config;
	}
	
	public IServer getServer() {
		return serverDel.getServer();
	}
	
	public CommandLineDetails getCommandLineDetails(ILaunch launch, IProgressMonitor monitor) {
		return getTemporaryDetails().toDetails();
	}
	
	protected CommandConfig getTemporaryDetails() {
		return details;
	}

	public void run(ILaunch launch, IProgressMonitor monitor) throws CoreException {
		CommandConfig det = getTemporaryDetails();
		ProcessUtility util = new ProcessUtility();
		try {
			Process p = util.callProcess(det.getCommand(), det.getParsedArgs(), det.getWorkingDir(), det.getEnvironment());
			IProcess process = util.createIProcess(launch, p, det.toDetails());
			launch.addProcess(process);
		} catch(IOException ioe) {
			abort("Failed to launch process", ioe, 0);
		}
	}

	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, SPIActivator.BUNDLE_ID, code, message, exception));
	}
	
}
