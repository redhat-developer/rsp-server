package io.typefox.lsp4j.chat.typed.server;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import io.typefox.lsp4j.chat.typed.shared.ChatClient;
import io.typefox.lsp4j.chat.typed.shared.ChatServer;
import io.typefox.lsp4j.chat.typed.shared.UserMessage;

public class ChatServerImpl implements ChatServer {
	
	private final List<ChatClient> clients = new CopyOnWriteArrayList<>();

	public CompletableFuture<Object> postMessage(UserMessage message) {
		for (ChatClient client : clients) {
			client.didPostMessage(message);
		}
		return CompletableFuture.completedFuture(new Object());
	}

	public Runnable addClient(ChatClient client) {
		this.clients.add(client);
		return () -> this.clients.remove(client);
	}

}
