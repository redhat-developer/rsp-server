/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.SocketLauncher;
import org.jboss.tools.rsp.server.model.ServerPersistenceManager;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;

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
	
	public List<RSPClient> getClients() {
		return serverImpl.getClients();
	}
	
	public void launch(String portString) throws Exception {
		launch(Integer.parseInt(portString));
	}

	public void launch(int port) throws Exception {
		persistenceEventManager.loadState();
		// create the chat server
		startListening(port, serverImpl);
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
			SocketLauncher<RSPClient> launcher = new SocketLauncher<>(server,
					RSPClient.class, socket);
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
		persistenceEventManager.saveState();
		closeAllConnections();
		socketRunnable.stopListening();
		try {
			serverSocket.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		ShutdownExecutor.getExecutor().shutdown();
	}
	

	private void closeAllConnections() {
		List<SocketLauncher<RSPClient>> all = 
				serverImpl.getActiveLaunchers();
		for( SocketLauncher<RSPClient> i : all ) {
			i.close();
		}
	}

}
