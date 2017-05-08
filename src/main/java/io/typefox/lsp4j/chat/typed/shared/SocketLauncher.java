package io.typefox.lsp4j.chat.typed.shared;

import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageConsumer;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

public class SocketLauncher<T> implements Launcher<T> {

	private final ExecutorService executor;
	private final StreamMessageProducer reader;
	private final RemoteEndpoint remoteEndpoint;
	private final T remoteProxy;

	public SocketLauncher(Socket socket, Object localService, Class<T> remoteInterface) {
		this(socket, localService, remoteInterface, Executors.newCachedThreadPool());
	}

	public SocketLauncher(Socket socket, Object localService, Class<T> remoteInterface, ExecutorService executor) {
		this.executor = executor;
		try {
			Map<String, JsonRpcMethod> supportedMethods = new LinkedHashMap<String, JsonRpcMethod>();
			supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(remoteInterface));
			supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(localService.getClass()));

			MessageJsonHandler jsonHandler = new MessageJsonHandler(supportedMethods);
			this.reader = new StreamMessageProducer(socket.getInputStream(), jsonHandler);

			MessageConsumer writer = new StreamMessageConsumer(socket.getOutputStream(), jsonHandler);
			this.remoteEndpoint = new RemoteEndpoint(writer, ServiceEndpoints.toEndpoint(localService));
			this.remoteProxy = ServiceEndpoints.toServiceObject(this.remoteEndpoint, remoteInterface);
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

	public T getRemoteProxy() {
		return remoteProxy;
	}

}
