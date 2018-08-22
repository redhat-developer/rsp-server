/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.jboss.tools.ssp.api.SSPClient;
import org.jboss.tools.ssp.api.SocketLauncher;
import org.jboss.tools.ssp.api.dao.DiscoveryPath;
import org.jboss.tools.ssp.eclipse.core.runtime.CoreException;
import org.jboss.tools.ssp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.ssp.eclipse.jdt.launching.IVMInstallType;
import org.jboss.tools.ssp.launching.LaunchingCore;
import org.jboss.tools.ssp.server.core.internal.IMemento;
import org.jboss.tools.ssp.server.core.internal.XMLMemento;
import org.jboss.tools.ssp.server.model.ServerPersistenceManager;
import org.jboss.tools.ssp.server.spi.discovery.IDiscoveryPathModel;
import org.jboss.tools.ssp.server.spi.model.IServerManagementModel;
import org.jboss.tools.ssp.server.spi.servertype.IServer;

public class ServerManagementServerLauncher {
	
	private final ServerPersistenceManager persistenceEventManager;
	
	public static void main(String[] args) throws Exception {
		ServerManagementServerLauncher instance = new ServerManagementServerLauncher();
		instance.launch(args[0]);
		instance.shutdownOnInput();
	}

	public void shutdownOnInput() throws IOException {
		System.out.println("Enter any character to stop");
		System.in.read();
		shutdown();
	}

	protected ServerManagementServerImpl serverImpl;
	private ListenOnSocketRunnable socketRunnable;
	private ServerSocket serverSocket;
	public ServerManagementServerLauncher() {
		serverImpl = new ServerManagementServerImpl(this);
		this.persistenceEventManager = new ServerPersistenceManager(this);
	}
	
	public IServerManagementModel getModel() {
		return serverImpl.getModel();
	}
	
	public List<SSPClient> getClients() {
		return serverImpl.getClients();
	}
	
	public void launch(String portString) throws Exception {
		launch(Integer.parseInt(portString));
	}

	public void launch(int port) throws Exception {
		loadState();
		// create the chat server
		startListening(port, serverImpl);
	}
	
