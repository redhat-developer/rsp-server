/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package io.typefox.lsp4j.chat.client;

import java.net.Socket;

import io.typefox.lsp4j.chat.shared.ChatServer;
import io.typefox.lsp4j.chat.shared.SocketLauncher;

public class ChatClientLauncher {

	public static void main(String[] args) throws Exception {
		// create the chat client
		ChatClientImpl chatClient = new ChatClientImpl();

		String host = args[0];
		Integer port = Integer.valueOf(args[1]);
		// connect to the server
		try (Socket socket = new Socket(host, port)) {
			// open a JSON-RPC connection for the opened socket
			SocketLauncher<ChatServer> launcher = new SocketLauncher<>(chatClient, ChatServer.class, socket);
			/*
             * Start listening for incoming message.
             * When the JSON-RPC connection is closed, 
             * e.g. the server is died, 
             * the client process should exit.
             */
			launcher.startListening().thenRun(() -> System.exit(0));
			// start the chat session with a remote chat server proxy
			chatClient.start(launcher.getRemoteProxy());
		}
	}

}
