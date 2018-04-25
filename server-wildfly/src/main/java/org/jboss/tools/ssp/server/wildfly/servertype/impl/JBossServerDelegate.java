package org.jboss.tools.ssp.server.wildfly.servertype.impl;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.ssp.launching.VMInstallModel;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;

public class JBossServerDelegate implements IServerDelegate {
	private IServer server;
	public JBossServerDelegate(IServer server) {
		this.server = server;
	}

	@Override
	public IStatus validate() {
		String home = server.getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		
		if( null == home ) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "Server home must not be null");
		}
		if(!(new File(home).exists())) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "Server home must exist");
		}
		
		String vmId = server.getAttribute(IJBossServerAttributes.VM_INSTALL_ID, (String)null);
		if( vmId == null ) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "VM id must not be null");
		}
		IVMInstall vmi = VMInstallModel.getDefault().findVMInstall(vmId);
		if( vmi == null ) {
			return new Status(IStatus.ERROR, "org.jboss.tools.ssp.server.wildfly", "VM " + vmId + " is not found in the VM model");
		}
		return Status.OK_STATUS;
	}

}
