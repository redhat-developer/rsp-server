/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal;

import org.jboss.tools.rsp.eclipse.debug.core.IStreamListener;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.debug.core.model.IStreamMonitor;
import org.jboss.tools.rsp.server.model.ServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class ServerStreamListener implements IStreamListener {
	private IServer server;
	private IProcess process;
	private int streamType;
	private String processId;
	public ServerStreamListener(IServer server, IProcess process, 
			String processId, int type) {
		this.server = server;
		this.process = process;
		this.streamType = type;
		this.processId = processId;
	}
	@Override
	public void streamAppended(String text, IStreamMonitor monitor) {
		fireStreamAppended(server, process, streamType, text);
	}
	private void fireStreamAppended(IServer server2, IProcess process, int streamType, String text) {
		ServerManagementModel.getDefault().getServerModel().fireServerStreamAppended(
				server2, processId, streamType, text);
	}
	
}