/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.tools.ssp.api.ServerManagementAPIConstants;
import org.jboss.tools.ssp.eclipse.core.runtime.IStatus;
import org.jboss.tools.ssp.eclipse.core.runtime.Status;
import org.jboss.tools.ssp.eclipse.debug.core.DebugEvent;
import org.jboss.tools.ssp.eclipse.debug.core.IDebugEventSetListener;
import org.jboss.tools.ssp.eclipse.debug.core.ILaunch;
import org.jboss.tools.ssp.eclipse.debug.core.IStreamListener;
import org.jboss.tools.ssp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.ssp.eclipse.debug.core.model.IStreamMonitor;
import org.jboss.tools.ssp.launching.RuntimeProcessEventManager;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;

public abstract class AbstractServerDelegate implements IServerDelegate, IDebugEventSetListener {
	private static final String PROCESS_ID_KEY = "process.id.key";
	
	private int serverState = STATE_UNKNOWN;
	private String currentMode = null;
	private List<ILaunch> launches = new ArrayList<ILaunch>();
	
	private IServer server;
	public AbstractServerDelegate(IServer server) {
		this.server = server;
		if( registerAsProcessListener())
			RuntimeProcessEventManager.getDefault().addListener(this);
	}
	
	public void dispose() {
		if( registerAsProcessListener())
			RuntimeProcessEventManager.getDefault().removeListener(this);
	}
	
	protected boolean registerAsProcessListener() {
		return true;
	}
	
	
	public IServer getServer() {
		return server;
	}

	@Override
	public abstract IStatus validate();

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
	public int getServerState() {
		return serverState;
	}

	protected void setServerState(int state) {
		if( state != this.serverState) {
			this.serverState = state;
			fireStateChanged(state);
		}
	}
	
	protected void fireStateChanged(int state) {
		ServerManagementModel.getDefault().getServerModel().fireServerStateChanged(server, state);
	}

	protected void fireServerProcessTerminated(String processId) {
		ServerManagementModel.getDefault().getServerModel().fireServerProcessTerminated(server, processId);
	}
	
	protected void fireServerProcessCreated(String processId) {
		ServerManagementModel.getDefault().getServerModel().fireServerProcessCreated(server, processId);
	}

	private void fireStreamAppended(IServer server2, IProcess process, int streamType, String text) {
		ServerManagementModel.getDefault().getServerModel().fireServerStreamAppended(
				server2, getProcessId(process), streamType, text);
	}

	/**
	 * Returns the ILaunchManager mode that the server is in. This method will
	 * return null if the server is not running.
	 * 
	 * @return the mode in which a server is running, one of the mode constants
	 *    defined by {@link org.eclipse.debug.core.ILaunchManager}, or
	 *    <code>null</code> if the server is stopped.
	 */
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
	
	public abstract IStatus start(String mode);
	
	public void handleDebugEvents(DebugEvent[] events) {
		ArrayList<ILaunch> launchList = new ArrayList<ILaunch>(this.launches);
		for( int i = 0; i < events.length; i++ ) {
			Object o = events[i].getSource();
			if( o instanceof IProcess && events[i].getKind() == DebugEvent.TERMINATE) {
				// a process has terminated. Check if it's one of mine
				for( ILaunch l : launchList ) {
					List<IProcess> processes = Arrays.asList(l.getProcesses());
					if( processes.contains(o)) {
						processTerminated((IProcess)o, l);
					}
				}
			}
		}
	}
	
	protected void processTerminated(IProcess p, ILaunch l) {
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
					getServer(), all[i], ServerManagementAPIConstants.STREAM_TYPE_SYSOUT);
			IStreamListener err = new ServerStreamListener(
					getServer(), all[i], ServerManagementAPIConstants.STREAM_TYPE_SYSERR);
			all[i].getStreamsProxy().getOutputStreamMonitor().addListener(out);
			all[i].getStreamsProxy().getErrorStreamMonitor().addListener(err);
			fireServerProcessCreated(pName);
		}
	}

	
	public class ServerStreamListener implements IStreamListener {

		
		private IServer server;
		private IProcess process;
		private int streamType;
		public ServerStreamListener(IServer server, IProcess process, int type) {
			this.server = server;
			this.process = process;
			this.streamType = type;
		}
		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			fireStreamAppended(server, process, streamType, text);
		}
		
	}
	
	protected String getProcessId(IProcess p) {
		return p.getAttribute(PROCESS_ID_KEY);
	}
}
