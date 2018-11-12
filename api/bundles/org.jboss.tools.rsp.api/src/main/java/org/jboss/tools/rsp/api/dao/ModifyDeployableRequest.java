/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class ModifyDeployableRequest {
	private ServerHandle server;
	private DeployableReference deployable;
	
	public ModifyDeployableRequest() {
		
	}
	public ModifyDeployableRequest(ServerHandle server, DeployableReference deployable) {
		this.server = server;
		this.deployable = deployable;
	}
	public ServerHandle getServer() {
		return server;
	}
	public void setServer(ServerHandle server) {
		this.server = server;
	}
	public DeployableReference getDeployable() {
		return deployable;
	}
	public void setDeployable(DeployableReference deployable) {
		this.deployable = deployable;
	}
}
