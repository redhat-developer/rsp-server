/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.publishing.IJBossPublishController;
import org.jboss.tools.rsp.server.wildfly.servertype.publishing.WildFlyPublishController;

public class WildFlyServerDelegate extends AbstractJBossServerDelegate {
	public WildFlyServerDelegate(IServer server) {
		super(server);
		setServerState(ServerManagementAPIConstants.STATE_STOPPED);
	}
	protected IServerStartLauncher getStartLauncher() {
		return new WildFlyStartLauncher(this);
	}
	
	protected IServerShutdownLauncher getStopLauncher() {
		return new WildFlyStopLauncher(this);
	}
	@Override
	protected IJBossPublishController createPublishController() {
		return new WildFlyPublishController(getServer(), this);
	}
	
	@Override
	public void updateServer(IServer dummyServer, UpdateServerResponse resp) {
		updateServer(dummyServer, resp, 
				new String[] {ServerManagementAPIConstants.SERVER_HOME_DIR});
	}
	@Override
	protected CreateServerValidation validate(IServer server) {
		CreateServerValidation vd = super.validate(server);
		if( !vd.getStatus().isOK()) {
			return vd;
		}
		String home = server.getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		String configFile = server.getAttribute(IJBossServerAttributes.WILDFLY_CONFIG_FILE, 
				IJBossServerAttributes.WILDFLY_CONFIG_FILE_DEFAULT);
		IPath configFilePath = new Path(home).append("standalone").append("configuration").append(configFile);
		if( !configFilePath.toFile().exists()) {
			return validationErrorResponse("Configuration file must exist", IJBossServerAttributes.WILDFLY_CONFIG_FILE, Activator.BUNDLE_ID);
		}
		return new CreateServerValidation(Status.OK_STATUS, null);
	}
}
