/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class ModifyDeployableRequest {
	private ServerHandle server;
	private DeployableReferenceWithOptions deployable;

	public ModifyDeployableRequest() {

	}

	public ModifyDeployableRequest(ServerHandle server, DeployableReferenceWithOptions deployable) {
		this.server = server;
		this.deployable = deployable;
	}

	public ServerHandle getServer() {
		return server;
	}

	public void setServer(ServerHandle server) {
		this.server = server;
	}

	public DeployableReferenceWithOptions getDeployable() {
		return deployable;
	}

	public void setDeployable(DeployableReferenceWithOptions deployable) {
		this.deployable = deployable;
	}
}
