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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.MultiStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.SERVER_STATE;
import org.jboss.tools.rsp.server.spi.model.polling.PollThreadUtils;
import org.jboss.tools.rsp.server.spi.model.polling.WebPortPoller;
import org.jboss.tools.rsp.server.spi.publishing.IPublishController;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.spi.util.VersionComparisonUtility;
import org.jboss.tools.rsp.server.wildfly.impl.Activator;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.ExtendedServerPropertiesAdapterFactory;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.JBossExtendedProperties;
import org.jboss.tools.rsp.server.wildfly.servertype.capabilities.ServerExtendedProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJBossServerDelegate extends AbstractServerDelegate {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractJBossServerDelegate.class);
	public static final String START_LAUNCH_SHARED_DATA = "AbstractJBossServerDelegate.startLaunch";
	public static final String STOP_LAUNCH_SHARED_DATA = "AbstractJBossServerDelegate.stopLaunch";
	
	private IPublishController publishController;
	
	public AbstractJBossServerDelegate(IServer server) {
		super(server);
	}

	protected IServerStartLauncher getStartLauncher() {
		return getStartLauncher(getServer());
	}
	protected abstract IServerStartLauncher getStartLauncher(IServer delegate);
	
	protected abstract IServerShutdownLauncher getStopLauncher();

	/**
	 * Discover the server state by actually checking 
	 * whatever mechanism should be used, and not just 
	 * returning cached values. 
	 */
	public void discoverServerState() {
		SERVER_STATE ss = PollThreadUtils.isServerStarted(getServer(), 
				getPoller(IServerStatePoller.SERVER_STATE.UP));
		if( ss == SERVER_STATE.UP ) {
			setServerState(ServerManagementAPIConstants.STATE_STARTED);
		} else if( ss == SERVER_STATE.DOWN) {
			setServerState(ServerManagementAPIConstants.STATE_STOPPED);
		} else {
			setServerState(ServerManagementAPIConstants.STATE_UNKNOWN);
		}
	}


	public String getPollURL(IServer server) {
		String host = server.getAttribute(IJBossServerAttributes.JBOSS_SERVER_HOST, 
				IJBossServerAttributes.JBOSS_SERVER_HOST_DEFAULT);
		int port = server.getAttribute(IJBossServerAttributes.JBOSS_SERVER_PORT, 
				IJBossServerAttributes.JBOSS_SERVER_PORT_DEFAULT);
		String url = NLS.bind("http://{0}:{1}", host, port);
		return url;
	}
	
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
			String removeLastDotMin = min == null ? null : min.endsWith(".") ? min.substring(0,min.length() - 1) : min;
			String removeLastDotMax = max == null ? null : max.endsWith(".") ? max.substring(0,max.length() - 1) : max;
			
			StringBuilder sb = new StringBuilder();
			
			String notCompat = "Server {0} is not compatible with discovered java version {1}. ";
			String notCompat2 = NLS.bind(notCompat, getServer().getId(), vmiVersion);
			sb.append(notCompat2);
			
			if( min == null ) {
				sb.append("This server requires a java version <= ");
				sb.append(removeLastDotMax);
				sb.append(".");
			} else if( max == null ) {
				sb.append("This server requires a java version >= ");
				sb.append(removeLastDotMin);
				sb.append(".");
			} else {
				sb.append("This server requires a java version between ");
				sb.append(removeLastDotMin);
				sb.append(" and ");
				sb.append(removeLastDotMax);
				sb.append(" inclusive. ");
			}

			sb.append("You may change a server's launch vm by setting the 'vm.install.path' property in a server's rsp configuration file.");
			return sb.toString();
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
		if( !force )
			launchPoller(IServerStatePoller.SERVER_STATE.DOWN);
		try {
			stopLaunch = getStopLauncher().launch(force);
			if( stopLaunch != null)
				registerLaunch(stopLaunch);
		} catch(CoreException ce) {
			if( !force )
				setServerState(IServerDelegate.STATE_STARTED);
			return ce.getStatus();
		} finally {
			if( force ) {
				setServerState(IServerDelegate.STATE_STOPPED);
			}
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
				setStartLaunch(null);
				setServerState(IServerDelegate.STATE_STOPPED);
			}
		}
		fireServerProcessTerminated(getProcessId(p));
	}
	
	@Override
	protected void setServerState(int state, boolean fire) {
		if( state == ServerManagementAPIConstants.STATE_STARTED) {
			pollDeploymentsForState(ServerManagementAPIConstants.STATE_STARTED);
		} else if( state == ServerManagementAPIConstants.STATE_STOPPED) {
			pollDeploymentsForState(ServerManagementAPIConstants.STATE_STOPPED);
		}
		super.setServerState(state, fire);
	}
	
	protected void pollDeploymentsForState(int state) {
		// For now, don't poll. Just set all to started. 
		for( DeployableState ds : getServerPublishModel().getDeployableStates() ) {
			getServerPublishModel().setDeployableState(ds.getReference(), state);
		}
	}
	
	protected ILaunch getStartLaunch() {
		return (ILaunch)getSharedData(START_LAUNCH_SHARED_DATA);
	}
	
	protected void setStartLaunch(ILaunch launch) {
		putSharedData(START_LAUNCH_SHARED_DATA, launch);
	}

	protected ILaunch getStopLaunch() {
		return (ILaunch)getSharedData(STOP_LAUNCH_SHARED_DATA);
	}
	
	protected void setStopLaunch(ILaunch launch) {
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

	protected IPublishController getOrCreatePublishController() {
		if( publishController == null ) {
			publishController = createPublishController();
		}
		return publishController;
	}
	
	protected abstract IPublishController createPublishController();
	
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
		boolean serverStarted = getServerState().getState() == ServerManagementAPIConstants.STATE_STARTED;
		int deployState = (serverStarted ? ServerManagementAPIConstants.STATE_STARTED : ServerManagementAPIConstants.STATE_STOPPED);
		setDeployableState(reference, deployState);
	}

	@Override
	public Attributes listDeploymentOptions() {
		CreateServerAttributesUtility util = new CreateServerAttributesUtility();
		util.addAttribute(ServerManagementAPIConstants.DEPLOYMENT_OPTION_OUTPUT_NAME, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING,
				"Customize the output name including extension for this deployment. Example: sample.war (Leave blank for default)", null);
		util.addAttribute(ServerManagementAPIConstants.DEPLOYMENT_OPTION_ASSEMBLY_FILE, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING,
				"Optionally set a deployment assembly file for more control. See documentation for details. (Leave blank for auto-detection of '.rsp/rsp.assembly.json' or default behavior.)", null);
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
		if( preUpdateServerValidationErrors(dummyServer, resp, unchangeableFields)) {
			return;
		}
		MultiStatus ret = new MultiStatus(ServerCoreActivator.BUNDLE_ID, 0, 
				NLS.bind("Updating Server {0}...", getServer().getName()), null);

		
		updateServerModules(dummyServer, ret);
		updateDependentAttributes(dummyServer, ret);
		
		if( !ret.isOK()) {
			resp.getValidation().setStatus(StatusConverter.convert(ret));
		}
	}
	
	public void updateDependentAttributes(IServer dummyServer, MultiStatus ret) {
		// Our start launcher is currently saving default vm and prog args 
		// which is weird but whatever, it's what we got
		try {
			getStartLauncher(dummyServer).getLaunchCommand("run");
		} catch(CoreException ce) {
			LOG.error(ce.getMessage(), ce);
		}
	}

	private void updateServerModules(IServer dummyServer, MultiStatus ret) {
		// Now, perform any changes that need to be done at the delegate level
		List<DeployableState> existing = getServerPublishModel().getDeployableStates();
		List<DeployableState> updated = dummyServer.getDelegate().getServerPublishModel().getDeployableStates();
		
		List<DeployableReference> existingRefs = existing.stream().map(s -> s.getReference()).collect(Collectors.toList());
		List<DeployableReference> updatedRefs = updated.stream().map(s -> s.getReference()).collect(Collectors.toList());
		
		for( DeployableReference ds : existingRefs ) {
			getServerPublishModel().fillOptionsFromCache(ds);
		}

		for( DeployableReference ds : updatedRefs ) {
			dummyServer.getDelegate().getServerPublishModel().fillOptionsFromCache(ds);
		}

		// Calculate the delta on modules?
		List<DeployableReference> unchanged = new ArrayList<>();
		List<DeployableReference> removed = new ArrayList<>();
		for( DeployableReference ds : existingRefs ) {
			DeployableState matching = findExactMatch(ds, updated);
			if( matching == null ) {
				removed.add(ds);
			} else {
				unchanged.add(ds);
				updatedRefs.remove(ds);
			}
		}

		// Handle removals
		for( DeployableReference removed1 : removed ) {
			ret.add(getServerPublishModel().removeDeployable(removed1));
		}
		// Whatever is left is 'added'
		for( DeployableReference ds : updatedRefs ) {
			ret.add(getServerPublishModel().addDeployable(ds));
		}
	}

	private boolean preUpdateServerValidationErrors(
			IServer dummyServer, UpdateServerResponse resp,
			String[] unchangeableFields) {
		
		// First, validate the changes
		IStatus stat = verifyUnchanged(dummyServer, getServer(), unchangeableFields);
		
		// We've already got errors? Return
		if( !stat.isOK()) {
			resp.getValidation().setStatus(StatusConverter.convert(stat));
			return true;
		}
		
		// Do next level validation
		CreateServerValidation validation = validate(dummyServer);
		if( !validation.getStatus().isOK()) {
			resp.setValidation(validation.toDao());
			return true;
		}
		
		stat = verifyDeploymentChanges(dummyServer, getServer());
		if( !stat.isOK()) {
			resp.getValidation().setStatus(StatusConverter.convert(stat));
			return true;
		}
		return false;
	}
	private DeployableState findExactMatch(DeployableReference reference, List<DeployableState> updated) {
		String label = reference.getLabel();
		String path = reference.getPath();
		for( DeployableState ds : updated) {
			if( !ds.getReference().getLabel().equals(label))
				continue;
			if( !ds.getReference().getPath().equals(path))
				continue;
			
			Map<String, Object> origOptions = reference.getOptions();
			Map<String, Object> updatedOptions = ds.getReference().getOptions();
			if( origOptions == null && updatedOptions == null )
				return ds;
			if( origOptions == null )
				continue;
			if( origOptions.equals(updatedOptions)) 
				return ds;
		}
		return null;
	}

	private IStatus verifyDeploymentChanges(IServer dummyServer, IServer server) {
		return Status.OK_STATUS;
	}

	private boolean isEqual(String one, String two) {
		return one == null ? two == null : one.equals(two);
	}
	
	@Override
	public void setDefaults(IServerWorkingCopy server) {
		super.setDefaults(server);
		server.setAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_BOOLEAN, false);
		server.setAttribute(IJBossServerAttributes.WILDFLY_PUBLISH_RESTART_PATTERN_KEY, 
				IJBossServerAttributes.WILDFLY_PUBLISH_RESTART_PATTERN_DEFAULT);
	}

	
	@Override
	public void setDependentDefaults(IServerWorkingCopy server) {
		new JBossVMRegistryDiscovery().ensureVMInstallAdded(server);
	}

}
