/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package org.jboss.tools.ssp.chat.client;

import java.util.Scanner;

import org.jboss.tools.ssp.chat.shared.ChatClient;
import org.jboss.tools.ssp.chat.shared.ChatServer;
import org.jboss.tools.ssp.chat.shared.UserMessage;

public class ChatClientImpl implements ChatClient {
	
	private final Scanner scanner = new Scanner(System.in);
	
	/**
	 * 1. Ask the user for a name
     * 2. Fetch existing messages from the remote server and display them
     * 3. Ask the user for a next message
     * 4. Post a new message to the chat server, continue with step 3
	 */
	public void start(ChatServer server) throws Exception {
		System.out.print("Enter your name: ");
		String user = scanner.nextLine();
		server.fetchMessages().get().forEach(message -> this.didPostMessage(message));
		while (true) {
			String content = scanner.nextLine();
			server.postMessage(new UserMessage(user, content));
		}
	}

	/**
	 * Display the posted message.
	 */
	public void didPostMessage(UserMessage message) {
		System.out.println(message.getUser() + ": " + message.getContent());
	}

}
