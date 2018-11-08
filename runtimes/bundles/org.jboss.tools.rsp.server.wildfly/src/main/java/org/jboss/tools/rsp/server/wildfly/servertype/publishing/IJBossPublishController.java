/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.publishing;

import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;

public interface IJBossPublishController {

	public IStatus canAddDeployable(DeployableReference reference);
	
	public IStatus canRemoveDeployable(DeployableReference reference);
	
	public IStatus canPublish();
	
	public void publishStart(int publishType) throws CoreException;

	public void publishFinish(int publishType) throws CoreException;

	public int publishModule(DeployableReference reference, int publishType, int modulePublishType) throws CoreException;
}
