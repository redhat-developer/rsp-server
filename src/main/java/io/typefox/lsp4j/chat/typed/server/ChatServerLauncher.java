package io.typefox.lsp4j.chat.typed.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.typefox.lsp4j.chat.typed.shared.ChatClient;
import io.typefox.lsp4j.chat.typed.shared.SocketLauncher;

public class ChatServerLauncher {

	public static void main(String[] args) throws Exception {
		ChatServerImpl chatServer = new ChatServerImpl();
		ExecutorService threadPool = Executors.newCachedThreadPool();

		Integer port = Integer.valueOf(args[0]);
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("The chat server is running on port " + port);
			threadPool.submit(() -> {
				while (true) {
					Socket socket = serverSocket.accept();
					SocketLauncher<ChatClient> launcher = new SocketLauncher<>(socket, chatServer, ChatClient.class,
							threadPool);

					Runnable removeClient = chatServer.addClient(launcher.getRemoteProxy());
					launcher.startListening().thenRun(removeClient);
				}
			});
			System.out.println("Enter any character to stop");
			System.in.read();
			System.exit(0);
		}
	}

}
