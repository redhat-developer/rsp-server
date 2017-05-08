package io.typefox.lsp4j.chat.generic.server;

import static io.typefox.lsp4j.chat.generic.shared.JsonRpcConstants.DID_POST_MESSAGE;
import static io.typefox.lsp4j.chat.generic.shared.JsonRpcConstants.POST_MESSAGE;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.lsp4j.jsonrpc.Endpoint;

import com.google.gson.JsonObject;

public class ChatServer implements Endpoint {

	private final List<Endpoint> clients = new CopyOnWriteArrayList<>();

	public CompletableFuture<Object> postMessage(JsonObject message) {
		for (Endpoint client : clients) {
			client.notify(DID_POST_MESSAGE, message);
		}
		return CompletableFuture.completedFuture(new Object());
	}

	public Runnable addClient(Endpoint client) {
		this.clients.add(client);
		return () -> this.clients.remove(client);
	}

	public CompletableFuture<?> request(String method, Object parameter) {
		if (POST_MESSAGE.equals(method)) {
			if (parameter instanceof JsonObject) {
				return postMessage((JsonObject) parameter);
			}
			throw new IllegalArgumentException(method + ", parameter: " + parameter);
		}
		throw new UnsupportedOperationException(method);
	}

	public void notify(String method, Object parameter) {
		throw new UnsupportedOperationException(method);
	}
}
