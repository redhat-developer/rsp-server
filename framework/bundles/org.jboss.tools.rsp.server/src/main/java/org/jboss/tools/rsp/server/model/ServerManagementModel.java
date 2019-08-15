/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMInstallRegistry;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesModel;
import org.jboss.tools.rsp.secure.model.ISecureStorageProvider;
import org.jboss.tools.rsp.server.CapabilityManagement;
import org.jboss.tools.rsp.server.discovery.DiscoveryPathModel;
import org.jboss.tools.rsp.server.discovery.serverbeans.ServerBeanTypeManager;
import org.jboss.tools.rsp.server.filewatcher.FileWatcherService;
import org.jboss.tools.rsp.server.jobs.JobManager;
import org.jboss.tools.rsp.server.secure.SecureStorageGuardian;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathModel;
import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeManager;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherService;
import org.jboss.tools.rsp.server.spi.jobs.IJobManager;
import org.jboss.tools.rsp.server.spi.model.ICapabilityManagement;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.IServerModel;
import org.jboss.tools.rsp.server.spi.model.IServerModelListener;
import org.jboss.tools.rsp.server.spi.model.ServerModelListenerAdapter;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerManagementModel implements IServerManagementModel {
	private static final Logger LOG = LoggerFactory.getLogger(ServerManagementModel.class);

	private static final String SECURESTORAGE_DIRECTORY = "securestorage";

	private ISecureStorageProvider secureStorage;
	private ICapabilityManagement capabilities;

	private IDiscoveryPathModel rpm;
	private IServerBeanTypeManager serverBeanTypeManager;
	private IServerModel serverModel;
	private IVMInstallRegistry vmModel;
	private IFileWatcherService fileWatcherService;
	private IDownloadRuntimesModel downloadRuntimeModel;
	private IJobManager jobManager;

	public ServerManagementModel() {
		this(LaunchingCore.getDataLocation());
	}
	
	/** protected for testing purposes **/
	public ServerManagementModel(File dataLocation) {
		this.capabilities = createCapabilityManagement();
		this.secureStorage = createSecureStorageProvider(getSecureStorageFile(dataLocation), capabilities);
		this.rpm = createDiscoveryPathModel();
		this.serverBeanTypeManager = createServerBeanTypeManager();
		this.serverModel = createServerModel();
		this.vmModel = createVMInstallRegistry();
		this.vmModel.addActiveVM();
		this.fileWatcherService = createFileWatcherService();
		this.fileWatcherService.start();
		this.downloadRuntimeModel = createDownloadRuntimesModel();
		this.jobManager = createJobManager();
	}
	
	@Override
	public IJobManager getJobManager() {
		return this.jobManager;
	}
	
	@Override
	public ISecureStorageProvider getSecureStorageProvider() {
		return secureStorage;
	}

	@Override
	public IDiscoveryPathModel getDiscoveryPathModel() {
		return rpm;
	}

	@Override
	public IServerBeanTypeManager getServerBeanTypeManager() {
		return serverBeanTypeManager;
	}

	@Override
	public IServerModel getServerModel() {
		return serverModel;
	}

	@Override
	public IVMInstallRegistry getVMInstallModel() {
		return vmModel;
	}

	@Override
	public ICapabilityManagement getCapabilityManagement() {
		return capabilities;
	}

	@Override
	public IFileWatcherService getFileWatcherService() {
		return fileWatcherService;
	}

	@Override
	public IDownloadRuntimesModel getDownloadRuntimeModel() {
		return downloadRuntimeModel;
	}
	
	@Override
	public void clientRemoved(RSPClient client) {
		capabilities.clientRemoved(client);
		if( secureStorage instanceof SecureStorageGuardian ) 
			((SecureStorageGuardian)secureStorage).removeClient(client);
	}

	public void clientAdded(RSPClient client) {
		capabilities.clientAdded(client);
	}

	private File getSecureStorageFile(File dataLocation) {
		File secure = new File(dataLocation, SECURESTORAGE_DIRECTORY);
		return secure;
	}

	/*
	 * Following methods are for tests / subclasses to override. This is not advised
	 * for clients / extenders, since this class appears to behave as a singleton.
	 */
	protected ISecureStorageProvider createSecureStorageProvider(File file, ICapabilityManagement mgmt) {
		return new SecureStorageGuardian(file, mgmt);
	}

	protected ICapabilityManagement createCapabilityManagement() {
		return new CapabilityManagement();
	}

	protected IDiscoveryPathModel createDiscoveryPathModel() {
		return new DiscoveryPathModel();
	}

	protected VMInstallRegistry createVMInstallRegistry() {
		return new VMInstallRegistry();
	}

	protected IServerModel createServerModel() {
		return new ServerModel(this);
	}

	protected IServerBeanTypeManager createServerBeanTypeManager() {
		return new ServerBeanTypeManager();
	}

	protected IDownloadRuntimesModel createDownloadRuntimesModel() {
		return RuntimeCoreActivator.createDownloadRuntimesModel();
	}

	protected IFileWatcherService createFileWatcherService() {
		return new FileWatcherService();
	}

	protected IJobManager createJobManager() {
		return new JobManager();
	}

	@Override
	public void dispose() {
		shutdownAllServers();
		if( this.jobManager != null ) {
			this.jobManager.shutdown();
		}
	}

	protected void shutdownAllServers() {
		if( getNotStoppedServers().isEmpty())
			return;
		
		shutdownServers(getStartedServers(), false);
		shutdownServers(getNotStoppedServers(), true);
	}
	
	protected void shutdownServers(List<IServer> list, boolean force) {
		if( list.isEmpty()) 
			return;
		
		ExecutorService threadExecutor = Executors.newFixedThreadPool(list.size());
		CountDownLatch latch = new CountDownLatch(list.size());
		Iterator<IServer> it = list.iterator();
		while(it.hasNext()) {
			final IServer next = it.next();
			submitShutdownServerRequest(next, force, threadExecutor, latch);
			try {
				boolean result = latch.await(60000, TimeUnit.MILLISECONDS);
				if( !result ) {
					LOG.error("Waiting too long for shutdown of servers during RSP shutdown");
				}
			} catch(InterruptedException ie) {
				// Ignore, do not set interrupt state again
			}
		}
		threadExecutor.shutdown();
	}
	
	private void submitShutdownServerRequest(IServer next, boolean force, 
			ExecutorService threadExecutor, CountDownLatch latch) {
		threadExecutor.submit(() -> {
			IServerModelListener l = new ServerModelListenerAdapter() {
				@Override
				public void serverStateChanged(ServerHandle server, ServerState state) {
					if( server.getId().equals(next.getId()) && state.getState() == ServerManagementAPIConstants.STATE_STOPPED) {
						serverModel.removeServerModelListener(this);
						latch.countDown();
					}
				}
			};
			serverModel.addServerModelListener(l);
			next.getDelegate().stop(force);
		});
	}
	
	
	private List<IServer> getStartedServers() {
		return serverModel.getServers().values().stream()
			.filter(s -> s.getDelegate().getServerRunState() == ServerManagementAPIConstants.STATE_STARTED)
			.collect(Collectors.toList());
	}
	private List<IServer> getNotStoppedServers() {
		return serverModel.getServers().values().stream()
				.filter(s -> s.getDelegate().getServerRunState() != ServerManagementAPIConstants.STATE_STOPPED)
				.collect(Collectors.toList());
	}
}
