/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package org.jboss.tools.ssp.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.tools.ssp.chat.shared.ChatClient;
import org.jboss.tools.ssp.chat.shared.SocketLauncher;

public class ChatServerLauncher {

	public static void main(String[] args) throws Exception {
		// create the chat server
		ChatServerImpl chatServer = new ChatServerImpl();
		ExecutorService threadPool = Executors.newCachedThreadPool();

		Integer port = Integer.valueOf(args[0]);
		// create the socket server
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("The chat server is running on port " + port);
			threadPool.submit( (Runnable) () -> {
				while (true) {
					try {
					// wait for clients to connect
					Socket socket = serverSocket.accept();
					// create a JSON-RPC connection for the accepted socket
					SocketLauncher<ChatClient> launcher = new SocketLauncher<>(chatServer, ChatClient.class, socket);
					// connect a remote chat client proxy to the chat server
					Runnable removeClient = chatServer.addClient(launcher.getRemoteProxy());
                    /*
                     * Start listening for incoming messages.
                     * When the JSON-RPC connection is closed
                     * disconnect the remote client from the chat server.
                     */
					launcher.startListening().thenRun(removeClient);
					} catch(IOException ioe) {
						// Log it
					}
				}
			});
			System.out.println("Enter any character to stop");
			System.in.read();
			System.exit(0);
		}
	}

}
