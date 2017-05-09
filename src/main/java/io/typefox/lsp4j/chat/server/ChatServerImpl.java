/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package io.typefox.lsp4j.chat.server;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import io.typefox.lsp4j.chat.shared.ChatClient;
import io.typefox.lsp4j.chat.shared.ChatServer;
import io.typefox.lsp4j.chat.shared.UserMessage;

public class ChatServerImpl implements ChatServer {
	
	private final List<UserMessage> messages = new CopyOnWriteArrayList<>();
	private final List<ChatClient> clients = new CopyOnWriteArrayList<>();

	public CompletableFuture<List<UserMessage>> fetchMessages() {
		return CompletableFuture.completedFuture(messages);
	}

	public void postMessage(UserMessage message) {
		messages.add(message);
		for (ChatClient client : clients) {
			client.didPostMessage(message);
		}
	}

	public Runnable addClient(ChatClient client) {
		this.clients.add(client);
		return () -> this.clients.remove(client);
	}

}
