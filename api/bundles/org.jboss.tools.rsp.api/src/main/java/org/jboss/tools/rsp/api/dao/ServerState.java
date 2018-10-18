/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.List;

public class ServerState {
	private ServerHandle server;
	private int state;
	private int publishState;
	private List<DeployableState> moduleState;
	public ServerState() {
		
	}
	public ServerHandle getServer() {
		return server;
	}
	public void setServer(ServerHandle server) {
		this.server = server;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getPublishState() {
		return publishState;
	}
	public void setPublishState(int publishState) {
		this.publishState = publishState;
	}
	public List<DeployableState> getModuleState() {
		return moduleState;
	}
	public void setModuleState(List<DeployableState> moduleState) {
		this.moduleState = moduleState;
	}
}
