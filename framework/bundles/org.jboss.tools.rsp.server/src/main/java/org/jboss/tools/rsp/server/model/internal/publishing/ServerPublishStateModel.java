/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal.publishing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherEventListener;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherService;
import org.jboss.tools.rsp.server.spi.publishing.IFullPublishRequiredCallback;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableDelta;
import org.jboss.tools.rsp.server.spi.servertype.IDeploymentAssemblyMapping;
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ServerPublishStateModel implements IServerPublishModel, IFileWatcherEventListener {
	static final Logger LOG = LoggerFactory.getLogger(ServerPublishStateModel.class);

	private final Map<String, DeployableState> states;
	private final Map<String, Map<String,Object>> deploymentOptions;
	private final Map<String, DeployableDelta> deltas = new HashMap<>();
	private final Map<String, DeploymentAssemblyFile> assembly = new HashMap<>();
	
	private AbstractServerDelegate delegate;
	private IFileWatcherService fileWatcher;
	private int publishState = AbstractServerDelegate.PUBLISH_STATE_UNKNOWN;
	
	private AutoPublishThread autoPublish;

	private IFullPublishRequiredCallback fullPublishRequired;
	
	public ServerPublishStateModel(AbstractServerDelegate delegate, IFileWatcherService fileWatcher) {
		this(delegate, fileWatcher, null);
	}
	public ServerPublishStateModel(AbstractServerDelegate delegate, 
			IFileWatcherService fileWatcher, IFullPublishRequiredCallback fullPublishRequired) {
		this.delegate = delegate;
		this.fileWatcher = fileWatcher;
		this.fullPublishRequired = fullPublishRequired;
		this.states = new LinkedHashMap<>();
		this.deploymentOptions = new LinkedHashMap<>();
	}

	@Override
	public synchronized void initialize(List<DeployableReference> references) {
		for( DeployableReference reference : references ) {
			addDeployableImpl(reference, ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN);
		}
		updateServerPublishStateFromDeployments();
		fireState();
	}
	
	private IStatus addDeployableImpl(DeployableReference reference, int publishState) {
		DeployableState deployableState = 
				createDeployableState(reference, publishState, ServerManagementAPIConstants.STATE_UNKNOWN);
		
		String key = getKey(reference);
		getStates().put(key, deployableState);
		deploymentOptions.put(getKey(reference), reference.getOptions());

		return registerFileWatcher(reference);
	}

	private DeployableState cloneDeployableState(DeployableReference reference, DeployableState state) {
		return createDeployableState(reference, state.getPublishState(), state.getState());
	}
	
	private DeployableState createDeployableState(DeployableReference reference, int publishState, int state) {
		DeployableState deployableState = new DeployableState();
		deployableState.setPublishState(publishState);
		deployableState.setState(state);
		deployableState.setReference(new DeployableReference(reference.getLabel(), reference.getPath()));
		deployableState.setServer(delegate.getServerHandle());
		return deployableState;
	}

	private IStatus cacheAssemblies(DeployableReference reference) {
		Map<String, Object>  opts = reference.getOptions();
		String assemblyFileS = opts == null ? null : (String)opts.get(ServerManagementAPIConstants.DEPLOYMENT_OPTION_ASSEMBLY_FILE);
		File assemblyFile = assemblyFileS == null || assemblyFileS.isEmpty() ? null : new File(assemblyFileS);
		boolean optionEmpty = (assemblyFileS == null || assemblyFileS.isEmpty());
		if( optionEmpty) {
			assemblyFile = findRspAssemblyJson(reference.getPath());
		}
		boolean useAssembly = assemblyFile != null && assemblyFile.exists();
		if( useAssembly ) {
			try {
				String contents = readFile(assemblyFile);
				Map<String, Object> assemblyAsJson = new Gson().fromJson(contents, Map.class);
				DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);
				assembly.put(getKey(reference), asObj);
			} catch( JsonSyntaxException jse ) {
				return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, IStatus.ERROR, 
						NLS.bind("Could not add deployable with path {0}: Error parsing deployment assembly file.", 
								getKey(reference)), jse);
			}
		}
		return Status.OK_STATUS;
	}
	
	private IStatus registerFileWatcher(DeployableReference reference) {
		if( fileWatcher != null ) {
			// DEPLOY_ASSEMBLY
			IStatus ret = cacheAssemblies(reference);
			if( !ret.isOK()) {
				return ret;
			}
			
			List<Path> sourcePaths = getDeploySourceFolders(reference);
			for( int i = 0; i < sourcePaths.size(); i++ ) {
				Path sourcePathToWatch = sourcePaths.get(i);
				File asFile = sourcePathToWatch.toFile();
				if( asFile.exists()) {
					boolean recursive = asFile.exists() && asFile.isDirectory();
					System.out.println("  Source to watch: " + sourcePathToWatch.toString());
					fileWatcher.addFileWatcherListener(sourcePathToWatch, this, recursive);
				}
			}
		}
		return Status.OK_STATUS;
	}
	
	private List<Path> getDeploySourceFolders(DeployableReference ref) {
		DeploymentAssemblyFile assemblyData = assembly.get(getKey(ref));
		if( assemblyData == null ) {
			File f = new File(ref.getPath());
			ArrayList<Path> al = new ArrayList<Path>();
			al.add(f.toPath());
			return al;
		} else {
			return getAssemblySourceFolders(ref);
		}
	}
	private List<Path> getAssemblySourceFolders(DeployableReference ref) {
		List<Path> ret = new ArrayList<Path>();
		DeploymentAssemblyFile assemblyData = assembly.get(getKey(ref));
		IDeploymentAssemblyMapping[] mappings = assemblyData.getMappings();
		if( mappings != null ) {
			for( int i = 0; i < mappings.length; i++ ) {
				IDeploymentAssemblyMapping singleMapping = mappings[i];
				String source = singleMapping.getSource();
				Path sourcePathToWatch = Paths.get(ref.getPath(), source);
				File sourcePathFileToWatch = sourcePathToWatch.toFile();
				if( sourcePathFileToWatch.exists()) {
					ret.add(sourcePathToWatch);
				}
			}
		}
		return ret;
	}
	
	public static String readFile(File file) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
		}
		return content;
	}

	private File findRspAssemblyJson(String path) {
		String needle = ServerManagementAPIConstants.DEPLOYMENT_OPTION_ASSEMBLY_FILE_DEFAULT;
		try {
			Object[] ret = Files.walk(new File(path).toPath()).filter(p2 -> p2.endsWith(needle)).toArray();
			if( ret != null && ret.length > 0 ) {
				return ((Path)ret[0]).toFile();
			}
		} catch(IOException ioe) {
		}
		return null;
	}
	/**
	 * Adds the given deployable to this model.
	 * 
	 * @param withOptions the deployable to add.
	 */
	@Override
	public synchronized IStatus addDeployable(DeployableReference withOptions) {
		DeployableState ds = getStates().get(getKey(withOptions));
		if (ds != null && ds.getPublishState() != ServerManagementAPIConstants.PUBLISH_STATE_REMOVE) {
			return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, IStatus.ERROR, 
					NLS.bind("Could not add deployable with path {0}: it already exists.", 
							getKey(withOptions)), null);
		}

		addDeployableImpl(withOptions, ServerManagementAPIConstants.PUBLISH_STATE_ADD);
		updateServerPublishStateFromDeployments();
		fireState();
		launchOrUpdateAutopublishThread();
		return Status.OK_STATUS;
	}

	@Override
	public synchronized boolean contains(DeployableReference reference) {
		return getStates().containsKey(getKey(reference));
	}

	@Override
	public synchronized IStatus removeDeployable(DeployableReference reference) {
		DeployableState ds = getStates().get(getKey(reference));
		if (ds == null) {
			return new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, IStatus.ERROR, 
					NLS.bind("Could not remove deploybale with path {0}: it doesn't exist", getKey(reference)),
							null);
		}
		if (ds.getPublishState() == ServerManagementAPIConstants.PUBLISH_STATE_ADD) {
			// It hasn't been added / published yet, so we can remove it immediately
			deployableRemoved(reference);
		}
		ds.setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE);
		String path = reference.getPath();
		if (fileWatcher != null) {
			// DEPLOY_ASSEMBLY TODO
			fileWatcher.removeFileWatcherListener(new File(path).toPath(), this);
		}
		updateServerPublishStateFromDeployments();
		fireState();
		launchOrUpdateAutopublishThread();
		return Status.OK_STATUS;
	}

	protected String getKey(DeployableReference reference) {
		if (reference == null) {
			return null;
		} 
		return reference.getPath();
	}
	
	@Override
	public synchronized void deployableRemoved(DeployableReference reference) {
		String k = getKey(reference);
		getStates().remove(k);
		deploymentOptions.remove(k);
	}

	@Override
	public synchronized List<DeployableState> getDeployableStates() {
		List<DeployableState> ret = getStates().values().stream().
				map(element -> cloneDeployableState(element.getReference(), element))
				.collect(Collectors.toList());
		return new ArrayList<>(ret);
	}

	@Override
	public synchronized List<DeployableState> getDeployableStatesWithOptions() {
		List<DeployableState> ret = getStates().values().stream().
				map(element -> cloneDeployableState(element.getReference(), element))
				.collect(Collectors.toList());
		for( DeployableState ds : ret ) {
			fillOptionsFromCache(ds.getReference());
		}
		return new ArrayList<>(ret);
	}

	@Override
	public synchronized DeployableState getDeployableState(DeployableReference reference) {
		DeployableState ds = getStates().get(getKey(reference));
		if (ds == null) {
			return null;
		}
		return cloneDeployableState(reference, ds);
	}

	/**
	 * for testing purposes
	 */
	protected Map<String, DeployableState> getStates() {
		return states;
	}

	/**
	 * for testing purposes
	 */
	protected Map<String, DeployableDelta> getDeltas() {
		return deltas;
	}

	public IDeploymentAssemblyMapping[] getDeployableResourceMappings(DeployableReference reference) {
		DeploymentAssemblyFile file = assembly.get(getKey(reference));
		if( file != null ) {
			return file.getMappings();
		}
		return new IDeploymentAssemblyMapping[] {
				new DeploymentAssemblyMapping(reference.getPath(), "/")
		};
	}

	
	@Override
	public synchronized void setDeployablePublishState(DeployableReference reference, int publishState) {
		DeployableState ds = getDeployableState(reference);
		if (ds == null) {
			return;
		}
		DeployableState next = createDeployableState(reference, publishState, ds.getState());
		String key = getKey(reference);
		getStates().put(key, next);
		if( publishState == ServerManagementAPIConstants.PUBLISH_STATE_NONE) {
			clearDelta(key);
		}
		updateServerPublishStateFromDeployments();
		launchOrUpdateAutopublishThread();

	}

	private void clearDelta(String key) {
		DeployableDelta delta2 = getDeltas().get(key);
		if( delta2 != null ) {
			delta2.clear();
		}
	}

	@Override
	public synchronized void setDeployableState(DeployableReference reference, int runState) {
		DeployableState ds = getStates().get(getKey(reference));
		if (ds == null) {
			return;
		}
		DeployableState next = createDeployableState(reference, ds.getPublishState(), runState);
		getStates().put(getKey(reference), next);
	}

	/*
	 * If a path matching one of our deployments has been modified,
	 * created, or deleted, we should respond to this. However, most 
	 * cases will not require us to do much of anything. 
	 * 
	 *  If the deployment is currently set to be 'added', we should not make any changes
	 *  to the publish state, since the next publish will do the full add as expected.
	 *  
	 *  If the deployment is currently set to be 'removed', we should not make any 
	 *  changes to the publish state, since the next publish will remove the deployment.
	 *  
	 *  If the deployment is currently set to be 'incremental', 
	 *  no change is needed. It's already marked as requiring a publish. 
	 *  
	 *  If the deployment is currently set to be 'full',
	 *  no change is needed. It's already marked as requiring a publish.
	 *  However it's worth registering the delta in case a server delegate 
	 *  needs to know the list of changed resources.
	 *  
	 *  If the deployment is currently set to 'unknown', 
	 *  we should not make any change, so the delegate knows the state is still uncertain.
	 *  
	 *  So only if the deployment is currently set to 'none' do we know 
	 *  that we should now mark it as requiring an incremental publish. 
	 * 
	 */
	@Override
	public synchronized void fileChanged(FileWatcherEvent event) {
		// DEPLOY_ASSEMBLY
		Path affected = event.getPath();
		//System.out.println("File changed: " + affected.toString());
		List<DeployableState> ds = new ArrayList<>(getStates().values());
		boolean changed = false;
		for( DeployableState d : ds ) {
			DeploymentAssemblyFile assemblyObj = assembly.get(getKey(d.getReference()));
			if( assemblyObj == null ) {
				changed = fileChangedNoAssembly(event, affected, d);
			} else {
				changed = fileChangedWithAssembly(event, affected, d);
			}
		}
		updateServerPublishStateFromDeployments();
		if( changed ) 
			fireState();
		launchOrUpdateAutopublishThread();
	}

	private boolean fileChangedNoAssembly(FileWatcherEvent event, Path affected, DeployableState d) {
		boolean changed = false;
		Path deploymentPath = new File(d.getReference().getPath()).toPath();
		changed |= fileChangedSinglePath(event,  affected, d, deploymentPath);
		return changed;
	}
	

	private boolean fileChangedWithAssembly(FileWatcherEvent event, Path affected, DeployableState d) {
		boolean changed = false;
		List<Path> sourcePaths = getAssemblySourceFolders(d.getReference());
		for( int i = 0; i < sourcePaths.size(); i++ ) {
			Path deploymentPath = sourcePaths.get(i);
			changed |= fileChangedSinglePath(event, affected, d, deploymentPath);
		}
		return changed;		
	}
	
	private boolean fileChangedSinglePath(FileWatcherEvent event, Path affected, 
			DeployableState d, Path deploymentPath) {
		boolean changed = false;
		if( affected.startsWith(deploymentPath)) {
			int currentPubState = d.getPublishState();
			if( currentPubState == ServerManagementAPIConstants.PUBLISH_STATE_NONE
					|| currentPubState == ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL) {
				int newState = getRequiredPublishStateOnFileChange(event);
				if( newState > currentPubState ) {
					d.setPublishState(newState);
					changed = true;
				}
			}
			if( currentPubState == ServerManagementAPIConstants.PUBLISH_STATE_NONE 
					|| currentPubState == ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL
					|| currentPubState == ServerManagementAPIConstants.PUBLISH_STATE_FULL ) {
				registerSingleDelta(event, d.getReference());
			}
		}
		return changed;
	}
	
	protected int getRequiredPublishStateOnFileChange(FileWatcherEvent event) {
		if( fullPublishRequired != null && 
				fullPublishRequired.requiresFullPublish(event)) {
			return ServerManagementAPIConstants.PUBLISH_STATE_FULL;
		}
		return ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL;
	}
	
	private void registerSingleDelta(FileWatcherEvent event, DeployableReference reference) {
		String key = getKey(reference);
		DeployableDelta dd = getDeltas().computeIfAbsent(key, k ->  new DeployableDelta(new DeployableReference(reference.getLabel(), reference.getPath())));
		DeploymentAssemblyFile assemblyMap = assembly.get(getKey(reference));
		if( assemblyMap == null ) {
			dd.registerChange(event);
		} else {
			dd.registerChange(event, assemblyMap);
		}
	}

	private void fireState() {
		if( delegate != null ) {
			delegate.fireServerStateChanged();
		}
	}

	public synchronized void updateServerPublishStateFromDeployments() {
		updateServerPublishStateFromDeployments(false);
	}
	
	public synchronized void updateServerPublishStateFromDeployments(boolean fireEvent) {
		List<DeployableState> vals = new ArrayList<>(getStates().values());
		int newState = ServerManagementAPIConstants.PUBLISH_STATE_NONE;

		if( deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_ADD, vals) || 
				deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE, vals) ||
				deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_FULL, vals)) {
			newState = ServerManagementAPIConstants.PUBLISH_STATE_FULL;
		} else {
			if( deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN, vals)) {
				newState = ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN;
			} else if( deployableExists(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, vals)) {
				newState = ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL;
			} else {
				newState = ServerManagementAPIConstants.PUBLISH_STATE_NONE;
			}
		}
		setServerPublishState(newState, fireEvent);
	}
	
	private boolean deployableExists(int publishState, List<DeployableState> deployableStates) {
		return deployableStates.stream()
				.anyMatch(deployState -> deployState.getPublishState() == publishState);
	}

	@Override
	public synchronized int getServerPublishState() {
		return this.publishState;
	}

	@Override
	public synchronized void setServerPublishState(int state, boolean fire) {
		if( state != this.publishState) {
			this.publishState = state;
			if( fire ) 
				fireState();
		}
	}

	@Override
	public synchronized DeployableReference fillOptionsFromCache(DeployableReference reference) {
		if (reference == null) {
			return null;
		}
		reference.setOptions(deploymentOptions.get(getKey(reference)));
		return reference;
	}

	@Override
	public synchronized IDeployableDelta getDeployableResourceDelta(DeployableReference reference) {
		return cloneDelta(deltas.get(getKey(reference)));
	}
	
	private IDeployableDelta cloneDelta(DeployableDelta delta) {
		if( delta == null )
			return null;
		DeployableReference ref = cloneReference(delta.getReference());
		return new DeployableDelta(ref, delta.getResourceDeltaMap());
	}
	private DeployableReference cloneReference(DeployableReference ref) {
		return ref == null ? null : new DeployableReference(ref.getLabel(), ref.getPath());
	}

	protected boolean isAutoPublisherEnabled() {
		return delegate.getServer().getAttribute(
				DefaultServerAttributes.AUTOPUBLISH_ENABLEMENT, 
				DefaultServerAttributes.AUTOPUBLISH_ENABLEMENT_DEFAULT);
	}
	
	protected int getInactivityTimeout() {
		return delegate.getServer().getAttribute(
				DefaultServerAttributes.AUTOPUBLISH_INACTIVITY_LIMIT, 
				DefaultServerAttributes.AUTOPUBLISH_INACTIVITY_LIMIT_DEFAULT);
	}

	protected void launchOrUpdateAutopublishThread() {
		if (isAutoPublisherEnabled()) {
			launchOrUpdateAutopublishThreadImpl();
		}
	}
	protected void launchOrUpdateAutopublishThreadImpl() {
		synchronized (this) {
			if (this.autoPublish != null) {
				if (this.autoPublish.isDone() || this.autoPublish.getPublishBegan()) {
					// we need a new thread
					this.autoPublish = createNewAutoPublishThread( getInactivityTimeout());
					this.autoPublish.start();
				} else {
					this.autoPublish.updateInactivityCounter();
				}
			} else {
				this.autoPublish = createNewAutoPublishThread( getInactivityTimeout());
				this.autoPublish.start();
			}
		}
	}
	
	protected AutoPublishThread createNewAutoPublishThread(int timeout) {
		return new AutoPublishThread(delegate.getServer(), timeout);
	}
	
}
