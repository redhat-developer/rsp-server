/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package io.typefox.lsp4j.chat.generic.client;

import static io.typefox.lsp4j.chat.generic.shared.JsonRpcConstants.DID_POST_MESSAGE;
import static io.typefox.lsp4j.chat.generic.shared.JsonRpcConstants.FETCH_MESSAGES;
import static io.typefox.lsp4j.chat.generic.shared.JsonRpcConstants.MESSAGE_CONTENT;
import static io.typefox.lsp4j.chat.generic.shared.JsonRpcConstants.MESSAGE_USER;
import static io.typefox.lsp4j.chat.generic.shared.JsonRpcConstants.POST_MESSAGE;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.Endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ChatClient implements Endpoint {

	private final Scanner scanner = new Scanner(System.in);

	public void start(Endpoint server) throws Exception {
		System.out.print("Enter your name: ");
		String user = scanner.nextLine();
		JsonArray messages = (JsonArray) server.request(FETCH_MESSAGES, null).get();
		messages.forEach(message -> this.didPostMessage((JsonObject) message));
		while (true) {
			String content = scanner.nextLine();
			JsonObject message = new JsonObject();
			message.addProperty(MESSAGE_USER, user);
			message.addProperty(MESSAGE_CONTENT, content);
			server.notify(POST_MESSAGE, message);
		}
	}

	public void didPostMessage(JsonObject message) {
		String user = message.get(MESSAGE_USER).getAsString();
		String content = message.get(MESSAGE_CONTENT).getAsString();
		System.out.println(user + ": " + content);
	}

	public CompletableFuture<?> request(String method, Object parameter) {
		throw new UnsupportedOperationException(method);
	}

	public void notify(String method, Object parameter) {
		if (DID_POST_MESSAGE.equals(method)) {
			if (parameter instanceof JsonObject) {
				this.didPostMessage((JsonObject) parameter);
			} else {
				throw new IllegalArgumentException(method + ", parameter: " + parameter);
			}
		} else {
			throw new UnsupportedOperationException(method);
		}
	}

}
