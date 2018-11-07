/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.MultiStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugEvent;
import org.jboss.tools.rsp.eclipse.debug.core.IDebugEventSetListener;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.IStreamListener;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.launching.RuntimeProcessEventManager;
import org.jboss.tools.rsp.launching.utils.StatusConverter;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.model.internal.ServerPublishStateModel;
import org.jboss.tools.rsp.server.model.internal.ServerStreamListener;
import org.jboss.tools.rsp.server.spi.model.IServerModel;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerPublishModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public abstract class AbstractServerDelegate implements IServerDelegate, IDebugEventSetListener {

	private static final String PROCESS_ID_KEY = "process.id.key";
	
	private int serverState = STATE_UNKNOWN;
	private String currentMode = null;
	private final List<ILaunch> launches = new ArrayList<>();
	protected final HashMap<String, Object> sharedData = new HashMap<>();
	private final IServer server;
	
	private final ServerPublishStateModel publishModel = new ServerPublishStateModel();
	
	public AbstractServerDelegate(IServer server) {
		this.server = server;
		if( registerAsProcessListener())
			RuntimeProcessEventManager.getDefault().addListener(this);
	}

	@Override
	public synchronized Object getSharedData(String key) {
		return sharedData.get(key);
	}
	
	@Override
	public synchronized void putSharedData(String key, Object o) {
		sharedData.put(key, o);
	}
	
	@Override
	public void dispose() {
		if( registerAsProcessListener())
			RuntimeProcessEventManager.getDefault().removeListener(this);
	}
	
	protected boolean registerAsProcessListener() {
		return true;
	}
	
	@Override
	public IServer getServer() {
		return server;
	}

	protected IStatus errorStatus(String err, String bundle) {
		return new Status(IStatus.ERROR, bundle, err);
	}

	protected CreateServerValidation validationErrorResponse(String msg, String key, String bundle) {
		if( key != null ) {
			return new CreateServerValidation(errorStatus(msg, bundle),
					Arrays.asList(key));
		}
		return new CreateServerValidation(errorStatus(msg, bundle),Collections.emptyList());
	}

	@Override
	public CreateServerValidation validate() {
		return new CreateServerValidation(Status.OK_STATUS, new ArrayList<String>());
	}

	/**
	 * Returns the current state of this server.
	 * <p>
	 * Note that this operation is guaranteed to be fast
	 * (it does not actually communicate with any actual
	 * server).
	 * </p>
	 *
	 * @return one of the server state (<code>STATE_XXX</code>)
	 * constants declared on {@link IServer}
	 */
	@Override
	public int getServerRunState() {
		return serverState;
	}
	
	protected ServerHandle getServerHandle() {
		IServerType ist = getServer().getServerType();
		ServerType st = new ServerType(ist.getId(), ist.getName(), ist.getDescription());
		ServerHandle sh = new ServerHandle(getServer().getId(), st);
		return sh;
	}
	
	
	@Override
	public ServerState getServerState() {
		ServerHandle handle = getServerHandle();
		ServerState state = new ServerState();
		state.setServer(handle);
		state.setState(getServerRunState());
		state.setDeployableStates(getServerPublishModel().getDeployableStates());
		return state;
	}
	
	protected void setServerState(int state) {
		setServerState(state, true);
	}

	protected void setServerState(int state, boolean fire) {
		if( state != this.serverState) {
			this.serverState = state;
			if( fire ) 
				fireStateChanged(getServerState());
		}
	}
	
	protected void fireStateChanged(ServerState state) {
		getServerModel().fireServerStateChanged(server, state);
	}

	protected void fireServerProcessTerminated(String processId) {
		getServerModel().fireServerProcessTerminated(server, processId);
	}
	
	protected void fireServerProcessCreated(String processId) {
		getServerModel().fireServerProcessCreated(server, processId);
	}

	protected IServerModel getServerModel() {
		return server.getServerManagementModel().getServerModel();
	}

	/**
	 * Returns the ILaunchManager mode that the server is in. This method will
	 * return null if the server is not running.
	 * 
	 * @return the mode in which a server is running, one of the mode constants
	 *    defined by {@link org.eclipse.debug.core.ILaunchManager}, or
	 *    <code>null</code> if the server is stopped.
	 */
	@Override
	public String getMode() {
		return currentMode;
	}
	
	public void setMode(String mode) {
		this.currentMode = mode;
	}

	/**
	 * Returns whether this server is in a state that it can
	 * be started in the given mode.
	 * <p>
	 * This call should complete reasonably fast and not require communication
	 * with the (potentially remote) server. If communication is required it
	 * should be done asynchronously and this method should either fail until
	 * that is complete or succeed and handle failure during start.
	 * </p><p>
	 * This method is called by the server core framework,
	 * in response to a call to <code>IServer.canStart()</code>.
	 * The framework has already filtered out obviously invalid situations,
	 * such as starting a server that is already running.
	 * Clients should never call this method directly.
	 * </p>
	 * 
	 * @param launchMode a mode in which a server can be launched,
	 *    one of the mode constants defined by
	 *    {@link org.eclipse.debug.core.ILaunchManager}
	 * @return a status object with code <code>IStatus.OK</code> if the server can
	 *    be started, otherwise a status object indicating why it can't
	 * @since 1.1
	 */
	public IStatus canStart(String launchMode) {
		return Status.OK_STATUS;
	}

	/**
	 * Returns whether this server is in a state that it can
	 * be restarted in the given mode. Note that only servers
	 * that are currently running can be restarted.
	 * <p>
	 * This call should complete reasonably fast and not require communication
	 * with the (potentially remote) server. If communication is required it
	 * should be done asynchronously and this method should either fail until
	 * that is complete or succeed and handle failure during restart.
	 * </p><p>
	 * This method is called by the server core framework,
	 * in response to a call to <code>IServer.canRestart()</code>.
	 * The framework has already filtered out obviously invalid situations,
	 * such as restarting a stopped server.
	 * Clients should never call this method directly.
	 * </p>
	 * 
	 * @param mode a mode in which a server can be launched,
	 *    one of the mode constants defined by
	 *    {@link org.eclipse.debug.core.ILaunchManager}
	 * @return a status object with code <code>IStatus.OK</code> if the server can
	 *    be restarted, otherwise a status object indicating why it can't
	 * @since 1.1
	 */
	public IStatus canRestart(String mode) {
		return Status.OK_STATUS;
	}

	/**
	 * Returns whether this server is in a state that it can
	 * be stopped.
	 * Servers can be stopped if they are not already stopped and if
	 * they belong to a state-set that can be stopped.
	 * <p>
	 * This call should complete reasonably fast and not require communication
	 * with the (potentially remote) server. If communication is required it
	 * should be done asynchronously and this method should either fail until
	 * that is complete or succeed and handle failure during stop.
	 * </p><p>
	 * This method is called by the server core framework,
	 * in response to a call to <code>IServer.canStop()</code>.
	 * The framework has already filtered out obviously invalid situations,
	 * such as stopping a server that is already stopped.
	 * Clients should never call this method directly.
	 * </p>
	 * 
	 * @return a status object with code <code>IStatus.OK</code> if the server can
	 *   be stopped, otherwise a status object indicating why it can't
	 * @since 1.1
	 */
	public IStatus canStop() {
		return Status.OK_STATUS;
	}
	
	protected boolean modesContains(String needle) {
		if (needle == null) {
			return false;
		}
		ServerLaunchMode[] modes = getServer().getServerType().getLaunchModes();
		return Arrays.stream(modes).anyMatch(
				mode -> needle.equals(mode.getMode()));
	}
	
	@Override
	public StartServerResponse start(String mode) {
		IStatus s = new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, "Server Start not implemented");
		return new StartServerResponse(StatusConverter.convert(s), null);
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		ArrayList<ILaunch> launchList = new ArrayList<>(this.launches);
		for( int i = 0; i < events.length; i++ ) {
			Object o = events[i].getSource();
			if( o instanceof IProcess && events[i].getKind() == DebugEvent.TERMINATE) {
				// a process has terminated. Check if it's one of mine
				for( ILaunch l : launchList ) {
					List<IProcess> processes = Arrays.asList(l.getProcesses());
					if( processes.contains(o)) {
						processTerminated((IProcess)o);
					}
				}
			}
		}
	}
	
	protected void processTerminated(IProcess p) {
		this.fireServerProcessTerminated(getProcessId(p));	
	}

	protected void registerLaunch(ILaunch launch2) {
		launches.add(launch2);
		String ctime = "" + System.currentTimeMillis();
		IProcess[] all = launch2.getProcesses();
		for( int i = 0; i < all.length; i++ ) {
			String pName = getServer().getTypeId() + ":" + getServer().getId()
					+ ":" + ctime + ":p" + i;
			all[i].setAttribute(PROCESS_ID_KEY, pName);
			IStreamListener out = new ServerStreamListener(
					getServer(), getProcessId(all[i]), 
					ServerManagementAPIConstants.STREAM_TYPE_SYSOUT);
			IStreamListener err = new ServerStreamListener(
					getServer(), getProcessId(all[i]), 
					ServerManagementAPIConstants.STREAM_TYPE_SYSERR);
			all[i].getStreamsProxy().getOutputStreamMonitor().addListener(out);
			all[i].getStreamsProxy().getErrorStreamMonitor().addListener(err);
			fireServerProcessCreated(pName);
		}
	}

	protected String getProcessId(IProcess p) {
		return p.getAttribute(PROCESS_ID_KEY);
	}
	
	/*
	 * Polling utility methods
	 */
	protected IPollResultListener launchServerResultListener() {
		return new IPollResultListener() {

			@Override
			public void stateNotAsserted(IServerStatePoller.SERVER_STATE expectedState, IServerStatePoller.SERVER_STATE currentState) {
				stop(true);
			}

			@Override
			public void stateAsserted(IServerStatePoller.SERVER_STATE expectedState, IServerStatePoller.SERVER_STATE currentState) {
				if (currentState == IServerStatePoller.SERVER_STATE.UP) {
					setServerState(STATE_STARTED);
				} else {
					setServerState(STATE_STOPPED);
				}
			}
		};
	}
	
	protected IPollResultListener shutdownServerResultListener() {
		return new IPollResultListener() {
			@Override
			public void stateNotAsserted(IServerStatePoller.SERVER_STATE expectedState, IServerStatePoller.SERVER_STATE currentState) {
				setServerState(STATE_STARTED);
			}

			@Override
			public void stateAsserted(IServerStatePoller.SERVER_STATE expectedState, IServerStatePoller.SERVER_STATE currentState) {
				if (currentState == IServerStatePoller.SERVER_STATE.UP) {
					setServerState(STATE_STARTED);
				} else {
					setServerState(STATE_STOPPED);
				}
			}
		};
	}

	public ILaunch[] getLaunches() {
		return launches.toArray(new ILaunch[launches.size()]);
	}

	@Override
	public IStatus canAddDeployable(DeployableReference reference) {
		return Status.OK_STATUS;
	}
	
	@Override
	public IStatus canRemoveDeployable(DeployableReference reference) {
		return Status.OK_STATUS;
	}
	
	@Override
	public IServerPublishModel getServerPublishModel() {
		return publishModel;
	}
	
	@Override
	public IStatus publish(int publishType) {
		MultiStatus ms = new MultiStatus(ServerCoreActivator.BUNDLE_ID, 0, "Publishing server " + getServer().getName(), null);
		try {
			publishStart(publishType);
			List<DeployableState> list = getServerPublishModel().getDeployableStates();
			for( DeployableState state : list ) {
				try {
					publish(publishType, state);
				} catch(CoreException ce) {
					String mod = state.getReference().getLabel();
					String server = getServer().getName();
					ms.add(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
							NLS.bind("Error while publishing deployable {0} to server {1}", mod, server), ce)); 
				}
			}
		} catch(CoreException ce) {
			ms.add(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
					NLS.bind("Error publishing to server {0}", getServer().getName()), ce));
		} finally {
			try {
				publishFinish(publishType);
			} catch(CoreException ce) {
				ms.add(new Status(IStatus.ERROR, ServerCoreActivator.BUNDLE_ID, 
						NLS.bind("Error completing publishing to server {0}", getServer().getName()), ce));
			}
		}
		
		return ms;
	}

	protected void publish(int publishType, DeployableState state) throws CoreException {
		int publishState = state.getPublishState();
		publishDeployable(state.getReference(), publishType, publishState);
		DeployableState postState = getServerPublishModel().getDeployableState(state.getReference());
		
		// If deployable was to be removed, and it was successfully removed, 
		// clean it from the publish model cache entirely. 
		if( publishState == ServerManagementAPIConstants.PUBLISH_STATE_REMOVE) {
			if( postState != null && postState.getPublishState() == ServerManagementAPIConstants.PUBLISH_STATE_NONE) {
				getServerPublishModel().deployableRemoved(state.getReference());
			}
		}
	}

	protected void publishStart(int publishType) throws CoreException {
		// Clients override
	}

	protected void publishFinish(int publishType) throws CoreException {
		// Clients override
	}

	protected void publishDeployable(DeployableReference reference, int publishType, int deployablemodulePublishType) throws CoreException {
		// Clients should override this default implementation
		setDeployablePublishState(reference, ServerManagementAPIConstants.PUBLISH_STATE_NONE);
		setDeployableState(reference, ServerManagementAPIConstants.STATE_STARTED);
	}

	protected void setDeployablePublishState(DeployableReference reference, int publishState) {
		getServerPublishModel().setDeployablePublishState(reference, publishState);
	}

	protected void setDeployableState(DeployableReference reference, int runState) {
		getServerPublishModel().setDeployableState(reference, runState);
	}
	
	protected DeployableState getDeployableState(DeployableReference reference) {
		return getServerPublishModel().getDeployableState(reference);
	}
	
	@Override
	public IStatus canPublish() {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus clientSetServerStarting(ServerStartingAttributes attr) {
		setServerState(STATE_STARTING, true);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus clientSetServerStarted(LaunchParameters attr) {
		setServerState(STATE_STARTED, true);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus stop(boolean force) {
		setServerState(STATE_STOPPED, true);
		return Status.OK_STATUS;
	}
}
