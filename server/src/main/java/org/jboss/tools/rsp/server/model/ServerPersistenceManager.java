/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallChangedListener;
import org.jboss.tools.rsp.eclipse.jdt.launching.PropertyChangeEvent;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.server.ServerManagementServerLauncher;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathListener;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathModel;

public class ServerPersistenceManager implements IDiscoveryPathListener, IVMInstallChangedListener {

	private ServerManagementServerLauncher serverLauncher;
	public ServerPersistenceManager(ServerManagementServerLauncher serverLauncher) {
		this.serverLauncher = serverLauncher; 
		serverLauncher.getModel().getDiscoveryPathModel().addListener(this);
		serverLauncher.getModel().getVMInstallModel().addListener(this);
	}
	
	@Override
	public void discoveryPathAdded(DiscoveryPath path) {
		persisteDiscoveryPaths();
	}
	@Override
	public void discoveryPathRemoved(DiscoveryPath path) {
		persisteDiscoveryPaths();
	}
	@Override
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		persistVms();
	}
	@Override
	public void vmChanged(PropertyChangeEvent event) {
		persistVms();
	}
	@Override
	public void vmAdded(IVMInstall vm) {
		persistVms();
	}
	@Override
	public void vmRemoved(IVMInstall vm) {
		persistVms();
	}
	
	private void persistVms() {
		try {
			saveVMs();
		} catch (IOException e) {
			LaunchingCore.log(e);
		}
	}
	
	private void persisteDiscoveryPaths() {
		try {
			saveDiscoveryPaths();
		} catch (IOException e) {
			LaunchingCore.log(e);
		}
	}
	

	public void saveState() {
		try {
			saveDiscoveryPaths();
			saveVMs();
			serverLauncher.getModel().getServerModel().saveServers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadState() {
		try {
			loadVMs();
			loadDiscoveryPaths();
			serverLauncher.getModel().getServerModel().loadServers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadDiscoveryPaths() throws IOException {
		File discoveryPathFile = new File(LaunchingCore.getDataLocation(), "discovery-paths");
		IDiscoveryPathModel discoveryPathModel = serverLauncher.getModel().getDiscoveryPathModel();
		discoveryPathModel.loadDiscoveryPaths(discoveryPathFile);
	}

	public void saveDiscoveryPaths() throws IOException {
		File discoveryPathFile = new File(LaunchingCore.getDataLocation(), "discovery-paths");
		IDiscoveryPathModel discoveryPathModel = serverLauncher.getModel().getDiscoveryPathModel();
		discoveryPathModel.saveDiscoveryPaths(discoveryPathFile);
	}
	
	public void saveVMs() throws IOException {
		File vmsFile = new File(LaunchingCore.getDataLocation(), "vms");
		serverLauncher.getModel().getVMInstallModel().save(vmsFile);
	}
	
	public void loadVMs() throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException {
		File vmsFile = new File(LaunchingCore.getDataLocation(), "vms");
		serverLauncher.getModel().getVMInstallModel().load(vmsFile);
	}
}
