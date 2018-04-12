package org.jboss.tools.ssp.client.bindings;

import java.io.IOException;
import java.net.Socket;

import org.jboss.tools.ssp.api.ServerManagementServer;
import org.jboss.tools.ssp.api.SocketLauncher;

public class ServerManagementClientLauncher {

	public static void main(String[] args) throws Exception {
		// todo verify args
		new ServerManagementClientLauncher(args[0], Integer.parseInt(args[1])).launch();
	}
	
	
	private ServerManagementClientImpl myClient;
	private Socket socket;
	private String host;
	private int port;
	private boolean connectionOpen = false;
	public ServerManagementClientLauncher(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void launch() throws Exception {
		// create the chat client
		ServerManagementClientImpl client = new ServerManagementClientImpl();
		// connect to the server
		socket = new Socket(host, port);
		// open a JSON-RPC connection for the opened socket
		SocketLauncher<ServerManagementServer> launcher = new SocketLauncher<>(client, ServerManagementServer.class, socket);
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
		if( socket != null ) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ServerManagementClientImpl getClient() {
		return this.myClient;
	}
	
	public boolean isConnectionActive() {
		return connectionOpen;
	}
	
	public ServerManagementServer getServerProxy() {
		if( myClient != null ) {
			return myClient.getProxy();
		}
		return null;
	}
}
