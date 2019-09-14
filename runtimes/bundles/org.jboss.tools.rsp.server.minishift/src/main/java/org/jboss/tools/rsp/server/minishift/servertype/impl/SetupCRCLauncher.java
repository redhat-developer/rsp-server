package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.server.minishift.servertype.AbstractLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class SetupCRCLauncher extends AbstractLauncher {

	public SetupCRCLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}
	
	@Override
	public String getProgramArguments() {
		String args = "setup ";		
		return args;
	}	

}
