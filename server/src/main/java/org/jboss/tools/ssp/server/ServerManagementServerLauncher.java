package org.jboss.tools.ssp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.tools.ssp.api.ServerManagementClient;
import org.jboss.tools.ssp.api.SocketLauncher;

public class ServerManagementServerLauncher {
	protected static ServerManagementServerLauncher instance;

	public static void main(String[] args) throws Exception {
		instance = new ServerManagementServerLauncher();
		instance.launch(args[0]);
	}

	public static ServerManagementServerLauncher getDefault() {
		return instance;
	}

	private ServerManagementServerImpl serverImpl;
	public ServerManagementServerLauncher() {
		serverImpl = new ServerManagementServerImpl();
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
					} catch (IOException ioe) {
						// Log it
					}
				}
			});
			System.out.println("Enter any character to stop");
			System.in.read();
			System.exit(0);
		}
	}

	public void shutdown() {
		closeAllConnections();
		saveAllModels();
		System.exit(0);
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
