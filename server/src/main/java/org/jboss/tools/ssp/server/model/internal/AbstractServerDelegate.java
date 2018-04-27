package org.jboss.tools.ssp.server.model.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.ssp.server.spi.servertype.IServer;
import org.jboss.tools.ssp.server.spi.servertype.IServerDelegate;

public abstract class AbstractServerDelegate implements IServerDelegate {

	private int serverState = STATE_UNKNOWN;
	private String currentMode = null;
	
	private IServer server;
	public AbstractServerDelegate(IServer server) {
		this.server = server;
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
		this.serverState = state;
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
}
