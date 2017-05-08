package io.typefox.lsp4j.chat.typed.client;

import java.util.Scanner;

import io.typefox.lsp4j.chat.typed.shared.ChatClient;
import io.typefox.lsp4j.chat.typed.shared.ChatServer;
import io.typefox.lsp4j.chat.typed.shared.UserMessage;

public class ChatClientImpl implements ChatClient {
	
	private final Scanner scanner = new Scanner(System.in);
	
	public void start(ChatServer server) throws Exception {
		System.out.print("Enter your name: ");
		String user = scanner.nextLine();
		server.fetchMessages().get().forEach(message -> this.didPostMessage(message));
		while (true) {
			String content = scanner.nextLine();
			server.postMessage(new UserMessage(user, content));
		}
	}

	public void didPostMessage(UserMessage message) {
		System.out.println(message.getUser() + ": " + message.getContent());
	}

}
