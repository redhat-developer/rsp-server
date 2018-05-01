package org.jboss.tools.ssp.server.spi.servertype;

import org.eclipse.core.runtime.IStatus;
import org.jboss.tools.ssp.api.ServerManagementClient;

public interface IServerDelegate {

	/**
	 * Server state constant (value 0) indicating that the
	 * server is in an unknown state.
	 * 
	 * @see #getServerState()
	 * @see #getModuleState(IModule[])
	 */
	public static final int STATE_UNKNOWN = ServerManagementClient.STATE_UNKNOWN;

	/**
	 * Server state constant (value 1) indicating that the
	 * server is starting, but not yet ready to serve content.
	 * 
	 * @see #getServerState()
	 * @see #getModuleState(IModule[])
	 */
	public static final int STATE_STARTING = ServerManagementClient.STATE_STARTING;

	/**
	 * Server state constant (value 2) indicating that the
	 * server is ready to serve content.
	 * 
	 * @see #getServerState()
	 * @see #getModuleState(IModule[])
	 */
	public static final int STATE_STARTED = ServerManagementClient.STATE_STARTED;

	/**
	 * Server state constant (value 3) indicating that the
	 * server is shutting down.
	 * 
	 * @see #getServerState()
	 * @see #getModuleState(IModule[])
	 */
	public static final int STATE_STOPPING = ServerManagementClient.STATE_STOPPING;

	/**
	 * Server state constant (value 4) indicating that the
	 * server is stopped.
	 * 
	 * @see #getServerState()
	 * @see #getModuleState(IModule[])
	 */
	public static final int STATE_STOPPED = ServerManagementClient.STATE_STOPPED;

	/**
	 * Publish state constant (value 0) indicating that it's
	 * in an unknown state.
	 * 
	 * @see #getServerPublishState()
	 * @see #getModulePublishState(IModule[])
	 */
	public static final int PUBLISH_STATE_UNKNOWN = ServerManagementClient.PUBLISH_STATE_UNKNOWN;

	/**
	 * Publish state constant (value 1) indicating that there
	 * is no publish required.
	 * 
	 * @see #getServerPublishState()
	 * @see #getModulePublishState(IModule[])
	 */
	public static final int PUBLISH_STATE_NONE = ServerManagementClient.PUBLISH_STATE_NONE;

	/**
	 * Publish state constant (value 2) indicating that an
	 * incremental publish is required.
	 * 
	 * @see #getServerPublishState()
	 * @see #getModulePublishState(IModule[])
	 */
	public static final int PUBLISH_STATE_INCREMENTAL = ServerManagementClient.PUBLISH_STATE_INCREMENTAL;

	/**
	 * Publish state constant (value 3) indicating that a
	 * full publish is required.
	 * 
	 * @see #getServerPublishState()
	 * @see #getModulePublishState(IModule[])
	 */
	public static final int PUBLISH_STATE_FULL = ServerManagementClient.PUBLISH_STATE_FULL;

	/**
	 * Publish kind constant (value 1) indicating an incremental publish request.
	 * 
	 * @see #publish(int, IProgressMonitor)
	 */
	public static final int PUBLISH_INCREMENTAL = ServerManagementClient.PUBLISH_INCREMENTAL;

	/**
	 * Publish kind constant (value 2) indicating a full publish request.
	 * 
	 * @see #publish(int, IProgressMonitor)
	 */
	public static final int PUBLISH_FULL = ServerManagementClient.PUBLISH_FULL;

	/**
	 * Publish kind constant (value 3) indicating an automatic publish request.
	 * 
	 * @see #publish(int, IProgressMonitor)
	 */
	public static final int PUBLISH_AUTO = ServerManagementClient.PUBLISH_AUTO;

	/**
	 * Publish kind constant (value 4) indicating a publish clean request
	 * 
	 * @see #publish(int, IProgressMonitor)
	 */
	public static final int PUBLISH_CLEAN = ServerManagementClient.PUBLISH_CLEAN;

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
	public int getServerState();

	/**
	 * Returns the ILaunchManager mode that the server is in. This method will
	 * return null if the server is not running.
	 * 
	 * @return the mode in which a server is running, one of the mode constants
	 *    defined by {@link org.eclipse.debug.core.ILaunchManager}, or
	 *    <code>null</code> if the server is stopped.
	 */
	public String getMode();
	
	public IStatus start(String mode);
	
	public IStatus validate();

	public IStatus stop(boolean force);
	
	public void dispose();
}
