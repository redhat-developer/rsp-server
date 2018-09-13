/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.util;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.SocketLauncher;

public class ClientLauncher {

	private ClientImpl myClient;
	private SocketLauncher<RSPServer> launcher;
	private Socket socket;
	private String host;
	private int port;
	private boolean connectionOpen = false;
	public ClientLauncher(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void launch() throws Exception {
		// create the chat client
		ClientImpl client = new ClientImpl();
		// connect to the server
		this.socket = new Socket(host, port);
		// open a JSON-RPC connection for the opened socket
		this.launcher = new SocketLauncher<>(client, RSPServer.class, socket);
		/*
         * Start listening for incoming message.
         * When the JSON-RPC connection is closed, 
         * e.g. the server is died, 
         * the client process should exit.
         */
		launcher.startListening().thenRun(() -> clientClosed());
		// start the chat session with a remote chat server proxy
		client.initialize(launcher.getRemoteProxy());
		myClient = client;
		connectionOpen = true;
	}

	private void clientClosed() {
		this.myClient = null;
		connectionOpen = false;
	}
	
	public void closeConnection() {
		if( launcher != null ) {
			launcher.close();
		}
	}
	
	public ClientImpl getClient() {
		return this.myClient;
	}
	
	public SocketLauncher<RSPServer> getLauncher() {
		return launcher;
	}
	
	public boolean isConnectionActive() {
		return connectionOpen;
	}
	
	public RSPServer getServerProxy() {
		if( myClient != null ) {
			return myClient.getProxy();
		}
		return null;
	}
	
	public Future<Void> getStartListeningResult() {
		return launcher.getStartListeningResult();
	}
}
