/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.publishing;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableResourceDelta;
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
	
	private IStatus validateSupportedDeployable(String path, String outputName, boolean mustExist) {
		if( path == null )
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, 
					NLS.bind("Server {0} does not support deployment with null path.", server.getId()));
		
		if( outputName == null ) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, 
					NLS.bind("Unable to discover preferred output name for deployment to server {0}", server.getId()));
		}
		
		File f = new File(path);
		// When removing a module, there's no reason it must exist
		if( mustExist && !f.exists())
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, 
					NLS.bind("Server {0} does not support deployments that do not exist in the filesystem.", server.getId()));

		if( mustExist && !supportsExplodedDeployment() && !f.isFile())
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, 
					NLS.bind("Server {0} does not support exploded deployment, and deployment path is a directory.", server.getId()));
		
		for( int i = 0; i < supportedSuffix.length; i++ ) {
			if( hasSuffix(outputName, supportedSuffix[i]))
				return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, Activator.BUNDLE_ID, NLS.bind(
				"Server {0} does not support deployment of resources without an approved suffix. " + 
				"Use the {1} deployment option to override the output name.", 
				server.getName(), ServerManagementAPIConstants.DEPLOYMENT_OPTION_OUTPUT_NAME));
	}
	
	protected boolean supportsExplodedDeployment() {
		return true;
	}
	
	protected String getOutputName(DeployableReference ref) {
		Map<String, Object> options = ref.getOptions();
		String def = null;
		if( ref.getPath() != null ) {
			def = new File(ref.getPath()).getName();
		}
		String k = ServerManagementAPIConstants.DEPLOYMENT_OPTION_OUTPUT_NAME; 
		if( options != null && options.get(k) != null ) {
			return (String)options.get(k);
		}
		return def;
	}
	
	@Override
	public IStatus canAddDeployable(DeployableReference ref) {
		String path = ref.getPath();
		IStatus valid = validateSupportedDeployable(path, getOutputName(ref), false);
		if(valid.isOK()) {
			return Status.OK_STATUS;
		}
		return valid;
	}

	@Override
	public IStatus canRemoveDeployable(DeployableReference ref) {
		String path = ref.getPath();
		IStatus valid = validateSupportedDeployable(path, getOutputName(ref), false);
		if(valid.isOK()) {
			return Status.OK_STATUS;
		}
		return valid;
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
	public int publishModule(DeployableReference reference, 
			int publishRequestType, int modulePublishState) throws CoreException {
		if( modulePublishState == ServerManagementAPIConstants.PUBLISH_STATE_REMOVE) {
			// Removal is always complete. No incrementals ;) 
			return removeModule(reference, publishRequestType, modulePublishState);
		} else {
			return copyModule(reference, publishRequestType, modulePublishState);
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
		String outName = src.getName();
		if( reference.getOptions() != null ) {
			String optOutputName = (String)reference.getOptions().get(ServerManagementAPIConstants.DEPLOYMENT_OPTION_OUTPUT_NAME);
			if( optOutputName != null ) {
				outName = optOutputName;
			}
		}
		File dest = getDeploymentFolder().resolve(outName).toFile();
		return dest.toPath();
	}
		
	protected int copyModule(DeployableReference opts, 
			int serverPublishRequest, int modulePublishState) throws CoreException {
		int publishType = getModulePublishType(serverPublishRequest, modulePublishState);
		if( publishType == ServerManagementAPIConstants.PUBLISH_INCREMENTAL) {
			return incrementalPublishCopyModule(opts, serverPublishRequest, modulePublishState);
		} else {
			return fullPublishCopyModule(opts, serverPublishRequest, modulePublishState);
		}
	}
	
	private int incrementalPublishCopyModule(DeployableReference opts, 
			int serverPublishRequest, int modulePublishState) throws CoreException {
		IDeployableResourceDelta delta = getDelegate().getServerPublishModel()
				.getDeployableResourceDelta(opts);
		
		File src = new File(opts.getPath());
		if( src.exists() && src.isFile()) {
			return fullPublishCopyZippedModule(opts, serverPublishRequest, modulePublishState);
		}

		if( delta == null )
			return ServerManagementAPIConstants.PUBLISH_STATE_NONE;
		
		if( src.exists() && src.isDirectory()) {
			return incrementalPublishCopyExplodedModule(opts, delta);
		}
		return ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN;
	}

	protected int getModulePublishType(int serverPublishType, int modulePublishState) {
		if( serverPublishType == ServerManagementAPIConstants.PUBLISH_CLEAN)
			return ServerManagementAPIConstants.PUBLISH_FULL;
		if( serverPublishType == ServerManagementAPIConstants.PUBLISH_FULL)
			return ServerManagementAPIConstants.PUBLISH_FULL;
		
		// if it's PUBLISH_AUTO or PUBLISH_INCREMENTAL, then go by the module
		if( modulePublishState == ServerManagementAPIConstants.PUBLISH_STATE_ADD)
			return ServerManagementAPIConstants.PUBLISH_FULL;
		if( modulePublishState == ServerManagementAPIConstants.PUBLISH_STATE_FULL)
			return ServerManagementAPIConstants.PUBLISH_FULL;
		if( modulePublishState == ServerManagementAPIConstants.PUBLISH_STATE_REMOVE)
			return ServerManagementAPIConstants.PUBLISH_FULL;
		if( modulePublishState == ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN)
			return ServerManagementAPIConstants.PUBLISH_FULL;
		
		return ServerManagementAPIConstants.PUBLISH_INCREMENTAL;
	}
	
	protected int fullPublishCopyModule(DeployableReference opts, int publishType, int modulePublishType) throws CoreException {
		File src = new File(opts.getPath());
		if( src.exists() && src.isFile()) {
			return fullPublishCopyZippedModule(opts, publishType, modulePublishType);
		}
		if( src.exists() && src.isDirectory()) {
			return fullPublishCopyExplodedModule(opts, publishType, modulePublishType);
		}
		return ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN;
	}
	
	protected int fullPublishCopyZippedModule(DeployableReference opts, int publishType, int modulePublishType) throws CoreException {
		File dest = getDestinationPath(opts).toFile();
		Path src = new File(opts.getPath()).toPath();
		try {
			Files.copy(src, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return ServerManagementAPIConstants.PUBLISH_STATE_NONE;
		} catch(IOException ioe) {
			LOG.error("Error publishing module {0} to server {1}", ioe);
			return delegate.getServerPublishModel().getDeployableState(opts).getPublishState();
		}
	}

	protected int fullPublishCopyExplodedModule(DeployableReference opts, int publishType, int modulePublishType) throws CoreException {
		File dest = getDestinationPath(opts).toFile();
		Path src = new File(opts.getPath()).toPath();
		try {
			completeDelete(dest.toPath());
			dest.mkdirs();
			Files.walkFileTree(src, new CopyFileVisitor(dest.toPath()));
			return ServerManagementAPIConstants.PUBLISH_STATE_NONE;
		} catch(IOException ioe) {
			LOG.error("Error publishing module {0} to server {1}", ioe);
			return delegate.getServerPublishModel().getDeployableState(opts).getPublishState();
		}
	}
	
	protected int incrementalPublishCopyExplodedModule(DeployableReference opts,
			IDeployableResourceDelta delta) throws CoreException {
		File dest = getDestinationPath(opts).toFile();
		Path src = new File(opts.getPath()).toPath();
		
		List<String> errors = new ArrayList<>();
		Map<Path, Integer> deltaVals = delta.getResourceDeltaMap();
		for( Map.Entry<Path, Integer> entry : deltaVals.entrySet()) {
			int change = entry.getValue();
			Path fileSrc = src.resolve(entry.getKey());
			Path fileDest = dest.toPath().resolve(entry.getKey());

			if( change == IDeployableResourceDelta.DELETED) {
				if( !fileDest.toFile().delete() ) {
					LOG.debug("Error: Cannot delete file " + fileDest.toFile().getAbsolutePath());
				}
			} else if( change == IDeployableResourceDelta.CREATED || 
					change == IDeployableResourceDelta.MODIFIED) {
				incrementalPublishCopySingleFile(fileSrc, fileDest, errors);
			}
		}

		return incrementalExplodedPublishResult(opts, errors);
	}
	
	private void incrementalPublishCopySingleFile(Path fileSrc, Path fileDest, List<String> errors) {
		if( !fileSrc.toFile().exists()) {
			errors.add("Source path does not exist: " + fileSrc.toString());
			return;
		}
		if(fileSrc.toFile().isDirectory()) {
			// destination should be a directory to create
			if( !fileDest.toFile().exists()) {
				boolean result = fileDest.toFile().mkdirs();
				if( !result ) {
					errors.add("Unable to create directory " + fileDest.toString());
				}
			}
		}
		if( fileSrc.toFile().isFile()) {
			try {
				Files.copy(fileSrc, fileDest, StandardCopyOption.REPLACE_EXISTING);
			} catch(IOException ioe) {
				errors.add("Unable to copy " + fileSrc.toString() + " to " + fileDest.toString());
			}
		}
	}

	private int incrementalExplodedPublishResult(DeployableReference opts, List<String> errors) {
		if( !errors.isEmpty() ) {
			String[] arr = errors.toArray(new String[errors.size()]);
			String errorString = String.join("\n", arr);
			LOG.error("Error publishing module {0} to server {1}:\n{2}", opts.getLabel(), getServer().getName(), errorString);
			// TODO maybe throw core exception here?? 
			return delegate.getServerPublishModel().getDeployableState(opts).getPublishState();
		}
		
		return ServerManagementAPIConstants.PUBLISH_STATE_NONE;
	}
	
	private void completeDelete(Path pathToBeDeleted) throws IOException {
		if( pathToBeDeleted.toFile().exists()) {
			try (Stream<Path> paths = Files.walk(pathToBeDeleted)) {
				paths.sorted(Comparator.reverseOrder())
			      .map(Path::toFile)
			      .forEach(File::delete);

			}
		}
	}

	protected int removeFileModule(DeployableReference reference, 
			int publishType, int modulePublishType,
			File destination) throws CoreException {
		if( destination.delete() )
			return ServerManagementAPIConstants.PUBLISH_STATE_NONE;
		return delegate.getServerPublishModel().getDeployableState(
				reference).getPublishState();
	}
	
	protected int removeExplodedModule(DeployableReference reference, 
			int publishType, int modulePublishType,
			File destination) throws CoreException {
		try {
			completeDelete(destination.toPath());
			return ServerManagementAPIConstants.PUBLISH_STATE_NONE;
		} catch(IOException ioe) {
			return delegate.getServerPublishModel().getDeployableState(
					reference).getPublishState();
		}
	}
	
	protected int removeModule(DeployableReference reference, 
			int publishRequestType, int modulePublishState) throws CoreException {
		File dest = getDestinationPath(reference).toFile();
		if( dest == null || !dest.exists()) {
			return delegate.getServerPublishModel().getDeployableState(
					reference).getPublishState();
		}
		
		if( dest.isFile()) {
			return removeFileModule(reference, publishRequestType, modulePublishState, dest);
		} else {//if( dest.isDirectory()) {
			return removeExplodedModule(reference, publishRequestType, modulePublishState, dest);
		}
	}
	
	
	public class CopyFileVisitor extends SimpleFileVisitor<Path> {
	    private final Path targetPath;
	    private Path sourcePath = null;
	    public CopyFileVisitor(Path targetPath) {
	        this.targetPath = targetPath;
	    }

	    @Override
	    public FileVisitResult preVisitDirectory(final Path dir,
	    final BasicFileAttributes attrs) throws IOException {
	        if (sourcePath == null) {
	            sourcePath = dir;
	        } else {
	        Files.createDirectories(targetPath.resolve(sourcePath
	                    .relativize(dir)));
	        }
	        return FileVisitResult.CONTINUE;
	    }

	    @Override
	    public FileVisitResult visitFile(final Path file,
	    final BasicFileAttributes attrs) throws IOException {
	    Files.copy(file,
	        targetPath.resolve(sourcePath.relativize(file)));
	    return FileVisitResult.CONTINUE;
	    }
	}
	
}
