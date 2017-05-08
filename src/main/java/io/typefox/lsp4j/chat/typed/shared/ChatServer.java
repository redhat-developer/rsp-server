package io.typefox.lsp4j.chat.typed.shared;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface ChatServer {
	
	@JsonRequest
	CompletableFuture<List<UserMessage>> fetchMessages();
	
	@JsonNotification
	void postMessage(UserMessage message);

}
