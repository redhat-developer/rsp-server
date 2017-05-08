package io.typefox.lsp4j.chat.generic.shared;

import java.net.Socket;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageConsumer;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer;

public class SocketLauncher implements Launcher<Endpoint> {

	private final ExecutorService executor;
	private final StreamMessageProducer reader;
	private final RemoteEndpoint remoteEndpoint;

	public SocketLauncher(Socket socket, Endpoint localEndpoint) {
		this(socket, localEndpoint, Executors.newCachedThreadPool());
	}

	public SocketLauncher(Socket socket, Endpoint localEndpoint, ExecutorService executor) {
		this.executor = executor;
		try {
			MessageJsonHandler jsonHandler = new MessageJsonHandler(Collections.emptyMap());
			this.reader = new StreamMessageProducer(socket.getInputStream(), jsonHandler);

			MessageConsumer writer = new StreamMessageConsumer(socket.getOutputStream(), jsonHandler);
			this.remoteEndpoint = new RemoteEndpoint(writer, localEndpoint);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public CompletableFuture<?> startListening() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return ConcurrentMessageProcessor.startProcessing(reader, remoteEndpoint, executor).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}, Executors.newSingleThreadExecutor());
	}

	public Endpoint getRemoteProxy() {
		return remoteEndpoint;
	}

}
