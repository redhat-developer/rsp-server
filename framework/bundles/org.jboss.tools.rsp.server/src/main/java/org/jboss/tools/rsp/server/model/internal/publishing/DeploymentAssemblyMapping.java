/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal.publishing;

import org.jboss.tools.rsp.server.spi.servertype.IDeploymentAssemblyMapping;

public class DeploymentAssemblyMapping implements IDeploymentAssemblyMapping {
	private String deployPath;
	private String source;

	public DeploymentAssemblyMapping(String source, String deployPath) {
		this.source = source;
		this.deployPath = deployPath;
	}

	public String getSource() {
		return source;
	}

	public String getDeployPath() {
		return deployPath;
	}
}
