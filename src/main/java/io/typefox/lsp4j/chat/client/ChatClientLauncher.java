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
		ChatClientImpl chatClient = new ChatClientImpl();

		String host = args[0];
		Integer port = Integer.valueOf(args[1]);
		try (Socket socket = new Socket(host, port)) {
			SocketLauncher<ChatServer> launcher = new SocketLauncher<>(chatClient, ChatServer.class, socket);
			launcher.startListening().thenRun(() -> System.exit(0));
			chatClient.start(launcher.getRemoteProxy());
		}
	}

}
