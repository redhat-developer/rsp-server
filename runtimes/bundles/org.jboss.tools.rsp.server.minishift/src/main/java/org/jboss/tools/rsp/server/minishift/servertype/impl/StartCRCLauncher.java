/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.dao.StringPrompt;
import org.jboss.tools.rsp.secure.crypto.CryptoException;
import org.jboss.tools.rsp.server.minishift.servertype.AbstractLauncher;
import org.jboss.tools.rsp.server.minishift.servertype.MinishiftPropertyUtility;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class StartCRCLauncher extends AbstractLauncher {
	
	private CRCServerDelegate crcServerDelegate;

	public StartCRCLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
		this.crcServerDelegate = (CRCServerDelegate) jBossServerDelegate;
	}

	@Override
	public String getProgramArguments() {
		String vmDriver = MinishiftPropertyUtility.getMinishiftVMDriver(getServer());
		String vmd = isEmpty(vmDriver) ? "" : "--vm-driver=" + vmDriver;
		
		String args = getCRCArguments();
		args = isEmpty(args) ? "" : " " + args;
				
		return "start " + vmd + args;
	}
	protected String getAppendedArguments() {
		String append = getServer().getAttribute(
				MinishiftServerDelegate.STARTUP_PROGRAM_ARGS_STRING, (String)null);
		return append == null ? "" : append;
	}
	
	protected String getCRCArguments() {
		
		String pullSecret = MinishiftPropertyUtility.getMinishiftImagePullSecret(crcServerDelegate);		
		
		String args = "";
		if( !isEmpty(pullSecret)) {
			args = " --pull-secret-file '" + pullSecret + "'";
		}
		return args;
	}
	
	private boolean isEmpty(String s) {
		return s == null ? true : s.isEmpty();
	}
}
