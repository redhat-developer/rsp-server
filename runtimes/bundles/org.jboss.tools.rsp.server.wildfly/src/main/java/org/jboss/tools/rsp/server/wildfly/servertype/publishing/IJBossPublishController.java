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