	private void loadState() {
		try {
			loadVMs();
			loadDiscoveryPaths();
			serverImpl.getModel().getServerModel().loadServers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadDiscoveryPaths() throws IOException {
		File discoveryPathFile = new File(LaunchingCore.getDataLocation(), "discovery-paths");
		if (!discoveryPathFile.exists()) {
			return;
		}
		Scanner scanner = new Scanner(discoveryPathFile);
		IDiscoveryPathModel discoveryPathModel = serverImpl.getModel().getDiscoveryPathModel();
		while (scanner.hasNextLine()) {
			String discoveryPathString = scanner.nextLine();
			if (StringUtils.isEmpty(discoveryPathString)) {
				continue;
			}
			discoveryPathModel.addPath(new DiscoveryPath(discoveryPathString));
		}
		scanner.close();
	}

	protected void startListening(int port, ServerManagementServerImpl server) throws IOException {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(port);
		// create the socket server
		try {
			socketRunnable = new ListenOnSocketRunnable(serverSocket, server);
			System.out.println("The server management server is running on port " + port);
			threadPool.submit(socketRunnable);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	private class ListenOnSocketRunnable implements Runnable {
		private ServerSocket serverSocket;
		private ServerManagementServerImpl server;
		private boolean listening = true;
		public ListenOnSocketRunnable(ServerSocket serverSocket, ServerManagementServerImpl server) {
			this.server = server;
			this.serverSocket = serverSocket;
		}
		@Override
		public void run() {
			while (isListening()) {
				oneSocket(serverSocket, server);
			}
		}
		
		public void stopListening() {
			setListening(false);
		}
		
		private synchronized boolean isListening() {
			return listening;
		}
		private synchronized void setListening(boolean listening) {
			this.listening = listening;
		}
		
	}
	
	private void oneSocket(ServerSocket serverSocket, ServerManagementServerImpl server) {
		try {
			// wait for clients to connect
			Socket socket = serverSocket.accept();
			// create a JSON-RPC connection for the accepted socket
			SocketLauncher<SSPClient> launcher = new SocketLauncher<>(server,
					SSPClient.class, socket);
			// connect a remote client proxy to the server
			Runnable removeClient = server.addClient(launcher);
			/*
			 * Start listening for incoming messages. When the JSON-RPC connection is closed
			 * disconnect the remote client from the server.
			 */
			launcher.startListening().thenRun(removeClient);
			System.out.println(
					"Client " + socket.getInetAddress().getCanonicalHostName() +
							":"+ socket.getPort() + " is connected");
		} catch(IOException ioe) {
			// We shouldn't fail if we're still supposed to be listening
			if (socketRunnable != null && socketRunnable.isListening())
				ioe.printStackTrace();
		}

	}

	public void shutdown() {
		saveState();
		closeAllConnections();
		socketRunnable.stopListening();
		try {
			serverSocket.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		ShutdownExecutor.getExecutor().shutdown();
	}
	
	public void saveState() {
		try {
			saveDiscoveryPaths();
			saveVMs();
			saveServers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveServers() throws CoreException {
		for (IServer server : serverImpl.getModel().getServerModel().getServers().values()) {
			server.save(new NullProgressMonitor());
		}
	}
	
	public void saveDiscoveryPaths() throws IOException {
		File discoveryPathFile = new File(LaunchingCore.getDataLocation(), "discovery-paths");
		if (!discoveryPathFile.exists()) {
			discoveryPathFile.createNewFile();
		}
		PrintWriter pw = new PrintWriter(discoveryPathFile);
		serverImpl.getModel().getDiscoveryPathModel().getPaths()
			.forEach(path -> pw.println(path.getFilepath()));
		pw.close();
	}
	
	public void saveVMs() throws IOException {
		File vmsFile = new File(LaunchingCore.getDataLocation(), "vms");
		if (!vmsFile.exists()) {
			vmsFile.createNewFile();
		}
		XMLMemento memento = XMLMemento.createWriteRoot("vms");
		for (IVMInstall vmInstall : serverImpl.getModel().getVMInstallModel().getVMs()) {
			IMemento vmMemento = memento.createChild("vm");
			vmMemento.putString("id", vmInstall.getId());
			vmMemento.putString("installLocation", vmInstall.getInstallLocation().getAbsolutePath());
			vmMemento.putString("type", vmInstall.getVMInstallType().getClass().getName());			
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		memento.save(out);
		byte[] bytes = out.toByteArray();
		Files.write(vmsFile.toPath(), bytes);
	}
	
	public void loadVMs() throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException {
		File vmsFile = new File(LaunchingCore.getDataLocation(), "vms");
		if (!vmsFile.exists()) {
			return;
		}
		IMemento vmsMemento = XMLMemento.loadMemento(new FileInputStream(vmsFile));
		for (IMemento vmMemento : vmsMemento.getChildren()) {
			String id = vmMemento.getString("id");
			if (serverImpl.getModel().getVMInstallModel().findVMInstall(id) != null) {
				continue;
			}
			String installLocation = vmMemento.getString("installLocation");
			String type = vmMemento.getString("type");
			
			@SuppressWarnings("unchecked")
			Class<IVMInstallType> typeClass = (Class<IVMInstallType>)Class.forName(type).asSubclass(IVMInstallType.class);
			IVMInstallType vmType = null;

			try {
				vmType = (IVMInstallType)typeClass.getMethod("getDefault").invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				vmType = typeClass.newInstance();
			}
			IVMInstall newVM = vmType.createVMInstall(id);
			newVM.setInstallLocation(new File(installLocation));
			serverImpl.getModel().getVMInstallModel().addVMInstall(newVM);
		}
		
	}
	
	

	private void closeAllConnections() {
		List<SocketLauncher<SSPClient>> all = 
				serverImpl.getActiveLaunchers();
		for( SocketLauncher<SSPClient> i : all ) {
			i.close();
		}
	}

}
