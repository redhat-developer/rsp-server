/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype;

import java.io.File;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.model.polling.PollThreadUtils;
import org.jboss.tools.rsp.server.spi.model.polling.WebPortPoller;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.spi.util.VersionComparisonUtility;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.ExtendedServerPropertiesAdapterFactory;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.JBossExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.ServerExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.publishing.IJBossPublishController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJBossServerDelegate extends AbstractServerDelegate {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractJBossServerDelegate.class);
	public static final String START_LAUNCH_SHARED_DATA = "AbstractJBossServerDelegate.startLaunch";
	
	private IJBossPublishController publishController;
	
	public AbstractJBossServerDelegate(IServer server) {
		super(server);
	}

	protected abstract IServerStartLauncher getStartLauncher();
	
	protected abstract IServerShutdownLauncher getStopLauncher();

	protected abstract String getPollURL(IServer server);

	@Override
	public CreateServerValidation validate() {
		return validate(getServer());
	}
	
	protected CreateServerValidation validate(IServer server) {
		String home = server.getAttribute(IJBossServerAttributes.SERVER_HOME, (String)null);
		
		if( null == home ) {
			return validationErrorResponse("Server home must not be null", IJBossServerAttributes.SERVER_HOME, Activator.BUNDLE_ID);
		}
		if(!(new File(home).exists())) {
			return validationErrorResponse("Server home must exist", IJBossServerAttributes.SERVER_HOME, Activator.BUNDLE_ID);
		}
		
		return new CreateServerValidation(Status.OK_STATUS, null);
	}

	public String getJavaCompatibilityError() {
		IVMInstall vmi = new JBossVMRegistryDiscovery().findVMInstall(this);
		if( vmi == null ) {
			String msg = "Server {0} can not find a valid virtual machine to use.";
			return NLS.bind(msg, getServer().getId());
		}

		ServerExtendedProperties props = new ExtendedServerPropertiesAdapterFactory()
				.getExtendedProperties(getServer());
		if( !(props instanceof JBossExtendedProperties )) {
			String msg = "Server {0} experienced an internal error.";
			return NLS.bind(msg, getServer().getId());
		}

		JBossExtendedProperties props2 = (JBossExtendedProperties) props;
		String min = props2.getMinimumJavaVersionString();
		String max = props2.getMaximumJavaVersionString();
		String vmiVersion = vmi.getJavaVersion();
		if( !isJavaCompatible(vmiVersion, min, max)) {
			String msg = "Server {0} is not compatible with discovered java version {1}";
			return NLS.bind(msg, getServer().getId(), vmiVersion);
		}
		return null;
	}


	protected boolean isJavaCompatible(String vmiVersion, String min, String max) {
		return VersionComparisonUtility.isJavaCompatible(vmiVersion, min, max);
	}

	@Override
	public IStatus canStart(String launchMode) {
		if( !modesContains(launchMode)) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID,
					"Server may not be launched in mode " + launchMode);
		}
		String javaCompatError = getJavaCompatibilityError();
		if( javaCompatError != null ) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID,
					"Server can not be started: " + javaCompatError);
		}
		if( getServerRunState() == IServerDelegate.STATE_STOPPED ) {
			IStatus v = validate().getStatus();
			if( !v.isOK() )
				return v;
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}
	
	@Override
	public StartServerResponse start(String mode) {
		IStatus stat = canStart(mode);
		if( !stat.isOK()) {
			org.jboss.tools.rsp.api.dao.Status s = StatusConverter.convert(stat);
			return new StartServerResponse(s, null);
		}
		
		setMode(mode);
		setServerState(IServerDelegate.STATE_STARTING);
		
		CommandLineDetails launchedDetails = null;
		try {
			launchPoller(IServerStatePoller.SERVER_STATE.UP);
			IServerStartLauncher launcher = getStartLauncher();
			ILaunch startLaunch2 = launcher.launch(mode);
			launchedDetails = launcher.getLaunchedDetails();
			setStartLaunch(startLaunch2);
			registerLaunch(startLaunch2);
		} catch(CoreException ce) {
			if( getStartLaunch() != null ) {
				IProcess[] processes = getStartLaunch().getProcesses();
				for( int i = 0; i < processes.length; i++ ) {
					try {
						processes[i].terminate();
					} catch(DebugException de) {
						LOG.error(de.getMessage(), de);
					}
				}
			}
			setServerState(IServerDelegate.STATE_STOPPED);
			org.jboss.tools.rsp.api.dao.Status s = StatusConverter.convert(ce.getStatus());
			return new StartServerResponse(s, launchedDetails);
		}
		return new StartServerResponse(StatusConverter.convert(Status.OK_STATUS), launchedDetails);
	}

	
	@Override
	public IStatus stop(boolean force) {
		setServerState(IServerDelegate.STATE_STOPPING);
		ILaunch stopLaunch = null;
		launchPoller(IServerStatePoller.SERVER_STATE.DOWN);
		try {
			stopLaunch = getStopLauncher().launch(force);
			if( stopLaunch != null)
				registerLaunch(stopLaunch);
		} catch(CoreException ce) {
			// Dead code... but I feel it's not dead?  idk :( 
//			if( stopLaunch != null ) {
//				IProcess[] processes = startLaunch.getProcesses();
//				for( int i = 0; i < processes.length; i++ ) {
//					try {
//						processes[i].terminate();
//					} catch(DebugException de) {
//						LaunchingCore.log(de);
//					}
//				}
//			}
			setServerState(IServerDelegate.STATE_STARTED);
			return ce.getStatus();
		}
		return Status.OK_STATUS;

	}
	
	protected void launchPoller(IServerStatePoller.SERVER_STATE expectedState) {
		IPollResultListener listener = expectedState == IServerStatePoller.SERVER_STATE.DOWN ? 
				shutdownServerResultListener() : launchServerResultListener();
		IServerStatePoller poller = getPoller(expectedState);
		PollThreadUtils.pollServer(getServer(), expectedState, poller, listener);
	}
	
	/*
	 * Default implementation, subclasses can override.
	 */
	protected IServerStatePoller getPoller(IServerStatePoller.SERVER_STATE expectedState) {
		return getDefaultWebPortPoller();
	}
	
	private IServerStatePoller getDefaultWebPortPoller() {
		return new WebPortPoller("Web Poller: " + this.getServer().getName()) {
			@Override
			protected String getURL(IServer server) {
				return getPollURL(server);
			}
		};
	}

	@Override
	protected void processTerminated(IProcess p) {
		ILaunch l = p.getLaunch();
		if( l == getStartLaunch() ) {
			IProcess[] all = l.getProcesses();
			boolean allTerminated = true;
			for( int i = 0; i < all.length; i++ ) {
				allTerminated &= all[i].isTerminated();
			}
			if( allTerminated ) {
				setMode(null);
				setServerState(IServerDelegate.STATE_STOPPED);
				setStartLaunch(null);
			}
		}
		fireServerProcessTerminated(getProcessId(p));
	}

	protected ILaunch getStartLaunch() {
		return (ILaunch)getSharedData(START_LAUNCH_SHARED_DATA);
	}
	
	protected void setStartLaunch(ILaunch launch) {
		putSharedData(START_LAUNCH_SHARED_DATA, launch);
	}
	
	@Override
	public CommandLineDetails getStartLaunchCommand(String mode, ServerAttributes params) {
		try {
			return getStartLauncher().getLaunchCommand(mode);
		} catch(CoreException ce) {
			LOG.error(ce.getMessage(), ce);
			return null;
		}
	}

	@Override
	public IStatus clientSetServerStarting(ServerStartingAttributes attr) {
		setServerState(STATE_STARTING, true);
		if( attr.isInitiatePolling()) {
			launchPoller(IServerStatePoller.SERVER_STATE.UP);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus clientSetServerStarted(LaunchParameters attr) {
		setServerState(STATE_STARTED, true);
		return Status.OK_STATUS;
	}

	protected IJBossPublishController getOrCreatePublishController() {
		if( publishController == null ) {
			publishController = createPublishController();
		}
		return publishController;
	}
	
	protected abstract IJBossPublishController createPublishController();
	
	@Override
	public IStatus canAddDeployable(DeployableReference ref) {
		return getOrCreatePublishController().canAddDeployable(ref);
	}
	
	@Override
	public IStatus canRemoveDeployable(DeployableReference reference) {
		return getOrCreatePublishController().canRemoveDeployable(getServerPublishModel().fillOptionsFromCache(reference));
	}
	
	@Override
	public IStatus canPublish() {
		return getOrCreatePublishController().canPublish();
	}

	@Override
	protected void publishStart(int publishType) throws CoreException {
		getOrCreatePublishController().publishStart(publishType);
	}

	@Override
	protected void publishFinish(int publishType) throws CoreException {
		getOrCreatePublishController().publishFinish(publishType);
		super.publishFinish(publishType);
	}

	@Override
	protected void publishDeployable(DeployableReference reference, 
			int publishRequestType, int modulePublishState) throws CoreException {
		int syncState = getOrCreatePublishController()
				.publishModule(reference, publishRequestType, modulePublishState);
		setDeployablePublishState(reference, syncState);
		
		// TODO launch a module poller?!
		setDeployableState(reference, ServerManagementAPIConstants.STATE_STARTED);
	}

	@Override
	public Attributes listDeploymentOptions() {
		CreateServerAttributesUtility util = new CreateServerAttributesUtility();
		util.addAttribute(ServerManagementAPIConstants.DEPLOYMENT_OPTION_OUTPUT_NAME, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING,
				"Customize the output name including extension for this deployment. Example: sample.war (Leave blank for default)", null);
		return util.toPojo();
	}

	protected IStatus verifyUnchanged(IServer ds, IServer server, String[] unchangeable) {
		for( int i = 0; i < unchangeable.length; i++ ) {
			String dsType = ds.getAttribute(unchangeable[i], (String)null);
			String type = server.getAttribute(unchangeable[i], (String)null);
			if( !isEqual(dsType, type)) {
				return new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Field " + unchangeable[i] + " may not be changed");
			}
		}
		return Status.OK_STATUS;
	}

	public void updateServer(IServer dummyServer, UpdateServerResponse resp,
			String[] unchangeableFields) {

		// First, validate the changes
		IStatus stat = verifyUnchanged(dummyServer, getServer(), unchangeableFields);
		
		// We've already got errors? Return
		if( !stat.isOK()) {
			resp.getValidation().setStatus(StatusConverter.convert(stat));
			return;
		}
		
		// Do next level validation
		CreateServerValidation validation = validate(dummyServer);
		if( !validation.getStatus().isOK()) {
			resp.setValidation(validation.toDao());
		}
		
		// Now, perform any changes that need to be done at the delegate level
	}
	private boolean isEqual(String one, String two) {
		return one == null ? two == null : one.equals(two);
	}
}
