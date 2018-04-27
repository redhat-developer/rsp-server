package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.ssp.launching.VMInstallModel;
import org.jboss.tools.ssp.server.model.internal.AbstractServerDelegate;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;

public class JBossServerDelegate extends AbstractServerDelegate {
	private ILaunch launch;
	
	public JBossServerDelegate(IServer server) {
		super(server);
	}

	@Override
	public IStatus validate() {
		String home = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		
		if( null == home ) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "Server home must not be null");
		}
		if(!(new File(home).exists())) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "Server home must exist");
		}
		
		String vmId = getServer().getAttribute(IJBossServerAttributes.VM_INSTALL_ID, (String)null);
		if( vmId == null ) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "VM id must not be null");
		}
		IVMInstall vmi = VMInstallModel.getDefault().findVMInstall(vmId);
		if( vmi == null ) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "VM " + vmId + " is not found in the VM model");
		}
		return Status.OK_STATUS;
	}

	
	public IStatus canStart(String launchMode) {
		if( !"run".equals(launchMode)) {
			return new Status(IStatus.ERROR, 
					"org.jboss.tools.ssp.server.wildfly", 
					"Server must be launched in run mode only.");
		}
		if( getServerState() == IServerDelegate.STATE_STOPPED ) {
			IStatus v = validate();
			if( !v.isOK() )
				return v;
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}
	
	@Override
	public IStatus start(String mode) {
		setServerState(IServerDelegate.STATE_STARTING);
		
		try {
			launch = new JBossStartLauncher(this).launch(mode);
			// TODO fire poller or similar
			setServerState(IServerDelegate.STATE_STARTED);
		} catch(CoreException ce) {
			if( launch != null ) {
				IProcess[] processes = launch.getProcesses();
				// TODO terminate them all!
			}
			setServerState(IServerDelegate.STATE_STOPPED);
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}
	
}
