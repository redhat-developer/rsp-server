package org.jboss.tools.rsp.server.wildfly.servertype.publishing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.AbstractJBossServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardJBossPublishController implements IJBossPublishController {

	private static final Logger LOG = LoggerFactory.getLogger(StandardJBossPublishController.class);
	
	
	private static final String[] supportedSuffix = new String[] {
		".jar", ".war", ".ear", ".rar", ".xml"
	};
	
	private IServer server;
	private AbstractJBossServerDelegate delegate;
	public StandardJBossPublishController(IServer server, AbstractJBossServerDelegate delegate) {
		this.server = server;
		this.delegate = delegate;
	}
	
	protected IServer getServer() {
		return server;
	}
	
	protected AbstractJBossServerDelegate getDelegate() {
		return delegate;
	}
	
	private boolean hasSuffix(String path, String suffix) {
		return path.endsWith(suffix);
	}
	private boolean isSupportedDeployable(String path, boolean mustExist) {
		if( path == null )
			return false;
		
		File f = new File(path);
		// When removing a module, there's no reason it must exist
		if( mustExist ) {
			if( !f.exists() || !f.isFile()) 
				return false;
		}
		
		for( int i = 0; i < supportedSuffix.length; i++ ) {
			if( hasSuffix(path, supportedSuffix[i]))
				return true;
		}
		return false;
	}
	
	
	@Override
	public IStatus canAddDeployable(DeployableReference reference) {
		if( isSupportedDeployable(reference.getPath(), true)) {
			return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, Activator.BUNDLE_ID, 
				NLS.bind("Server {0} cannot add deployable from {1}", server.getName(), reference.getPath()));
	}

	@Override
	public IStatus canRemoveDeployable(DeployableReference reference) {
		if( isSupportedDeployable(reference.getPath(), false)) {
			return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, Activator.BUNDLE_ID, 
				NLS.bind("Server {0} cannot remove deployable from {1}", server.getName(), reference.getPath()));
	}

	@Override
	public IStatus canPublish() {
		return Status.OK_STATUS;
	}

	@Override
	public void publishStart(int publishType) throws CoreException {
		// We use a filesystem based publishing, so we have no tasks here
	}

	@Override
	public void publishFinish(int publishType) throws CoreException {
		// We use a filesystem based publishing, so we have no tasks here
	}

	@Override
	public int publishModule(DeployableReference reference, int publishType, int modulePublishType)
			throws CoreException {
		if( modulePublishType == ServerManagementAPIConstants.PUBLISH_STATE_REMOVE) {
			return removeModule(reference, publishType, modulePublishType);
		} else {
			return copyModule(reference, publishType, modulePublishType);
		}
	}

	protected Path getDeploymentFolder() {
		// TODO this may need to be abstracted out eventually if we 
		// support things like custom config folders etc. 
		String home = getServer().getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		Path p = new File(home).toPath().resolve("server").resolve("default").resolve("deploy");
		return p;
	}

	protected Path getDestinationPath(DeployableReference reference) {
		File src = new File(reference.getPath());
		String srcName = src.getName();
		File dest = getDeploymentFolder().resolve(srcName).toFile();
		return dest.toPath();
	}

		
	
	protected int copyModule(DeployableReference reference, int publishType, int modulePublishType) throws CoreException {
		File dest = getDestinationPath(reference).toFile();
		try {
			Files.copy(new File(reference.getPath()).toPath(), dest.toPath());
			return ServerManagementAPIConstants.PUBLISH_STATE_NONE;
		} catch(IOException ioe) {
			LOG.error("Error publishing module {0} to server {1}", ioe);
			return delegate.getServerPublishModel().getDeployableState(reference).getPublishState();
		}
	}


	protected int removeModule(DeployableReference reference, int publishType, int modulePublishType) throws CoreException {
		File dest = getDestinationPath(reference).toFile();
		if( dest != null && dest.exists()) {
			if( !dest.delete() )
				return delegate.getServerPublishModel().getDeployableState(reference).getPublishState();
		}
		return ServerManagementAPIConstants.PUBLISH_STATE_NONE;
	}
	
}
