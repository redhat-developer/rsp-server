/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.publishing.IFullPublishRequiredCallback;
import org.jboss.tools.rsp.server.spi.publishing.IPublishController;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.jboss.tools.rsp.server.wildfly.servertype.actions.EditServerConfigurationActionHandler;
import org.jboss.tools.rsp.server.wildfly.servertype.actions.ShowInBrowserActionHandler;
import org.jboss.tools.rsp.server.wildfly.servertype.publishing.WildFlyPublishController;

public class WildFlyServerDelegate extends AbstractJBossServerDelegate {
	private IFullPublishRequiredCallback fullPublishCallback;
	public WildFlyServerDelegate(IServer server) {
		super(server);
		setServerState(ServerManagementAPIConstants.STATE_STOPPED);
	}

	@Override
	protected IFullPublishRequiredCallback getFullPublishRequiredCallback() {
		if(fullPublishCallback == null ) {
			return new WildFlyFullPublishRequiredCallback(this);
		}
		return fullPublishCallback;
	}

	
	@Override
	protected IServerStartLauncher getStartLauncher(IServer server) {
		return new WildFlyStartLauncher(server.getDelegate());
	}
	
	@Override
	protected IServerShutdownLauncher getStopLauncher() {
		return new WildFlyStopLauncher(this);
	}
	@Override
	protected IPublishController createPublishController() {
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
		IPath homePath = new Path(home);
		if( !homePath.toFile().exists()) {
			return validationErrorResponse("Server's Home Directory must exist", IJBossServerAttributes.SERVER_HOME, Activator.BUNDLE_ID);
		}

		String baseDir = server.getAttribute(IJBossServerAttributes.SERVER_BASE_DIR, IJBossServerAttributes.SERVER_BASE_DIR_DEFAULT);
		baseDir = baseDir == null || baseDir.trim().isEmpty() ? IJBossServerAttributes.SERVER_BASE_DIR_DEFAULT : baseDir;
		IPath baseDirPath = !isRelativePath(baseDir) ? new Path(baseDir) : homePath.append(baseDir);
		if( !baseDirPath.toFile().exists()) {
			return validationErrorResponse("Server's Base Directory must exist: " + baseDirPath.toString(), IJBossServerAttributes.SERVER_BASE_DIR, Activator.BUNDLE_ID);
		}

		String configFile = server.getAttribute(IJBossServerAttributes.WILDFLY_CONFIG_FILE, 
				IJBossServerAttributes.WILDFLY_CONFIG_FILE_DEFAULT);
		IPath configFilePath = baseDirPath.append("configuration").append(configFile);
		if( !configFilePath.toFile().exists()) {
			return validationErrorResponse("Configuration file must exist: " + configFilePath, IJBossServerAttributes.WILDFLY_CONFIG_FILE, Activator.BUNDLE_ID);
		}
		return new CreateServerValidation(Status.OK_STATUS, null);
	}
	
	public static boolean isRelativePath(String s) {
		Path p = new org.jboss.tools.rsp.eclipse.core.runtime.Path(s);
		return !p.isAbsolute();
	}
	
	@Override
	public ListServerActionResponse listServerActions() {
		ListServerActionResponse ret = new ListServerActionResponse();
		ret.setStatus(StatusConverter.convert(Status.OK_STATUS));
		List<ServerActionWorkflow> allActions = new ArrayList<>();
		if( getServerState().getState() == ServerManagementAPIConstants.STATE_STARTED)
			allActions.add(new ShowInBrowserActionHandler(this).getInitialWorkflow());
		allActions.add(EditServerConfigurationActionHandler.getInitialWorkflow(this));
		ret.setWorkflows(allActions);
		return ret;
	}
	
	@Override
	public WorkflowResponse executeServerAction(ServerActionRequest req) {
		if( ShowInBrowserActionHandler.ACTION_SHOW_IN_BROWSER_ID.equals(req.getActionId() )) {
			return new ShowInBrowserActionHandler(this).handle(req);
		}
		if( EditServerConfigurationActionHandler.ACTION_ID.equals(req.getActionId() )) {
			return new EditServerConfigurationActionHandler(this).handle(req);
		}
		return cancelWorkflowResponse();
	}
	
}
