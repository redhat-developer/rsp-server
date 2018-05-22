/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.tools.ssp.api.ServerManagementClient;
import org.jboss.tools.ssp.api.SocketLauncher;
import org.jboss.tools.ssp.server.spi.model.IServerManagementModel;

public class ServerManagementServerLauncher {
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
	public ServerManagementServerLauncher() {
		serverImpl = new ServerManagementServerImpl(this);
	}
	
	public IServerManagementModel getModel() {
		return serverImpl.getModel();
	}
	
	public List<ServerManagementClient> getClients() {
		return serverImpl.getClients();
	}
	
	public void launch(String portString) throws Exception {
		launch(Integer.parseInt(portString));
	}

	public void launch(int port) throws Exception {
		// create the chat server
		startListening(port, serverImpl);
	}

	protected void startListening(int port, ServerManagementServerImpl server) throws IOException {
		ExecutorService threadPool = Executors.newCachedThreadPool();

		// create the socket server
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("The server management server is running on port " + port);
			threadPool.submit((Runnable) () -> {
				while (true) {
					oneSocket(serverSocket, server);
				}
			});
			shutdownOnInput();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	private void oneSocket(ServerSocket serverSocket, ServerManagementServerImpl server) {
		try {

			// wait for clients to connect
			Socket socket = serverSocket.accept();
			// create a JSON-RPC connection for the accepted socket
			SocketLauncher<ServerManagementClient> launcher = new SocketLauncher<>(server,
					ServerManagementClient.class, socket);
			// connect a remote client proxy to the server
			Runnable removeClient = server.addClient(launcher);
			/*
			 * Start listening for incoming messages. When the JSON-RPC connection is closed
			 * disconnect the remote client from the chat server.
			 */
			launcher.startListening().thenRun(removeClient);
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public void shutdown() {
		closeAllConnections();
		saveAllModels();
		ShutdownExecutor.getExecutor().shutdown();
	}
	
	private void saveAllModels() {
		// TODO Auto-generated method stub
		
	}

	private void closeAllConnections() {
		List<SocketLauncher<ServerManagementClient>> all = 
				serverImpl.getActiveLaunchers();
		for( SocketLauncher<ServerManagementClient> i : all ) {
			i.close();
		}
	}

}
