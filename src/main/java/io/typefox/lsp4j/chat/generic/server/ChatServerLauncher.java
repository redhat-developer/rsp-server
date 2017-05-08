package io.typefox.lsp4j.chat.generic.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.typefox.lsp4j.chat.generic.shared.SocketLauncher;

public class ChatServerLauncher {

	public static void main(String[] args) throws Exception {
		ChatServer chatServer = new ChatServer();
		ExecutorService threadPool = Executors.newCachedThreadPool();

		Integer port = Integer.valueOf(args[0]);
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
				Socket socket = serverSocket.accept();
				SocketLauncher launcher = new SocketLauncher(socket, chatServer, threadPool);
				
				Runnable removeClient = chatServer.addClient(launcher.getRemoteProxy());
				launcher.startListening().thenRun(removeClient);
			}
		}
	}

}
