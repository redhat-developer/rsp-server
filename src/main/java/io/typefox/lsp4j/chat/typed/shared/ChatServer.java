package io.typefox.lsp4j.chat.typed.shared;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface ChatServer {
	
	@JsonRequest
	CompletableFuture<Object> postMessage(UserMessage message);

}
