package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.ssp.launching.LaunchingCore;
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
				for( int i = 0; i < processes.length; i++ ) {
					try {
						processes[i].terminate();
					} catch(DebugException de) {
						LaunchingCore.log(de);
					}
				}
			}
			setServerState(IServerDelegate.STATE_STOPPED);
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus stop(boolean force) {
		setServerState(IServerDelegate.STATE_STOPPING);
		ILaunch stopLaunch = null;
		try {
			stopLaunch = new JBossStopLauncher(this).launch(force);
			// TODO launch poller
			IProcess p = launch.getProcesses()[0];
			p.getStreamsProxy().getOutputStreamMonitor().addListener(new IStreamListener() {
				@Override
				public void streamAppended(String text, IStreamMonitor monitor) {
					System.out.println(text);
				}
			});
			p.getStreamsProxy().getErrorStreamMonitor().addListener(new IStreamListener() {
				@Override
				public void streamAppended(String text, IStreamMonitor monitor) {
					System.out.println(text);
				}
			});
			
			setServerState(IServerDelegate.STATE_STOPPED);
		} catch(CoreException ce) {
			if( stopLaunch != null ) {
				IProcess[] processes = launch.getProcesses();
				for( int i = 0; i < processes.length; i++ ) {
					try {
						processes[i].terminate();
					} catch(DebugException de) {
						LaunchingCore.log(de);
					}
				}
			}
			setServerState(IServerDelegate.STATE_STARTED);
			return ce.getStatus();
		}
		return Status.OK_STATUS;

	}
	
}
