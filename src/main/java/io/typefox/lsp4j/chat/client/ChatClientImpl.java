/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package io.typefox.lsp4j.chat.client;

import java.util.Scanner;

import io.typefox.lsp4j.chat.shared.ChatClient;
import io.typefox.lsp4j.chat.shared.ChatServer;
import io.typefox.lsp4j.chat.shared.UserMessage;

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
