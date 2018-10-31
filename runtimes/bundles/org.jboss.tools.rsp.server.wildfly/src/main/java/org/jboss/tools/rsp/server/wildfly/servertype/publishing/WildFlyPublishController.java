package org.jboss.tools.rsp.server.wildfly.servertype.publishing;

import java.io.File;
import java.nio.file.Path;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WildFlyPublishController extends StandardJBossPublishController implements IJBossPublishController {

	private static final Logger LOG = LoggerFactory.getLogger(WildFlyPublishController.class);
	
	public WildFlyPublishController(IServer server, AbstractJBossServerDelegate delegate) {
		super(server, delegate);
	}
	
	@Override
	protected Path getDeploymentFolder() {
		// TODO this may need to be abstracted out eventually if we 
		// support things like custom config folders etc. 
		String home = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		Path p = new File(home).toPath().resolve("standalone").resolve("deployments");
		return p;
	}
	
	@Override
	public int publishModule(DeployableReference reference, int publishType, int modulePublishType)
			throws CoreException {
		// TODO since we're only using zips, right now, we don't need to fiddle with markers
		// This will change very soon, and thats why these methods are overridden here
		
		if( modulePublishType == ServerManagementAPIConstants.PUBLISH_STATE_REMOVE) {
			int ret = removeModule(reference, publishType, modulePublishType);
			return ret;
		} else {
			int ret = copyModule(reference, publishType, modulePublishType);
			return ret;
		}
	}
	

	@Override
	public void publishFinish(int publishType) throws CoreException {
		// Add the markers where appropriate
	}
}
