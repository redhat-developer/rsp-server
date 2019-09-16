package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.foundation.core.launchers.CommandTimeoutException;
import org.jboss.tools.rsp.foundation.core.launchers.ProcessUtility;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.spi.model.polling.AbstractPoller;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class CRCStatusPoller extends AbstractPoller implements IServerStatePoller{
	
	public String getMinishiftCommand(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, (String) null);
	}
	public String getWorkingDirectory(IServer server) {
		return new Path(getMinishiftCommand(server)).removeLastSegments(1).toOSString();
	}
	
	private String[] callCRCStatus(IServer server) throws CommandTimeoutException, IOException {		
		String[] args = new String[] { "status" };
		String cmd = getMinishiftCommand(server);
		ProcessUtility util = new ProcessUtility();
		String[] lines = util.callMachineReadable(
				cmd, args, getWorkingDirectory(server), 
				new EnvironmentUtility(server).getEnvironment(true, true));
		return lines;
	}
	
	protected IStatus parseOutput(String[] lines) {
		for (int i = 0; i < lines.length;) {
			if (lines[i] != null && lines[i].toLowerCase().contains("running")) {
				return Status.OK_STATUS;
			} else {
				return new Status(IStatus.ERROR, Activator.BUNDLE_ID, 
						"CRC is stopped.");
			}
		}
		return new Status(IStatus.INFO, Activator.BUNDLE_ID, 
				"CRC status indicates the cluster is starting.");
	}

	@Override
	protected SERVER_STATE onePing(IServer server) {
		String[] lines = null;
		try {
			lines = callCRCStatus(server);
			IStatus stat = parseOutput(lines);
			if (stat.isOK()) {
				//checkOpenShiftHealth(server, 4000); TODO 
				return SERVER_STATE.UP;
			}
			return SERVER_STATE.DOWN;
		} catch (TimeoutException te) {
			cancel(IServerStatePoller.CANCELATION_CAUSE.TIMEOUT_REACHED);
			return SERVER_STATE.DOWN;
		} catch (IOException ioe) {
			cancel(IServerStatePoller.CANCELATION_CAUSE.FAILED);
			return SERVER_STATE.DOWN;
		}
	}

	@Override
	protected String getThreadName() {
		return "CRC Poller: " + getServer().getName();
	}

}
