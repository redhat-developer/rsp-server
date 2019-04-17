/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import org.jboss.tools.rsp.api.dao.util.EqualsUtility;

public class ServerDeployableReference {

	private ServerHandle server;
	private DeployableReference deployableReference;

	public ServerDeployableReference() {
	}

	public ServerDeployableReference(ServerHandle server2, DeployableReference ref) {
		this.server = server2;
		this.deployableReference = ref;
	}

	public ServerHandle getServer() {
		return server;
	}

	public void setServer(ServerHandle server) {
		this.server = server;
	}

	public DeployableReference getDeployableReference() {
		return deployableReference;
	}

	public void setDeployableReference(DeployableReference deployableReference) {
		this.deployableReference = deployableReference;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		result = prime * result + ((deployableReference == null) ? 0 : deployableReference.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerDeployableReference other = (ServerDeployableReference) obj;
		return EqualsUtility.areEqual(server, other.server)
				&& EqualsUtility.areEqual(deployableReference, other.deployableReference);
	}

}
