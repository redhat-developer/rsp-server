package org.jboss.tools.rsp.server.spi.launchers;

import java.io.IOException;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
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
	
	public CommandLineDetails getCommandLineDetails(ILaunch launch,
			NullProgressMonitor nullProgressMonitor) {
		return getTemporaryDetails().toDetails();
	}
	
	protected CommandConfig getTemporaryDetails() {
		return details;
	}

	public void run(ILaunch launch, NullProgressMonitor nullProgressMonitor) throws CoreException {
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
